package uk.gov.dwp.health.esao.verified.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.components.drs.DrsPayloadBuilder;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.crypto.exceptions.EventsMessageException;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.verified.application.VerifiedSubmissionHandlerConfiguration;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.handlers.CaseServiceHandler;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import uk.gov.dwp.health.esao.verified.items.drs.EsaDrsMetadata;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.messageq.amazon.sns.MessagePublisher;
import uk.gov.dwp.health.messageq.amazon.sqs.events.SqsReceivedMessageEvent;
import uk.gov.dwp.health.messageq.items.event.EventMessage;
import uk.gov.dwp.health.messageq.items.event.MetaData;
import uk.gov.dwp.regex.InvalidNinoException;
import uk.gov.dwp.regex.NinoValidator;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.UUID;

public class IncomingMgSubscription implements SqsReceivedMessageEvent {
  private static final Logger LOG = LoggerFactory.getLogger(IncomingMgSubscription.class.getName());
  private final VerifiedSubmissionHandlerConfiguration configuration;
  private final CaseServiceHandler caseServiceHandler;
  private final MessagePublisher snsPublisher;

  public IncomingMgSubscription(
      VerifiedSubmissionHandlerConfiguration configuration,
      CaseServiceHandler caseServiceHandler,
      MessagePublisher messagePublisher) {
    this.caseServiceHandler = caseServiceHandler;
    this.snsPublisher = messagePublisher;
    this.configuration = configuration;
  }

  @Override
  @SuppressWarnings("squid:S3776")
  public void handleMessage(EventMessage messageContent) throws IOException {

    ClaimReferenceItem claimReferenceItem =
        new ObjectMapper()
            .readValue(messageContent.serialisedBodyContentsToJson(), ClaimReferenceItem.class);

    if (claimReferenceItem.isContentValid()) {

      Response response = caseServiceHandler.queryCaseService(claimReferenceItem);
      LOG.debug(
          "call case service for a single application, reference '{}'",
          claimReferenceItem.getClaimRef());

      if (response.getStatus() == HttpStatus.SC_OK) {

        RequestJson submissionItem =
            new ObjectMapper().readValue(response.getEntity().toString(), RequestJson.class);

        if (submissionItem.isContentValid()) {
          try {
            LOG.info(
                "publishing verified submission record to DRS for claim ref {}",
                claimReferenceItem.getClaimRef());
            String correlationId = drsDispatchPayload(claimReferenceItem, submissionItem);
            LOG.info("successfully sent to DRS with correlationId = {}", correlationId);

            caseServiceHandler.updateCase(claimReferenceItem, MessageConstants.STATUS_SENT);

            LOG.info(
                "claim reference {} is successfully updated with status {} as true",
                claimReferenceItem.getClaimRef(),
                MessageConstants.STATUS_SENT);

          } catch (EventsMessageException e) {
            throw new IOException("send to DRS failed", e);
          } catch (InvalidNinoException e) {
            throw new IOException("nino format is invalid", e);
          } catch (Exception e) {
            throw new IOException("Unable to update case status", e);
          }

        } else {
          throw new ValidationException(
              String.format(
                  "the returned application record contains invalid items for ref %s",
                  claimReferenceItem.getClaimRef()));
        }
      } else {
        throw new ValidationException(
            String.format(
                "case record service error error %d :: %s",
                response.getStatus(), response.getEntity().toString()));
      }

    } else {
      throw new ValidationException("the input 'claim_ref' is invalid or empty");
    }
  }

  private String drsDispatchPayload(ClaimReferenceItem claimReference, RequestJson submissionItem)
      throws IOException, EventsMessageException, InvalidNinoException {
    EsaDrsMetadata drsMetadata = new EsaDrsMetadata();
    final ObjectMapper mapper = new ObjectMapper();

    drsMetadata.setClaimRef(claimReference.getClaimRef());

    // mandatory items
    drsMetadata.setBusinessUnitID("35");
    drsMetadata.setDocumentType(10467);
    drsMetadata.setClassification(0);
    drsMetadata.setDocumentSource(4);

    // optional metadata items
    drsMetadata.setBenefitType(37);
    drsMetadata.setNino(new NinoValidator(submissionItem.getDataCapture().getNino()));
    drsMetadata.setPostCode(submissionItem.getApplicant().getResidenceAddress().getPostCode());
    if (submissionItem.getApplicant().getContactOptionsList().isEmpty()) {
      drsMetadata.setCustomerMobileNumber(MessageConstants.BLANK);
    } else {
      drsMetadata.setCustomerMobileNumber(
          DataTransformation.transformToJsapsPhoneNumber(
              submissionItem.getApplicant().getContactOptionsList().stream()
                  .filter(contact -> contact.isPreferred())
                  .findFirst()
                  .get()
                  .getData()));
    }

    UUID correlationId = UUID.randomUUID();

    MetaData metaData = new MetaData(Collections.singletonList(configuration.getSnsSubject()));
    metaData.setRoutingKey(configuration.getSnsRoutingKey());
    metaData.setCorrelationId(correlationId.toString());

    EventMessage messageQueueEvent = new EventMessage();
    messageQueueEvent.setMetaData(metaData);
    messageQueueEvent.setBodyContents(
        mapper.readValue(
            new DrsPayloadBuilder<RequestJson, EsaDrsMetadata>()
                .getDrsPayloadJson(submissionItem, drsMetadata),
            Object.class));

    try {
      snsPublisher.publishMessageToSnsTopic(
          configuration.isSnsEncryptMessages(),
          configuration.getSnsTopicName(),
          configuration.getSnsSubject(),
          messageQueueEvent,
          null);

    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | EventsMessageException
        | CryptoException e) {

      throw new EventsMessageException(
          String.format("%s :: %s", e.getClass().getName(), e.getMessage()));
    }

    return correlationId.toString();
  }
}
