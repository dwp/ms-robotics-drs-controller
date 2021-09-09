package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.shared.util.StatutoryExtraPaymentEnum;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JsapsSubmissionRecordItemTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(JsapsSubmissionRecordItemTest.class.getName());
  private static final String CLAIM_REF = "Claim123";
  private RequestJson fullCaseItem;

  @Before
  public void init() throws IOException {
    String content = FileUtils.readFileToString(new File("src/test/resources/full-application-json.json"));
    fullCaseItem =
        new ObjectMapper()
            .readValue(
                String.format(content, "\"AA370773A\""),
                RequestJson.class);
  }

  @Test
  public void testFullCaseIsLoadedClassInput() throws IOException, ParseException {
    JsapsSubmissionRecordItem instance =
        new JsapsSubmissionRecordItem.JsapsSubmissionItemBuilder()
            .withRequestJsonClass(fullCaseItem)
            .withClaimRef(CLAIM_REF)
            .build();

    assertTrue("JsapsSubmissionRecordItem content should be valid", instance.isContentValid());
    compareObjects(fullCaseItem, instance);
  }

  @Test
  public void testFullCaseIsLoaded() throws IOException, ParseException {
    JsapsSubmissionRecordItem instance =
        new JsapsSubmissionRecordItem.JsapsSubmissionItemBuilder()
            .withRequestJson(new ObjectMapper().writeValueAsString(fullCaseItem))
            .withClaimRef(CLAIM_REF)
            .build();

    assertTrue("JsapsSubmissionRecordItem content should be valid", instance.isContentValid());
    compareObjects(fullCaseItem, instance);
  }

  @Test
  public void testSSPEndDateTransformation() throws IOException, ParseException {
    fullCaseItem.getDataCapture().setSspEnd("yes");
    fullCaseItem.getDataCapture().setSspEnd("2019-08-02");
    fullCaseItem.getDataCapture().setSspRecent(null);
    fullCaseItem.getDataCapture().setSspRecentEnd(null);
    JsapsSubmissionRecordItem instance =
        new JsapsSubmissionRecordItem.JsapsSubmissionItemBuilder()
            .withRequestJson(new ObjectMapper().writeValueAsString(fullCaseItem))
            .withClaimRef(CLAIM_REF)
            .build();

    assertTrue("JsapsSubmissionRecordItem content should be valid", instance.isContentValid());
    assertThat(
        "bad ssp_received",
        instance.getSspReceived(),
        is(equalTo(DataTransformation.yesNoToYN(fullCaseItem.getDataCapture().getSsp()))));
    assertThat(
        "bad ssp_end_date",
        instance.getSspEndDate(),
        is(
            equalTo(
                DataTransformation.transformToJsapsDate(
                    LOG, "ssp_end_date", fullCaseItem.getDataCapture().getSspEnd()))));
  }

  @Test
  public void testRecentSSPEndDateTransformation() throws IOException, ParseException {
    fullCaseItem.getDataCapture().setSspEnd("no");
    fullCaseItem.getDataCapture().setSspEnd(null);
    fullCaseItem.getDataCapture().setSspRecent("yes");
    fullCaseItem.getDataCapture().setSspRecentEnd("2019-08-02");
    JsapsSubmissionRecordItem instance =
        new JsapsSubmissionRecordItem.JsapsSubmissionItemBuilder()
            .withRequestJson(new ObjectMapper().writeValueAsString(fullCaseItem))
            .withClaimRef(CLAIM_REF)
            .build();

    assertTrue("JsapsSubmissionRecordItem content should be valid", instance.isContentValid());
    assertThat(
        "bad ssp_received",
        instance.getSspReceived(),
        is(equalTo(DataTransformation.yesNoToYN(fullCaseItem.getDataCapture().getSspRecent()))));
    assertThat(
        "bad ssp_end_date",
        instance.getSspEndDate(),
        is(
            equalTo(
                DataTransformation.transformToJsapsDate(
                    LOG, "ssp_end_date", fullCaseItem.getDataCapture().getSspRecentEnd()))));
  }

  @Test
  public void testNoSSPEndDateTransformation() throws IOException, ParseException {
    fullCaseItem.getDataCapture().setSsp("no");
    fullCaseItem.getDataCapture().setSspEnd(null);
    fullCaseItem.getDataCapture().setSspRecent("no");
    fullCaseItem.getDataCapture().setSspRecentEnd(null);
    JsapsSubmissionRecordItem instance =
        new JsapsSubmissionRecordItem.JsapsSubmissionItemBuilder()
            .withRequestJson(new ObjectMapper().writeValueAsString(fullCaseItem))
            .withClaimRef(CLAIM_REF)
            .build();

    assertTrue("JsapsSubmissionRecordItem content should be valid", instance.isContentValid());
    assertThat(
        "bad ssp_received",
        instance.getSspReceived(),
        is(equalTo(DataTransformation.yesNoToYN(fullCaseItem.getDataCapture().getSspRecent()))));
    assertThat(
        "bad ssp_end_date",
        instance.getSspEndDate(),
        is(
            equalTo("")));
  }

  private void compareObjects(RequestJson initialObject, JsapsSubmissionRecordItem builtObject)
      throws ParseException {
    assertNotNull(initialObject);
    assertNotNull(builtObject);

    // mandatory items
    assertThat(
        "bad submitted_date",
        builtObject.getSubmittedDate(),
        is(
            equalTo(
                DataTransformation.transformToJsapsDate(
                    LOG, "submitted_date", initialObject.getSubmissionDate()))));
    assertThat("bad claim_ref", builtObject.getRef(), is(equalTo(CLAIM_REF)));
    assertThat(
        "bad nino", builtObject.getNino(), is(equalTo(initialObject.getDataCapture().getNino())));
    assertThat(
        "bad first name",
        builtObject.getFirstName(),
        is(equalTo(initialObject.getApplicant().getFirstName())));
    assertThat(
        "bad surname",
        builtObject.getSurname(),
        is(equalTo(initialObject.getApplicant().getSurName())));
    assertThat(
        "bad date_of_birth",
        builtObject.getDob(),
        is(
            equalTo(
                DataTransformation.transformToJsapsDate(
                    LOG, "dob", initialObject.getApplicant().getDateOfBirth()))));
    assertThat(
        "bad contact_number",
        builtObject.getContactNumber(),
        is(
            equalTo(DataTransformation.transformToJsapsPhoneNumber(
                initialObject.getApplicant().getContactOptionsList().stream()
                .filter(contact -> contact.isPreferred()).findFirst().get().getData()))));
    assertThat(
        "bad use_claimant_address",
        builtObject.getUseClaimantAddress(),
        is(equalTo(initialObject.getDataCapture().getCorrespondenceAddress() != null ? "N" : "Y")));
    assertThat(
        "bad claim_start_date",
        builtObject.getClaimStartDate(),
        is(
            equalTo(
                DataTransformation.transformToJsapsDate(
                    LOG, "claim_start_date", initialObject.getDataCapture().getClaimStartDate()))));
    assertThat(
        "bad ssp_received",
        builtObject.getSspReceived(),
        is(equalTo(DataTransformation.yesNoToYN(initialObject.getDataCapture().getSsp()))));
    assertThat(
        "bad ssp_end_date",
        builtObject.getSspEndDate(),
        is(
            equalTo(
                DataTransformation.transformToJsapsDate(
                    LOG, "ssp_end_date", initialObject.getDataCapture().getSspEnd()))));
    assertThat(
        "bad receiving_statutory_extra",
        builtObject.getReceivingStatExtra(),
        is(
            equalTo(
                StatutoryExtraPaymentEnum.valueOf(
                        initialObject.getDataCapture().getStatutoryPayOther())
                    .getJsapsValue())));
    assertThat(
        "bad expected_fit_for_work_date",
        builtObject.getFitForWorkDate(),
        is(
            equalTo(
                DataTransformation.transformToJsapsDate(
                    LOG,
                    "expected_fit_for_work_date",
                    initialObject.getDataCapture().getBackToWorkDate()))));

    assertThat(
        "bad voluntary_work_activity",
        builtObject.getVoluntaryWorkActivity(),
        is(
            equalTo(
                DataTransformation.yesNoToYN(
                    initialObject.getDataCapture().getVoluntaryWorkQuestion()))));
    if (DataTransformation.toBoolYN(builtObject.getVoluntaryWorkActivity())) {
      assertThat(
          "bad voluntary_work_assignments",
          builtObject.getVoluntaryWorks().size(),
          is(equalTo(initialObject.getDataCapture().getVoluntaryWork().size())));
          validationVoluntaryWork(initialObject, builtObject);
    } else {
      assertThat(
          "bad voluntary_work_assignments", builtObject.getVoluntaryWorks().size(), is(equalTo(0)));
    }

    assertThat(
        "bad employed_work",
        builtObject.getEmployedWork(),
        is(
            equalTo(
                DataTransformation.yesNoToYN(
                    initialObject.getDataCapture().getEmploymentQuestion()))));

    if (DataTransformation.toBoolYN(builtObject.getEmployedWork())) {
      validationEmployedWork(initialObject, builtObject);
    } else {
      assertThat(
          "bad employed_work_assignments", builtObject.getEmployedItems().size(), is(equalTo(0)));
    }

    assertThat(
        "bad receiving_pension",
        builtObject.getReceivingPension(),
        is(
            equalTo(
                DataTransformation.yesNoToYN(initialObject.getDataCapture().getPensionQuestion()))));
    if (DataTransformation.toBoolYN(builtObject.getReceivingPension())) {
      assertThat(
          "bad pension_details",
          builtObject.getPensionItems().size(),
          is(equalTo(initialObject.getDataCapture().getPensions().size())));

    } else {
      assertThat("bad pension_details", builtObject.getPensionItems().size(), is(equalTo(0)));
    }

    assertThat(
        "bad receiving_permanent_health_insurance",
        builtObject.getReceivingHealthInsurance(),
        is(
            equalTo(
                DataTransformation.yesNoToYN(initialObject.getDataCapture().getInsuranceQuestion()))));
    if (DataTransformation.toBoolYN(builtObject.getReceivingHealthInsurance())) {
      assertThat(
          "bad health_insurance_details",
          builtObject.getHealthItems().size(),
          is(equalTo(initialObject.getDataCapture().getInsurances().size())));

    } else {
      assertThat(
          "bad health_insurance_details", builtObject.getPensionItems().size(), is(equalTo(0)));
    }

    assertThat(
        "bad currently_in_hospital",
        builtObject.getCurrentlyInHospital(),
        is(
            equalTo(
                DataTransformation.yesNoToYN(initialObject.getDataCapture().getHospitalInpatient()))));
    assertThat(
        "bad pregnant value",
        builtObject.getPregnant(),
        is(equalTo(DataTransformation.yesNoToYN(initialObject.getDataCapture().getPregnant()))));
    assertThat("bad special_rule", builtObject.getSpecialRuleApplication(),
        is(equalTo(DataTransformation.yesNoToYN(initialObject.getDataCapture().getSevereCondition()))));

    assertThat("bad consent_dwp_share_with_doc", builtObject.getConsentDwpShareWithDoc(),
            is(equalTo(DataTransformation.yesNoToYN(initialObject.getDataCapture().getDwpShareWithDoc()))));

    assertThat("bad consent_doc_share_with_dwp", builtObject.getConsentDocShareWithDwp(),
            is(equalTo(DataTransformation.yesNoToYN(initialObject.getDataCapture().getDocShareWithDWP()))));

    assertThat(
        "bad ds1500_already_submitted",
        builtObject.getDs1500submitted(),
        is(equalTo(initialObject.getDataCapture().getDs1500Report() != null ? "Y" : "N")));

    assertNotNull("bad medical_conditions", builtObject.getConditionItems());
    assertThat(
        "bad medical_conditions",
        builtObject.getConditionItems().size(),
        is(equalTo(initialObject.getDataCapture().getConditionsList().size())));

    assertThat("bad gp_surgery_details", builtObject.getGpDetails().getDoctorTitle()
        + " " + builtObject.getGpDetails().getDoctorForename(),
        is(equalTo(initialObject.getDataCapture().getMedicalCentre().getDoctorName())));

    assertThat("bad banking_details", builtObject.getBankingDetails().getName(),
        is(equalTo(initialObject.getDataCapture().getBankName())));
    assertThat("bad banking_details", builtObject.getBankingDetails().getAccountNumber(),
        is(equalTo(initialObject.getDataCapture().getBankAccountNumber())));
    assertThat("bad banking_details", builtObject.getBankingDetails().getSortCode(),
        is(equalTo(convertToSortCodeFormat(initialObject.getDataCapture().getBankSortCode()))));

  }

  private String convertToSortCodeFormat(String sortCode) {
    char sortCodeSeparator = '-';
    StringBuffer sbSortCode = new StringBuffer(sortCode);
    sbSortCode.insert(4, sortCodeSeparator).insert(2, sortCodeSeparator);
    return sbSortCode.toString();
  }

  private void validationVoluntaryWork(
      RequestJson initialObject, JsapsSubmissionRecordItem builtObject) {
    assertThat(
        "bad voluntary_work_assignments",
        builtObject.getVoluntaryWorks().size(),
        is(equalTo(initialObject.getDataCapture().getVoluntaryWork().size())));

    for (int index = 0; index < builtObject.getVoluntaryWorks().size(); index++) {
      assertThat(
          builtObject.getVoluntaryWorks().get(index).getOrganisation().getOrgName(),
          is(
              equalTo(
                  initialObject
                      .getDataCapture()
                      .getVoluntaryWork()
                      .get(index)
                      .getOrganisationName())));
      assertThat(
          builtObject.getVoluntaryWorks().get(index).getOrganisation().getOrgRole(),
          is(equalTo(initialObject.getDataCapture().getVoluntaryWork().get(index).getRole())));
    }
  }

  private void validationEmployedWork(
      RequestJson initialObject, JsapsSubmissionRecordItem builtObject) {
    assertThat(
        "bad employed_work_assignments",
        builtObject.getEmployedItems().size(),
        is(equalTo(initialObject.getDataCapture().getEmployments().size())));

    for (int index = 0; index < builtObject.getEmployedItems().size(); index++) {
      assertThat(
          builtObject.getEmployedItems().get(index).getOrganisation().getOrgName(),
          is(
              equalTo(
                  initialObject
                      .getDataCapture()
                      .getEmployments()
                      .get(index)
                      .getEmployerName())));
      assertThat(
          builtObject.getEmployedItems().get(index).getOrganisation().getOrgRole(),
          is(equalTo(initialObject.getDataCapture().getEmployments().get(index).getJobTitle())));
    }
  }
}
