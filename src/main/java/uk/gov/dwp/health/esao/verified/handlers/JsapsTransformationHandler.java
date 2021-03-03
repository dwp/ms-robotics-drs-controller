package uk.gov.dwp.health.esao.verified.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import uk.gov.dwp.health.esao.verified.items.JsapsSubmissionRecordItem;

import javax.validation.ValidationException;
import java.io.IOException;
import java.text.ParseException;

public class JsapsTransformationHandler {

  public String transformForJsaps(
      ClaimReferenceItem claimReferenceItem, RequestJson applicationRecordItem)
      throws IOException, ParseException {

    JsapsSubmissionRecordItem jsapsSubmissionRecordItem =
        new JsapsSubmissionRecordItem.JsapsSubmissionItemBuilder()
            .withClaimRef(claimReferenceItem.getClaimRef())
            .withRequestJsonClass(applicationRecordItem)
            .build();

    if (!jsapsSubmissionRecordItem.isContentValid()) {
      throw new ValidationException("Mongo record transformation error occurred");
    }

    return new ObjectMapper().writeValueAsString(jsapsSubmissionRecordItem);
  }
}
