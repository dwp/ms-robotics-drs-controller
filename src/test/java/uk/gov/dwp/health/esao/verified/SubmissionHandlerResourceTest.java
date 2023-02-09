package uk.gov.dwp.health.esao.verified;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionHandlerResourceTest {
  private static final String DEFAULT_ERROR_MSG =
      "'Unable to process request' for any internal errors";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static JsapsSubmissionRecordItem jsapsSubmissionRecordItem;
  private static RequestJson serialisedCaseFromDb;
  private static RequestJson invalidSerialisedCaseFromDb;

  @Captor private ArgumentCaptor<ClaimReferenceItem> claimCaptor;

  @Captor private ArgumentCaptor<RequestJson> claimRecordCaptor;

  @Mock private JsapsTransformationHandler jsapsTransformationHandler;

  @Mock private CaseServiceHandler caseServiceHandler;

  @BeforeClass
  public static void init() throws IOException {
    String content =
        FileUtils.readFileToString(
            new File("src/test/resources/full-application-json.json"), StandardCharsets.UTF_8);
    jsapsSubmissionRecordItem =
        MAPPER.readValue(
            FileUtils.readFileToString(
                new File("src/test/resources/jsaps-submission-item.json"), StandardCharsets.UTF_8),
            JsapsSubmissionRecordItem.class);
    serialisedCaseFromDb =
        MAPPER.readValue(String.format(content, "\"AS123123D\""), RequestJson.class);
    invalidSerialisedCaseFromDb =
        MAPPER.readValue(String.format(content, "\"AS12312\""), RequestJson.class);
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
    ClaimReferenceItem claimRefClass =
        new ObjectMapper().readValue("{\"claim_ref\":\"hello123\"}", ClaimReferenceItem.class);

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class)))
        .thenReturn(Response.ok().entity(MAPPER.writeValueAsString(serialisedCaseFromDb)).build());
    when(jsapsTransformationHandler.transformForJsaps(
            any(ClaimReferenceItem.class), any(RequestJson.class)))
        .thenReturn(MAPPER.writeValueAsString(jsapsSubmissionRecordItem));

    SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceHandler);
    Response response = instance.generatePdfDocument(MAPPER.writeValueAsString(claimRefClass));

    JsapsSubmissionRecordItem body =
        MAPPER.readValue(response.getEntity().toString(), JsapsSubmissionRecordItem.class);
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));
    assertTrue(body.isContentValid());

    assertThat(
        MAPPER.writeValueAsString(body),
        is(equalTo(MAPPER.writeValueAsString(jsapsSubmissionRecordItem))));

    verify(caseServiceHandler, times(1)).queryCaseService(claimCaptor.capture());
    assertThat(MAPPER.writeValueAsString(claimCaptor.getValue()), is(equalTo(MAPPER.writeValueAsString(claimRefClass))));

    verify(jsapsTransformationHandler, times(1)).transformForJsaps(claimCaptor.capture(), claimRecordCaptor.capture());
    assertThat(
        MAPPER.writeValueAsString(claimCaptor.getValue()),
        is(equalTo(MAPPER.writeValueAsString(claimRefClass))));

    verify(jsapsTransformationHandler, times(1))
        .transformForJsaps(claimCaptor.capture(), claimRecordCaptor.capture());
    assertThat(
        MAPPER.writeValueAsString(claimRecordCaptor.getValue()),
        is(equalTo(MAPPER.writeValueAsString(serialisedCaseFromDb))));
  }

  @Test
  public void exampleInvalidContent() throws IOException {
    ClaimReferenceItem claimRefClass =
        new ObjectMapper().readValue("{\"claim_ref\":\"hello123\"}", ClaimReferenceItem.class);

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class)))
        .thenReturn(
            Response.ok().entity(MAPPER.writeValueAsString(invalidSerialisedCaseFromDb)).build());

    SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceHandler);
    Response response = instance.generatePdfDocument(MAPPER.writeValueAsString(claimRefClass));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }

  @Test
  public void exampleNoContentResponse() throws IOException {
    ClaimReferenceItem claimRefClass =
        new ObjectMapper().readValue("{\"claim_ref\":\"hello123\"}", ClaimReferenceItem.class);

    when(caseServiceHandler.queryCaseService(any(ClaimReferenceItem.class)))
        .thenReturn(Response.noContent().build());

    SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceHandler);
    Response response = instance.generatePdfDocument(MAPPER.writeValueAsString(claimRefClass));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_NO_CONTENT)));
  }
}
