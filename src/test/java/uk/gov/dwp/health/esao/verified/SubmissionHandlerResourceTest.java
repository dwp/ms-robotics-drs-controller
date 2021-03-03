package uk.gov.dwp.health.esao.verified;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.verified.handlers.CaseServiceHandler;
import uk.gov.dwp.health.esao.verified.handlers.JsapsTransformationHandler;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import uk.gov.dwp.health.esao.verified.items.JsapsSubmissionRecordItem;

import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionHandlerResourceTest {
  private static final String DEFAULT_ERROR_MSG = "'Unable to process request' for any internal errors";
  private static ObjectMapper mapper = new ObjectMapper();

  private static JsapsSubmissionRecordItem jsapsSubmissionRecordItem;
  private static RequestJson serialisedCaseFromDb;
  private static RequestJson invalidSerialisedCaseFromDb;

  @Captor private ArgumentCaptor<ClaimReferenceItem> claimCaptor;

  @Captor private ArgumentCaptor<RequestJson> claimRecordCaptor;

  @Mock private JsapsTransformationHandler jsapsTransformationHandler;

  @Mock private CaseServiceHandler caseServiceHandler;

  @BeforeClass
  public static void init() throws IOException {
    String content = FileUtils.readFileToString(new File("src/test/resources/full-application-json.json"));
    jsapsSubmissionRecordItem =
        mapper.readValue(
            FileUtils.readFileToString(new File("src/test/resources/jsaps-submission-item.json")),
            JsapsSubmissionRecordItem.class);
    serialisedCaseFromDb =
        mapper.readValue(
            String.format(content, "\"AS123123D\""),
            RequestJson.class);
    invalidSerialisedCaseFromDb =
        mapper.readValue(
            String.format(content, "\"AS12312\""),
            RequestJson.class);
  }

  @Test
  public void nullJsonGenerates500() {
    SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceHandler);
    Response response = instance.generatePdfDocument(null);

    assertThat(response.getEntity().toString(), is(equalTo(DEFAULT_ERROR_MSG)));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  }

  @Test
  public void exampleReturnContainsClaimRef() throws IOException, ParseException {
    ClaimReferenceItem claimRefClass = new ObjectMapper().readValue("{\"claim_ref\":\"hello123\"}", ClaimReferenceItem.class);

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class)))
        .thenReturn(Response.ok().entity(mapper.writeValueAsString(serialisedCaseFromDb)).build());
    when(jsapsTransformationHandler.transformForJsaps(any(ClaimReferenceItem.class), any(RequestJson.class)))
        .thenReturn(mapper.writeValueAsString(jsapsSubmissionRecordItem));

    SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceHandler);
    Response response = instance.generatePdfDocument(mapper.writeValueAsString(claimRefClass));

    JsapsSubmissionRecordItem body =
        mapper.readValue(response.getEntity().toString(), JsapsSubmissionRecordItem.class);
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));
    assertTrue(body.isContentValid());

    assertThat(
        mapper.writeValueAsString(body),
        is(equalTo(mapper.writeValueAsString(jsapsSubmissionRecordItem))));

    verify(caseServiceHandler, times(1)).queryCaseService(claimCaptor.capture());
    assertThat(mapper.writeValueAsString(claimCaptor.getValue()), is(equalTo(mapper.writeValueAsString(claimRefClass))));

    verify(jsapsTransformationHandler, times(1)).transformForJsaps(claimCaptor.capture(), claimRecordCaptor.capture());
    assertThat(
        mapper.writeValueAsString(claimRecordCaptor.getValue()),
        is(equalTo(mapper.writeValueAsString(serialisedCaseFromDb))));
  }

  @Test
  public void exampleInvalidContent() throws IOException {
    ClaimReferenceItem claimRefClass = new ObjectMapper().readValue("{\"claim_ref\":\"hello123\"}", ClaimReferenceItem.class);

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class)))
        .thenReturn(Response.ok().entity(mapper.writeValueAsString(invalidSerialisedCaseFromDb)).build());

    SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceHandler);
    Response response = instance.generatePdfDocument(mapper.writeValueAsString(claimRefClass));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }

  @Test
  public void exampleNoContentResponse() throws IOException {
    ClaimReferenceItem claimRefClass = new ObjectMapper().readValue("{\"claim_ref\":\"hello123\"}", ClaimReferenceItem.class);

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class)))
        .thenReturn(Response.noContent().build());

    SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceHandler);
    Response response = instance.generatePdfDocument(mapper.writeValueAsString(claimRefClass));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_NO_CONTENT)));
  }

}
