package uk.gov.dwp.health.esao.verified.consumers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.crypto.exceptions.EventsMessageException;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.verified.application.VerifiedSubmissionHandlerConfiguration;
import uk.gov.dwp.health.esao.verified.handlers.CaseServiceHandler;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import uk.gov.dwp.health.messageq.amazon.sns.MessagePublisher;
import uk.gov.dwp.health.messageq.items.event.EventMessage;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IncomingMgSubscriptionTest {
  private static final String ROUTING_KEY = "test-routing-key";
  private static final String TOPIC_NAME = "test-topic";
  private static final String TEST_SUBJECT = "test";
  private RequestJson applicationObject;
  private String jsonContent;

  @Mock private VerifiedSubmissionHandlerConfiguration configuration;

  @Mock private MessagePublisher snsPublisher;

  @Mock
  private CaseServiceHandler caseServiceHandler;

  @Captor
  private ArgumentCaptor<EventMessage> eventMessageCaptor;

  @Captor ArgumentCaptor<String> topicCaptor;

  @Captor ArgumentCaptor<String> subjectCaptor;

  @Before
  public void init() throws IOException {
    when(configuration.getSnsRoutingKey()).thenReturn(ROUTING_KEY);
    when(configuration.getSnsTopicName()).thenReturn(TOPIC_NAME);
    when(configuration.getSnsSubject()).thenReturn(TEST_SUBJECT);
    when(configuration.isSnsEncryptMessages()).thenReturn(true);
    jsonContent = FileUtils.readFileToString(new File("src/test/resources/full-application-json.json"));
    applicationObject = new ObjectMapper().readValue(String.format(jsonContent, "\"AA370773A\""), RequestJson.class);
  }

  @Test
  public void testVerifiedClaimIsPassedToDrs()
      throws IOException, NoSuchMethodException, InvocationTargetException, EventsMessageException, InstantiationException, IllegalAccessException, CryptoException {

    ObjectMapper mapper = new ObjectMapper();
    EventMessage incomingMsg =
        mapper
            .readValue(new File("src/test/resources/sqsMessage.json"), EventMessage.class);

    Response response =
        Response.status(200)
            .entity(mapper.writeValueAsString(applicationObject))
            .build();

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class))).thenReturn(response);

    IncomingMgSubscription instance =
        new IncomingMgSubscription(configuration, caseServiceHandler, snsPublisher);
    instance.handleMessage(incomingMsg);

    verify(snsPublisher).publishMessageToSnsTopic(eq(true), topicCaptor.capture(), subjectCaptor.capture(), eventMessageCaptor.capture(), eq(null));
    assertThat(eventMessageCaptor.getValue().getMetaData().getRoutingKey(), is(equalTo(ROUTING_KEY)));
    assertThat(subjectCaptor.getValue(), is(equalTo(TEST_SUBJECT)));
    assertThat(topicCaptor.getValue(), is(equalTo(TOPIC_NAME)));

    String payload = mapper.writeValueAsString(mapper.readValue(eventMessageCaptor.getValue().serialisedBodyContentsToJson(), Map.class).get("payload"));
    RequestJson requestJson = mapper.readValue(payload, RequestJson.class);

    JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(mapper.readValue(eventMessageCaptor.getValue().serialisedBodyContentsToJson(), Map.class).get("metadata")));
    assertThat(jsonNode.get("businessUnitID").asText(), is(equalTo("35")));
    assertThat(jsonNode.get("documentType").asInt(), is(equalTo(10467)));
    assertThat(jsonNode.get("classification").asInt(), is(equalTo(0)));
    assertThat(jsonNode.get("documentSource").asInt(), is(equalTo(4)));
    assertThat(jsonNode.get("benefitType").asInt(), is(equalTo(37)));

    assertThat(String.format("%s%s",
        jsonNode.get("nino").get("ninoBody").asText(),
        jsonNode.get("nino").get("ninoSuffix").asText()),
        is(equalTo(requestJson.getDataCapture().getNino())));

    assertThat(jsonNode.get("postCode").asText(), is(equalTo(requestJson.getApplicant().getResidenceAddress().getPostCode())));

    assertThat(payload, is(equalTo(mapper.writeValueAsString(applicationObject))));
  }


  @Test(expected = ValidationException.class)
  public void testEmptyClaimRef() throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    EventMessage incomingMsg =
        mapper
            .readValue(new File("src/test/resources/sqsMessageEmptyClaimRef.json"), EventMessage.class);

    Response response =
        Response.status(200)
            .entity(mapper.writeValueAsString(applicationObject))
            .build();

    IncomingMgSubscription instance =
        new IncomingMgSubscription(configuration, caseServiceHandler, snsPublisher);
    instance.handleMessage(incomingMsg);

  }

  @Test(expected = ValidationException.class)
  public void testInvalidSubmissionItems() throws IOException {

    applicationObject = new ObjectMapper().readValue(String.format(jsonContent, "\"AA37077\""), RequestJson.class);
    ObjectMapper mapper = new ObjectMapper();
    EventMessage incomingMsg =
        mapper
            .readValue(new File("src/test/resources/sqsMessage.json"), EventMessage.class);

    Response response =
        Response.status(200)
            .entity(mapper.writeValueAsString(applicationObject))
            .build();

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class))).thenReturn(response);

    IncomingMgSubscription instance =
        new IncomingMgSubscription(configuration, caseServiceHandler, snsPublisher);
    instance.handleMessage(incomingMsg);

  }

}
