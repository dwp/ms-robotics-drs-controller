package uk.gov.dwp.health.esao.verified.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.health.esao.casedata.models.Tag;
import uk.gov.dwp.health.esao.verified.application.VerifiedSubmissionHandlerConfiguration;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import uk.gov.dwp.tls.TLSConnectionBuilder;
import uk.gov.dwp.tls.TLSGeneralException;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceHandlerTest {
  private static final String CLAIM_REF_JSON = "{\"claim_ref\":\"%s\"}";
  private TLSConnectionBuilder tlsConnectionBuilder;

  @Mock private VerifiedSubmissionHandlerConfiguration configuration;

  @Rule public WireMockRule caseServiceMock = new WireMockRule(wireMockConfig().port(8877));

  @Before
  public void setup() throws MalformedURLException {
    tlsConnectionBuilder = new TLSConnectionBuilder(null, null, null, null);

    when(configuration.getCaseServiceApplicationEndpoint())
        .thenReturn(new URL("http://localhost:8877/v1/cases/:{caseId}/application"));
    when(configuration.getCaseServiceQueryEndpoint())
        .thenReturn(new URL("http://localhost:8877/v1/cases/:{caseId}"));
    when(configuration.getCaseServiceParameter()).thenReturn("caseId");
  }

  @Test
  public void caseServiceApplicationReturnsVerified() throws IOException {
    String expectedJson = buildVerifiedClaimDocument(true, true);
    String caseId = "abc123";

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s/application", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    Response response = instance.queryCaseService(generateClaimReference(caseId));

    assertNotNull(response);
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));
    assertThat(response.getEntity(), is(equalTo(expectedJson)));
  }

  @Test
  public void caseServiceApplicationReturnsNotVerified() throws IOException {
    String expectedJson = buildVerifiedClaimDocument(true, false);
    String caseId = "abc987";

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    Response response = instance.queryCaseService(generateClaimReference(caseId));

    assertNotNull(response);
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_NO_CONTENT)));
    assertThat(
        response.getEntity(),
        is(equalTo(String.format("The json ClaimRef %s does not exist or is not " +
              "available for robotics", caseId))));
  }

  @Test
  public void caseServiceApplicationReturnsNoVerifyBlock() throws IOException {
    String expectedJson = buildVerifiedClaimDocument(false, false);
    String caseId = "xxx567";

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    Response response = instance.queryCaseService(generateClaimReference(caseId));

    assertNotNull(response);
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_NO_CONTENT)));
    assertThat(
        response.getEntity(),
        is(equalTo(String.format("The json ClaimRef %s does not exist or is not " +
              "available for robotics", caseId))));
  }

  @Test
  public void caseSummaryReturns404() throws IOException {
    String caseId = "abc123";

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withStatus(404)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    Response response = instance.queryCaseService(generateClaimReference(caseId));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_NO_CONTENT)));
  }

  @Test
  public void caseSummaryThrowsAnError() throws IOException {
    caseServiceMock.stop();

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);

    Response response = instance.queryCaseService(generateClaimReference("xxx999xxx"));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  }

  @Test
  public void caseApplicationReturns404() throws IOException {
    String expectedJson = buildVerifiedClaimDocument(true, true);
    String caseId = "bad3456";

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s/application", caseId)))
            .willReturn(aResponse().withStatus(404)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    Response response = instance.queryCaseService(generateClaimReference(caseId));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_NOT_FOUND)));
  }

  @Test
  public void caseApplicationReturns500() throws IOException {
    String expectedJson = buildVerifiedClaimDocument(true, true);
    String caseId = "bad3456";

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s/application", caseId)))
            .willReturn(aResponse().withStatus(500)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    Response response = instance.queryCaseService(generateClaimReference(caseId));
    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  }

  private ClaimReferenceItem generateClaimReference(String claimRef) throws IOException {
    return new ObjectMapper()
        .readValue(String.format(CLAIM_REF_JSON, claimRef), ClaimReferenceItem.class);
  }

  @Test
  public void caseServiceUpdateCaseStatusSent() throws IOException, CertificateException,
       UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
       KeyManagementException, TLSGeneralException {
    String expectedJson = buildVerifiedClaimDocument(true, true);
    String caseId = "xxx567";
    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));
    caseServiceMock.stubFor(
        patch(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    CloseableHttpResponse response = instance.updateCase(generateClaimReference(caseId), "sent");

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode(), is(equalTo(HttpStatus.SC_OK)));
  }

  @Test
  public void caseServiceUpdateCaseStatusSentTwice() throws IOException, CertificateException,
                                                                UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
                                                                KeyManagementException, TLSGeneralException {
    String expectedJson = buildSentClaimDocument(true, true);
    String caseId = "xxx567";
    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));
    caseServiceMock.stubFor(
        patch(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    CloseableHttpResponse response = instance.updateCase(generateClaimReference(caseId), "sent");

    assertNull(response);
  }

  @Test
  public void caseServiceUpdateCaseStatusDelivered() throws IOException, CertificateException,
                                                           UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
                                                           KeyManagementException, TLSGeneralException {
    String expectedJson = buildSentClaimDocument(true, true);
    String caseId = "xxx567";
    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));
    caseServiceMock.stubFor(
        patch(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(200)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    CloseableHttpResponse response = instance.updateCase(generateClaimReference(caseId), "delivered");

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode(), is(equalTo(HttpStatus.SC_OK)));
  }

  @Test
  public void caseServiceUpdateCaseStatusReturns500() throws IOException, CertificateException,
                         UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
                         KeyManagementException, TLSGeneralException {
    String expectedJson = buildVerifiedClaimDocument(true, true);
    String caseId = "xxx567";

    caseServiceMock.stubFor(
        patch(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(500)));

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", caseId)))
            .willReturn(aResponse().withBody(expectedJson).withStatus(500)));

    CaseServiceHandler instance = new CaseServiceHandler(configuration, tlsConnectionBuilder);
    CloseableHttpResponse response = instance.updateCase(generateClaimReference(caseId), "sent");

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  }

  private String buildVerifiedClaimDocument(
      boolean includeVerificationBlock, boolean verificationStatus) throws IOException {
    String contents = FileUtils.readFileToString(new File("src/test/resources/verified-case.json"));
    if (includeVerificationBlock) {
      contents =
          String.format(
              String.format(
                  contents,
                  ",\n"
                      + "    {\n"
                      + "      \"name\": \"verified\",\n"
                      + "      \"value\": %s\n"
                      + "    }"),
              Boolean.toString(verificationStatus));

    } else {
      contents = String.format(contents, "");
    }

    return contents;
  }

  private String buildSentClaimDocument(
      boolean includeSentBlock, boolean sentStatus) throws IOException {
    String contents = FileUtils.readFileToString(new File("src/test/resources/verified-case.json"));
    if (includeSentBlock) {
      contents =
          String.format(
              String.format(
                  contents,
                  ",\n"
                      + "    {\n"
                      + "      \"name\": \"verified\",\n"
                      + "      \"value\": \"true\"\n"
                      + "    }"
                      +",\n"
                      + "    {\n"
                      + "      \"name\": \"sent\",\n"
                      + "      \"value\": %s\n"
                      + "    }"
                      +"\n"),
              Boolean.toString(sentStatus));

    } else {
      contents = String.format(contents, "");
    }

    return contents;
  }
}
