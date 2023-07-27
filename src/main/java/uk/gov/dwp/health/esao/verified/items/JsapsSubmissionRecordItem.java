package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.shared.models.Employments;
import uk.gov.dwp.health.esao.shared.models.Insurances;
import uk.gov.dwp.health.esao.shared.models.NonMandatoryAddress;
import uk.gov.dwp.health.esao.shared.models.Pensions;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.shared.models.VoluntaryWork;
import uk.gov.dwp.health.esao.shared.util.StatutoryExtraPaymentEnum;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;
import uk.gov.dwp.regex.NinoValidator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("deprecation")
public class JsapsSubmissionRecordItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(JsapsSubmissionRecordItem.class.getName());

  @NotNull
  @JsonAlias({"date_submitted"})
  @JsonProperty("submitted_date")
  private String submittedDate;

  @NotNull
  @JsonAlias({"ref"})
  @JsonProperty("claim_ref")
  private String ref;

  @NotNull
  @JsonProperty("nino")
  private String nino;

  @NotNull
  @JsonAlias({"forenames"})
  @JsonProperty("first_name")
  private String firstName;

  @NotNull
  @JsonProperty("surname")
  private String surname;

  @NotNull
  @JsonAlias({"dob"})
  @JsonProperty("date_of_birth")
  private String dob;

  @NotNull
  @JsonProperty("contact_number")
  private String contactNumber;

  @NotNull
  @JsonProperty("claimant_address")
  private AddressTransformItem claimantAddress;

  @NotNull
  @JsonProperty("use_claimant_address")
  private String useClaimantAddress;

  @JsonProperty("correspondence_address")
  private AddressTransformItem correspondanceAddress;

  @JsonProperty("language_completed")
  private String languageCompleted;

  @JsonProperty("welsh_postcode")
  private String welshPostcode;

  @JsonProperty("written_comms")
  private String writtenComms;

  @JsonProperty("verbal_comms")
  private String verbalComms;

  @NotNull
  @JsonProperty("claim_start_date")
  private String claimStartDate;

  @NotNull
  @JsonProperty("ssp_received")
  private String sspReceived;

  @JsonProperty("ssp_end_date")
  private String sspEndDate;

  @NotNull
  @JsonProperty("receiving_statutory_extra")
  private String receivingStatExtra;

  @JsonProperty("expected_fit_for_work_date")
  private String fitForWorkDate;

  @NotNull
  @JsonProperty("voluntary_work_activity")
  private String voluntaryWorkActivity;

  @NotNull
  @JsonProperty("voluntary_work_assignments")
  private List<VoluntaryWorkTransformItem> voluntaryWorks;

  @NotNull
  @JsonProperty("employed_work")
  private String employedWork;

  @NotNull
  @JsonProperty("employed_work_details")
  private List<EmployedWorkTransformItem> employedItems;

  @NotNull
  @JsonProperty("receiving_pension")
  private String receivingPension;

  @NotNull
  @JsonProperty("pension_details")
  private List<PensionProviderTransformItem> pensionItems;

  @NotNull
  @JsonProperty("receiving_permanent_health_insurance")
  private String receivingHealthInsurance;

  @NotNull
  @JsonProperty("health_insurance_details")
  private List<HealthProviderTransformItem> healthItems;

  @NotNull
  @JsonProperty("currently_in_hospital")
  private String currentlyInHospital;

  @JsonProperty("admission_date")
  private String admissionDate;

  @NotNull
  @JsonProperty("pregnant")
  private String pregnant;

  @JsonAlias("due_date")
  @JsonProperty("baby_due_date")
  private String babyDueDate;

  @NotNull
  @JsonProperty("special_rule_application")
  private String specialRuleApplication;

  @NotNull
  @JsonProperty("ds1500_already_submitted")
  private String ds1500submitted;

  @NotNull
  @JsonProperty("coronavirus")
  private String coronavirus;

  @NotNull
  @JsonProperty("consent_dwp_share_with_doc")
  private String consentDwpShareWithDoc;

  @NotNull
  @JsonProperty("consent_doc_share_with_dwp")
  private String consentDocShareWithDwp;

  @JsonProperty("coronavirus_date")
  private String coronavirusDate;

  @JsonProperty("other_health_condition")
  private String otherHealthCondition;

  @JsonAlias("conditions")
  @JsonProperty("medical_conditions")
  private List<ConditionTransformItem> conditionItems;

  @NotNull
  @JsonProperty("gp_surgery_details")
  private MedicalDetailsTransformItem gpDetails;

  @NotNull
  @JsonProperty("banking_details")
  private BankDetailsTransformItem bankingDetails;

  @Override
  @JsonIgnore
  @SuppressWarnings("squid:S3776") // keeping cognitive complexity for ease of reading
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid =
          !getSubmittedDate().isEmpty()
              && ValidationUtils.isValidDate(LOG, "submitted_date", getSubmittedDate());
      ValidationUtils.logOutput(LOG, "submitted_date", isValid);
    }

    if (isValid) {
      isValid = !getRef().isEmpty();
      ValidationUtils.logOutput(LOG, "claim_ref", isValid);
    }

    if (isValid) {
      isValid = NinoValidator.validateNINO(getNino());
      ValidationUtils.logOutput(LOG, "nino", isValid);
    }

    if (isValid) {
      isValid = !getFirstName().isEmpty();
      ValidationUtils.logOutput(LOG, "first_name", isValid);
    }

    if (isValid) {
      isValid = !getSurname().isEmpty();
      ValidationUtils.logOutput(LOG, "surname", isValid);
    }

    if (isValid) {
      isValid = ValidationUtils.validDateNotInTheFuture(LOG, "date_of_birth", getDob());
    }

    if (isValid) {
      isValid = getClaimantAddress().isContentValid();
      ValidationUtils.logOutput(LOG, "claimant_address", isValid);
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "use_claimant_address", getUseClaimantAddress());
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "ssp_received", getSspReceived());
    }

    if (isValid && !getSspEndDate().isEmpty()) {
      isValid = ValidationUtils.isValidDate(LOG, "ssp_end_date", getSspEndDate());
    }

    if (isValid) {
      isValid =
          ValidationUtils.validateStatutoryExtras(
              LOG, "receiving_statutory_extra", getReceivingStatExtra());
    }

    if (isValid && !getFitForWorkDate().isEmpty()) {
      isValid = ValidationUtils.isValidDate(LOG, "expected_fit_for_work_date", getFitForWorkDate());
    }

    if (isValid) {
      isValid =
          ValidationUtils.isValidYN(LOG, "voluntary_work_activity", getVoluntaryWorkActivity());
    }

    if (isValid && DataTransformation.toBoolYN(getVoluntaryWorkActivity())) {
      for (VoluntaryWorkTransformItem item : getVoluntaryWorks()) {
        isValid = item.isContentValid();

        if (!isValid) {
          break;
        }
      }
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "employed_work", getEmployedWork());
    }

    if (isValid && DataTransformation.toBoolYN(getEmployedWork())) {
      for (EmployedWorkTransformItem item : getEmployedItems()) {
        isValid = item.isContentValid();

        if (!isValid) {
          break;
        }
      }
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "receiving_pension", getReceivingPension());
    }

    if (isValid && DataTransformation.toBoolYN(getReceivingPension())) {
      for (PensionProviderTransformItem item : getPensionItems()) {
        isValid = item.isContentValid();

        if (!isValid) {
          break;
        }
      }
    }

    if (isValid) {
      isValid =
          ValidationUtils.isValidYN(
              LOG, "receiving_permanent_health_insurance", getReceivingHealthInsurance());
    }

    if (isValid && DataTransformation.toBoolYN(getReceivingHealthInsurance())) {
      for (HealthProviderTransformItem item : getHealthItems()) {
        isValid = item.isContentValid();

        if (!isValid) {
          break;
        }
      }
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "currently_in_hospital", getCurrentlyInHospital());
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "pregnant", getPregnant());
    }

    if (isValid) {
      isValid =
          ValidationUtils.isValidYN(LOG, "special_rule_application", getSpecialRuleApplication());
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "coronavirus", getCoronavirus());
    }

    if (isValid) {
      isValid =
        ValidationUtils.isValidYN(LOG, "consent_dwp_share_with_doc", getConsentDwpShareWithDoc());
    }

    if (isValid) {
      isValid =
        ValidationUtils.isValidYN(LOG, "consent_doc_share_with_dwp", getConsentDocShareWithDwp());
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "ds1500_already_submitted", getDs1500submitted());
    }

    if (isValid && getConditionItems() != null) {
      for (ConditionTransformItem item : getConditionItems()) {
        isValid = item.isContentValid();

        if (!isValid) {
          break;
        }
      }
    }

    if (isValid) {
      isValid = getGpDetails().isContentValid();
    }

    if (isValid) {
      isValid = getBankingDetails().isContentValid();
    }

    return isValid;
  }

  public static class JsapsSubmissionItemBuilder {
    private static final Logger LOG =
        LoggerFactory.getLogger(JsapsSubmissionItemBuilder.class.getName());

    private RequestJson caseRecord;
    private String claimRef;

    public JsapsSubmissionItemBuilder withRequestJsonClass(RequestJson inputClass) {
      validateAndTransformInput(inputClass);
      return this;
    }

    public JsapsSubmissionItemBuilder withRequestJson(String inputJson) throws IOException {
      validateAndTransformInput(new ObjectMapper().readValue(inputJson, RequestJson.class));
      return this;
    }

    public JsapsSubmissionItemBuilder withClaimRef(String claimRef) {
      this.claimRef = claimRef;
      return this;
    }

    private void validateAndTransformInput(RequestJson inputRecord) {
      if (!inputRecord.isContentValid()) {
        throw new ValidationException(
            "incoming RequestJson record contains invalid items, rejecting");
      }

      this.caseRecord = inputRecord;
      try {

        // update main item date formats
        getCaseRecord()
            .setSubmissionDate(
                DataTransformation.transformToJsapsDate(
                    LOG, "submitted_date", inputRecord.getSubmissionDate()));
        getCaseRecord()
            .getApplicant()
            .setDateOfBirth(
                DataTransformation.transformToJsapsDate(
                    LOG, "dob", inputRecord.getApplicant().getDateOfBirth()));
        getCaseRecord()
            .getDataCapture()
            .setClaimStartDate(
                DataTransformation.transformToJsapsDate(
                    LOG, "claim_start_date", inputRecord.getDataCapture().getClaimStartDate()));
        getCaseRecord()
            .getDataCapture()
            .setSspEnd(
                DataTransformation.transformToJsapsDate(
                    LOG, "ssp_end", inputRecord.getDataCapture().getSspEnd()));
        getCaseRecord()
            .getDataCapture()
            .setSspRecentEnd(
                DataTransformation.transformToJsapsDate(
                    LOG, "ssp_recent_end", inputRecord.getDataCapture().getSspRecentEnd()));
        getCaseRecord()
            .getDataCapture()
            .setBackToWorkDate(
                DataTransformation.transformToJsapsDate(
                    LOG, "back_to_work_date", inputRecord.getDataCapture().getBackToWorkDate()));
        getCaseRecord()
            .getDataCapture()
            .setClaimEndDate(
                DataTransformation.transformToJsapsDate(
                    LOG, "claim_end_date", inputRecord.getDataCapture().getClaimEndDate()));
        getCaseRecord()
            .getDataCapture()
            .setHospitalAdmissionDate(
                DataTransformation.transformToJsapsDate(
                    LOG,
                    "hospital_admission_date",
                    inputRecord.getDataCapture().getHospitalAdmissionDate()));
        getCaseRecord()
            .getDataCapture()
            .setDueDate(
                DataTransformation.transformToJsapsDate(
                    LOG, "due_date", inputRecord.getDataCapture().getDueDate()));
        getCaseRecord()
            .getDataCapture()
            .setCoronavirusDate(
                DataTransformation.transformToJsapsDate(
                    LOG, "coronavirus_date", inputRecord.getDataCapture().getCoronavirusDate()));

        if (getCaseRecord().getDataCapture().getConditionsList() == null) {
          getCaseRecord().getDataCapture().setConditionsList(new ArrayList());
        }

      } catch (ParseException e) {
        throw new ValidationException(e);
      }
    }

    public JsapsSubmissionRecordItem build() throws IOException, ParseException {
      ObjectMapper mapper = new ObjectMapper();

      JsapsSubmissionRecordItem buildItem =
          mapper.readValue(
              mapper.writeValueAsString(getCaseRecord()), JsapsSubmissionRecordItem.class);
      ObjectReader reader = mapper.readerForUpdating(buildItem);
      reader.withValueToUpdate(buildItem);

      reader.readValue(mapper.writeValueAsBytes(getCaseRecord().getDataCapture()));
      reader.readValue(mapper.writeValueAsBytes(getCaseRecord().getApplicant()));

      buildItem.setRef(getClaimRef());
      buildItem.setClaimantAddress(
          new AddressTransformItem(getCaseRecord().getApplicant().getResidenceAddress()));

      buildItem.setUseClaimantAddress(
          DataTransformation.boolToYN(
              getCaseRecord().getDataCapture().getCorrespondenceAddress() == null));

      if (!DataTransformation.toBoolYN(buildItem.getUseClaimantAddress())) {
        buildItem.setCorrespondanceAddress(
            new AddressTransformItem(getCaseRecord().getDataCapture().getCorrespondenceAddress()));
      } else {
        List<String> emptyAddressLines  =  new ArrayList<String>();
        emptyAddressLines.add(MessageConstants.BLANK);
        NonMandatoryAddress emptyAddress = new NonMandatoryAddress();
        emptyAddress.setLines(emptyAddressLines);
        emptyAddress.setPremises(MessageConstants.BLANK);
        emptyAddress.setPostCode(MessageConstants.BLANK);
        buildItem.setCorrespondanceAddress(new AddressTransformItem(emptyAddress));
      }

      buildItem.setVoluntaryWorkActivity(
          DataTransformation.boolToYN(getCaseRecord().getDataCapture().getVoluntaryWork() != null));
      buildItem.setVoluntaryWorks(new ArrayList<>());

      if (DataTransformation.toBoolYN(buildItem.getVoluntaryWorkActivity())) {
        for (VoluntaryWork item : getCaseRecord().getDataCapture().getVoluntaryWork()) {
          buildItem.getVoluntaryWorks().add(new VoluntaryWorkTransformItem(item));
        }
      }

      buildItem.setEmployedWork(
          DataTransformation.boolToYN(getCaseRecord().getDataCapture().getEmployments() != null));
      buildItem.setEmployedItems(new ArrayList<>());

      if (DataTransformation.toBoolYN(buildItem.getEmployedWork())) {
        for (Employments item : getCaseRecord().getDataCapture().getEmployments()) {
          buildItem.getEmployedItems().add(new EmployedWorkTransformItem(item));
        }
      }

      buildItem.setReceivingPension(
          DataTransformation.yesNoToYNWithDefaultYes(getCaseRecord()
             .getDataCapture().getPensionQuestion()));
      buildItem.setPensionItems(new ArrayList<>());

      if (getCaseRecord().getDataCapture().getPensions() != null) {
        for (Pensions item : getCaseRecord().getDataCapture().getPensions()) {
          buildItem.getPensionItems().add(new PensionProviderTransformItem(item));
        }
      }

      buildItem.setReceivingHealthInsurance(
          DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getInsuranceQuestion()));
      buildItem.setHealthItems(new ArrayList<>());

      if (getCaseRecord().getDataCapture().getInsurances() != null) {
        for (Insurances item : getCaseRecord().getDataCapture().getInsurances()) {
          buildItem.getHealthItems().add(new HealthProviderTransformItem(item));
        }
      }

      buildItem.setGpDetails(
          new MedicalDetailsTransformItem(getCaseRecord().getDataCapture().getMedicalCentre()));

      // contactNumber
      if (getCaseRecord().getApplicant().getContactOptionsList().isEmpty()) {
        buildItem.setContactNumber(MessageConstants.BLANK);
      } else {
        buildItem.setContactNumber(DataTransformation.transformToJsapsPhoneNumber(
            getCaseRecord().getApplicant().getContactOptionsList().stream()
            .filter(contact -> contact.isPreferred()).findFirst().get().getData()));
      }

      // sspReceived
      buildItem.setSspReceived(
          DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getSsp()));
      if (buildItem.getSspReceived() == MessageConstants.MARKER_NO) {
        buildItem.setSspReceived(
            DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getSspRecent()));
      }

      // sspEndData
      if (getCaseRecord().getDataCapture().getSspEnd() != null) {
        buildItem.setSspEndDate(getCaseRecord().getDataCapture().getSspEnd());
      } else if (getCaseRecord().getDataCapture().getSspRecentEnd() != null) {
        buildItem.setSspEndDate(getCaseRecord().getDataCapture().getSspRecentEnd());
      } else {
        buildItem.setSspEndDate(MessageConstants.BLANK);
      }

      // receivingExtra
      buildItem.setReceivingStatExtra(
          StatutoryExtraPaymentEnum.valueOf(getCaseRecord().getDataCapture().getStatutoryPayOther())
              .getJsapsValue());

      // fitForWorkDate
      if (getCaseRecord().getDataCapture().getBackToWorkDate() != null) {
        buildItem.setFitForWorkDate(getCaseRecord().getDataCapture().getBackToWorkDate());
      } else if (getCaseRecord().getDataCapture().getClaimEndDate() != null) {
        buildItem.setFitForWorkDate(getCaseRecord().getDataCapture().getClaimEndDate());
      } else {
        buildItem.setFitForWorkDate(MessageConstants.BLANK);
      }

      // currentlyInHospital
      buildItem.setCurrentlyInHospital(
          DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getHospitalInpatient()));

      // admissionDate
      if (getCaseRecord().getDataCapture().getHospitalAdmissionDate() != null) {
        buildItem.setAdmissionDate(getCaseRecord().getDataCapture().getHospitalAdmissionDate());
      } else {
        buildItem.setAdmissionDate(MessageConstants.BLANK);
      }

      // pregnant
      buildItem.setPregnant(
          DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getPregnant()));

      //baby_due_date
      if (getCaseRecord().getDataCapture().getDueDate() != null) {
        buildItem.setBabyDueDate(getCaseRecord().getDataCapture().getDueDate());
      } else {
        buildItem.setBabyDueDate(MessageConstants.BLANK);
      }

      // ds1500
      buildItem.setDs1500submitted(
          DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getDs1500Report()));

      //coronavirus
      if (MessageConstants.YES.equals(getCaseRecord().getDataCapture().getCoronavirus())
              || getCaseRecord().getDataCapture().getCoronavirusReason() != null) {
        buildItem.setCoronavirus(MessageConstants.MARKER_YES);
      } else {
        buildItem.setCoronavirus(MessageConstants.MARKER_NO);
      }

      //coronavirus_date
      if (getCaseRecord().getDataCapture().getCoronavirusDate() != null) {
        buildItem.setCoronavirusDate(getCaseRecord().getDataCapture().getCoronavirusDate());
      } else {
        buildItem.setCoronavirusDate(MessageConstants.BLANK);
      }

      //consent_dwp_share_with_doc
      buildItem.setConsentDwpShareWithDoc(
              DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getDwpShareWithDoc()));

      //consent_doc_share_with_dwp
      buildItem.setConsentDocShareWithDwp(
              DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getDocShareWithDWP()));

      //other_health_condition
      buildItem.setOtherHealthCondition(
          DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getOtherHealthCondition()));

      // special rules
      buildItem.setSpecialRuleApplication(
              DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getSevereCondition()));

      buildItem.setBankingDetails(new BankDetailsTransformItem(getCaseRecord().getDataCapture()));

      buildItem.setLanguageCompleted(getCaseRecord().getDataCapture().getLanguage());

      buildItem.setWelshPostcode(
          DataTransformation.yesNoToYN(getCaseRecord().getDataCapture().getWelshPostcode()));

      if (MessageConstants.YES.equalsIgnoreCase(
          getCaseRecord().getDataCapture().getWelshPostcode())) {
        buildItem.setWrittenComms(getCaseRecord().getDataCapture().getLangPrefWriting());
        buildItem.setVerbalComms(getCaseRecord().getDataCapture().getLangPrefSpeaking());
      } else {
        buildItem.setWrittenComms(MessageConstants.BLANK);
        buildItem.setVerbalComms(MessageConstants.BLANK);
      }


      return buildItem;
    }

    private RequestJson getCaseRecord() {
      return caseRecord;
    }

    private String getClaimRef() {
      return claimRef;
    }
  }
}
