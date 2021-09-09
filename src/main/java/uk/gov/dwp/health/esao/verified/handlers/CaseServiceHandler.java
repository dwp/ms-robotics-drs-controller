package uk.gov.dwp.health.esao.verified.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.casedata.models.CaseData;
import uk.gov.dwp.health.esao.casedata.models.Tag;
import uk.gov.dwp.health.esao.verified.application.VerifiedSubmissionHandlerConfiguration;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import uk.gov.dwp.tls.TLSConnectionBuilder;
import uk.gov.dwp.tls.TLSGeneralException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CaseServiceHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(CaseServiceHandler.class.getName());

  private final VerifiedSubmissionHandlerConfiguration configuration;
  private final TLSConnectionBuilder tlsConnectionBuilder;

  public CaseServiceHandler(
      VerifiedSubmissionHandlerConfiguration config, TLSConnectionBuilder connectionBuilder) {
    this.tlsConnectionBuilder = connectionBuilder;
    this.configuration = config;
  }

  private List<Tag> getCaseSummaryTags(ClaimReferenceItem claimReferenceItem) {
    List<Tag> tags = new ArrayList<>();
    try {
      CloseableHttpResponse getResponse =
          httpGetCaseServiceEndpoint(
              claimReferenceItem, configuration.getCaseServiceQueryEndpoint());

      CaseData caseSummary = new ObjectMapper()
                                 .readValue(EntityUtils.toString(getResponse.getEntity()),
                                     CaseData.class);
      tags = caseSummary.getTags();
    } catch (IOException
                          | KeyStoreException
                          | CertificateException
                          | NoSuchAlgorithmException
                          | UnrecoverableKeyException
                          | KeyManagementException
                          | TLSGeneralException e) {
      LOGGER.error("Error while fetching case summary tags{} {}",
          e.getClass().getName(), e.getMessage());
    }
    return tags;
  }

  public Response queryCaseService(ClaimReferenceItem claimReferenceItem) {
    Response response;
    try {
      CloseableHttpResponse getResponse =
          httpGetCaseServiceEndpoint(
              claimReferenceItem, configuration.getCaseServiceQueryEndpoint());

      if (getResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("case header exists for reference {}", claimReferenceItem.getClaimRef());

        CaseData caseSummary = new ObjectMapper()
                 .readValue(EntityUtils.toString(getResponse.getEntity()), CaseData.class);
        if (resolveVerifiedStatus(caseSummary)) {

          getResponse =
              httpGetCaseServiceEndpoint(
                  claimReferenceItem, configuration.getCaseServiceApplicationEndpoint());


          response =
              Response.status(getResponse.getStatusLine().getStatusCode())
                  .entity(EntityUtils.toString(getResponse.getEntity()))
                  .build();

          LOGGER.debug(
              "case reference {} is verified and the application is resolved",
              claimReferenceItem.getClaimRef());

        } else {
          response =
              Response.status(HttpStatus.SC_NO_CONTENT)
                  .entity(String.format(MessageConstants.NOT_VERIFIED,
                      claimReferenceItem.getClaimRef()))
                  .build();
          LOGGER.debug("the case reference {} is not verified", claimReferenceItem.getClaimRef());
        }

      } else {
        response =
            Response.status(HttpStatus.SC_NO_CONTENT)
                .entity(String.format(MessageConstants.NOT_VERIFIED,
                    claimReferenceItem.getClaimRef()))
                .build();
        LOGGER.debug("the case reference {} does not exist", claimReferenceItem.getClaimRef());
      }

    } catch (IOException
        | KeyStoreException
        | CertificateException
        | NoSuchAlgorithmException
        | UnrecoverableKeyException
        | KeyManagementException
        | TLSGeneralException e) {

      response =
          Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
              .entity(String.format(MessageConstants.ERROR_MSG_500, e.getMessage()))
              .build();
      LOGGER.error("Error while fetching case status {} {}", e.getClass().getName(),
          e.getMessage());
    }

    return response;
  }

  private CloseableHttpResponse httpGetCaseServiceEndpoint(
      ClaimReferenceItem claimReferenceItem, URL endpoint)
      throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
          KeyStoreException, KeyManagementException, TLSGeneralException, IOException {
    LOGGER.debug("create GET request for case service to endpoint {}", endpoint);
    HttpGet httpGet = new HttpGet(createCaseServiceUrl(claimReferenceItem.getClaimRef(), endpoint));

    LOGGER.info("calling case service with claim reference {}", claimReferenceItem.getClaimRef());
    CloseableHttpResponse response =
        getTlsConnectionBuilder().configureSSLConnection().execute(httpGet);
    LOGGER.debug("received {} from {}", response.getStatusLine().getStatusCode(), httpGet.getURI());

    return response;
  }

  public CloseableHttpResponse updateCase(
      ClaimReferenceItem claimReferenceItem, String status)
      throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
                 KeyStoreException, KeyManagementException, TLSGeneralException, IOException {
    URL endpoint = configuration.getCaseServiceQueryEndpoint();
    LOGGER.debug("create PATCH request for case service to endpoint {}", endpoint);
    HttpPatch httpPatch = new HttpPatch(createCaseServiceUrl(
        claimReferenceItem.getClaimRef(), endpoint));
    httpPatch.addHeader("Content-Type", "application/json-patch+json");
    Tag tag = new Tag();
    tag.setName(status);
    tag.setValue(MessageConstants.STATUS_TRUE);
    DateFormat df = new SimpleDateFormat(MessageConstants.TIMESTAMP_FORMAT);
    String timestamp = df.format(new Date());
    tag.setTimestamp(timestamp);
    List<Tag> tags = getCaseSummaryTags(claimReferenceItem);
    tags.add(tag);
    Patch patch = new Patch();
    patch.setValue(tags);
    List<Patch> list = new ArrayList();
    list.add(patch);
    String tagJson = new ObjectMapper().writeValueAsString(list);
    httpPatch.setEntity(new StringEntity(tagJson));
    CloseableHttpResponse response = null;
    if (tags.size() == 3 && status.equals(MessageConstants.STATUS_SENT)
            || tags.size() == 4 && status.equals(MessageConstants.STATUS_DELIVERED)) {
      LOGGER.info("updating claim nino {}, reference {}, with status {} as true at {}",
          tags.get(0).getValue(), claimReferenceItem.getClaimRef(), status, timestamp);
      response =
          getTlsConnectionBuilder().configureSSLConnection().execute(httpPatch);
    } else {
      LOGGER.info("claim nino {}, reference {} is already updated with status {}",
          tags.get(0).getValue(), claimReferenceItem.getClaimRef(), status);
    }
    if (response != null) {
      LOGGER.debug("received {} from {}", response.getStatusLine().getStatusCode(),
          httpPatch.getURI());
    }

    return response;
  }

  private boolean resolveVerifiedStatus(CaseData caseSummary) {
    boolean caseVerificationStatus = false;

    for (Tag item : caseSummary.getTags()) {
      if (item.getName().equalsIgnoreCase(MessageConstants.VERIFIED_MARKER)) {
        caseVerificationStatus = Boolean.valueOf(item.getValue());
        break;
      }
    }

    return caseVerificationStatus;
  }

  private TLSConnectionBuilder getTlsConnectionBuilder() {
    return tlsConnectionBuilder;
  }

  private String createCaseServiceUrl(String claimRef, URL url) {
    StringSubstitutor stringSubstitutor =
        new StringSubstitutor(
            Collections.singletonMap(configuration.getCaseServiceParameter(), claimRef),
            MessageConstants.DEFAULT_PREFIX,
            StringSubstitutor.DEFAULT_VAR_END);

    return stringSubstitutor.replace(url);
  }
}
