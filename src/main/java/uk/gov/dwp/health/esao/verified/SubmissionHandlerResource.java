package uk.gov.dwp.health.esao.verified;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.handlers.CaseServiceHandler;
import uk.gov.dwp.health.esao.verified.handlers.JsapsTransformationHandler;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;


@Path("/")
public class SubmissionHandlerResource {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(SubmissionHandlerResource.class.getName());
  private final JsapsTransformationHandler jsapsTransformationHandler;
  private final CaseServiceHandler caseServiceHandler;

  public SubmissionHandlerResource(
      JsapsTransformationHandler jsapsTransformationHandler,
      CaseServiceHandler caseServiceHandler) {
    this.jsapsTransformationHandler = jsapsTransformationHandler;
    this.caseServiceHandler = caseServiceHandler;
  }

  @POST
  @Path("iagQueryEsaSubmissionByClaimRef")
  public Response generatePdfDocument(String json) {
    Response queryResponse;
    Response response;
    try {

      ClaimReferenceItem claimReferenceItem =
          new ObjectMapper().readValue(json, ClaimReferenceItem.class);
      if (claimReferenceItem.isContentValid()) {

        queryResponse = caseServiceHandler.queryCaseService(claimReferenceItem);
        LOGGER.debug(
            "call case service for a single application, reference '{}'",
            claimReferenceItem.getClaimRef());

        if (queryResponse.getStatus() == HttpStatus.SC_OK) {

          LOGGER.debug(
              "Reference '{}' is a verified case, parse reply", claimReferenceItem.getClaimRef());
          RequestJson submissionItem =
              new ObjectMapper().readValue(queryResponse.getEntity().toString(), RequestJson.class);

          if (submissionItem.isContentValid()) {
            LOGGER.info(
                "reference {} data is all valid, begin transformation for IAG robotics",
                claimReferenceItem.getClaimRef());
            response =
                Response.ok()
                    .entity(
                        jsapsTransformationHandler.transformForJsaps(
                            claimReferenceItem, submissionItem))
                    .build();
            LOGGER.info(
                "response code {} - reference {} found and response sent",
                HttpStatus.SC_OK,
                claimReferenceItem.getClaimRef());
            caseServiceHandler.updateCase(claimReferenceItem,
                MessageConstants.STATUS_DELIVERED);

          } else {
            throw new ValidationException(
                String.format(
                    "the returned application record contains invalid items for ref %s",
                    claimReferenceItem.getClaimRef()));
          }

        } else if (queryResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
          response =
              Response.noContent().build();
          LOGGER.info(
              "response code {} - reference {} not found or exist",
              HttpStatus.SC_NO_CONTENT,
              claimReferenceItem.getClaimRef());

        } else {
          throw new ValidationException(
              String.format(
                  "case record service error %d :: %s",
                  queryResponse.getStatus(), queryResponse.getEntity().toString()));
        }

      } else {
        throw new ValidationException("the input 'claim_ref' is invalid or empty");
      }
    } catch (JsonParseException | JsonMappingException | ValidationException e) {
      response = Response.status(HttpStatus.SC_BAD_REQUEST)
                     .entity(MessageConstants.ERROR_MSG_400).build();
      LOGGER.warn(
          "response code {} - payload contains invalid items",
          HttpStatus.SC_BAD_REQUEST);
      LOGGER.error(e.getClass().getName(), e);
    } catch (Exception e) {
      response =
        Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .entity(MessageConstants.ERROR_MSG_500).build();
      LOGGER.warn(
          "response code {} - unable to process request",
          HttpStatus.SC_INTERNAL_SERVER_ERROR);
      LOGGER.error(e.getClass().getName(), e);
    }

    return response;
  }
}
