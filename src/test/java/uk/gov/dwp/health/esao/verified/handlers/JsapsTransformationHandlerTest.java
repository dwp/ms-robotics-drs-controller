package uk.gov.dwp.health.esao.verified.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.health.esao.shared.models.Applicant;
import uk.gov.dwp.health.esao.shared.models.Conditions;
import uk.gov.dwp.health.esao.shared.models.ContactOptions;
import uk.gov.dwp.health.esao.shared.models.DataCapture;
import uk.gov.dwp.health.esao.shared.models.DeductionDetails;
import uk.gov.dwp.health.esao.shared.models.Employments;
import uk.gov.dwp.health.esao.shared.models.MandatoryAddress;
import uk.gov.dwp.health.esao.shared.models.MedicalCentre;
import uk.gov.dwp.health.esao.shared.models.NonMandatoryAddress;
import uk.gov.dwp.health.esao.shared.models.RequestJson;
import uk.gov.dwp.health.esao.shared.models.VoluntaryWork;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings( "deprecation" )
public class JsapsTransformationHandlerTest {

  private RequestJson requestJson;
  private List<Employments> employmentsList = new ArrayList<>();
  private List<VoluntaryWork> voluntaryWorksList = new ArrayList<>();
  private List<DeductionDetails> deductionDetailsList = new ArrayList<>();

  private MedicalCentre medicalCentre;
  private MandatoryAddress mandatoryAddress;
  private NonMandatoryAddress nonMandatoryAddress;
  private List<Conditions> conditionsList = new ArrayList<>();
  private Applicant applicant;
  private List<String> tags = new ArrayList<>();
  private DataCapture dataCapture;
  private ClaimReferenceItem claimRefClass;
  private VoluntaryWork voluntaryWork;
  private Employments employments;

  @Before
  public void setup() throws IOException {

    claimRefClass = new ObjectMapper().readValue("{\"claim_ref\":\"hello123\"}", ClaimReferenceItem.class);

    List<String> lines = new ArrayList<>();
    lines.add("abc");
    lines.add("xyz");

    ContactOptions contactOptions;
    Conditions conditions;

    DeductionDetails deductionDetails;

    List<String> employmentStatus = new ArrayList<>();
    employmentStatus.add("employee");
    employmentStatus.add("selfEmployed");

    deductionDetails = new DeductionDetails("1", "stuff");
    deductionDetailsList.add(deductionDetails);

    mandatoryAddress = new MandatoryAddress();
    mandatoryAddress.setLines(lines);
    mandatoryAddress.setPremises("premises");
    mandatoryAddress.setPostCode("NE7 7ND");

    nonMandatoryAddress = new NonMandatoryAddress();
    nonMandatoryAddress.setLines(null);
    nonMandatoryAddress.setPremises(null);
    nonMandatoryAddress.setPostCode(null);

    voluntaryWork = new VoluntaryWork("ABC-org", mandatoryAddress, "IT", "yes", "0.5");

    employments = new Employments("SE", "john", "075656565", mandatoryAddress, employmentStatus, "yes", "2019-01-01",
        "yes", "2", "daily", "100", "yes", "", "detail");

    contactOptions = new ContactOptions("contactId", "telmobile", "07666", true);

    medicalCentre = new MedicalCentre("NHS NCL", "067787787", mandatoryAddress, "Dr ABC");

    List<ContactOptions> contactOptionsList = new ArrayList<>();
    contactOptionsList.add(contactOptions);

    conditions = new Conditions("fever", "2019-01-01");

    conditionsList.add(conditions);


    dataCapture = new DataCapture("en", conditionsList, medicalCentre, "yes", "yes", "yes", "yes", "maternity", "AA370773A", "John", "Llyods", "010101",
        null, "12312312", "2019-01-01", "yes", "yes", "2019-02-02", "yes", "2019-02-02", "yes", "2019-02-02", "yes", "no",
        "yes", "hosp_name", "hosp-ward", "2019-03-03", "yes", voluntaryWorksList, "yes", employmentsList, mandatoryAddress,
        "no", null, "2019-10-02", "yes", "2019-10-02", "yes", null, "yes", "no",  null, "yes", null, "yes", "no", "username@domain.com", "yes", "high-risk", "high risk description", "yes","2019-02-02", "yes", null, null, "yes", "English", "Welsh");



    tags.add("_67678688");
    applicant = new Applicant(mandatoryAddress, "FName", "SName", "1981-01-01", contactOptionsList);
    requestJson = new RequestJson("msgId", applicant, "ref", "2019-04-11T10:02:07.098Z", dataCapture, "declaration", tags, "08988");

  }

  @Test
  public void validRequestJsonWithExpectedResponse() throws ParseException, IOException {
    String expectedResponse = "{\"submitted_date\":\"11/04/19\",\"claim_ref\":\"hello123\",\"nino\":\"AA370773A\",\"first_name\":\"FName\",\"surname\":\"SName\",\"date_of_birth\":\"01/01/81\",\"contact_number\":\"07666\",\"claimant_address\":" +
                                  "{\"address_line_1\":\"abc\",\"address_line_2\":\"xyz\",\"town\":\"\",\"county\":\"premises\",\"postcode\":\"NE7 7ND\"},\"use_claimant_address\":\"N\",\"correspondence_address\":{\"address_line_1\":\"abc\"," +
                                  "\"address_line_2\":\"xyz\",\"town\":\"\",\"county\":\"premises\",\"postcode\":\"NE7 7ND\"},\"language_completed\":\"en\",\"welsh_postcode\":\"Y\",\"written_comms\":\"English\",\"verbal_comms\":\"Welsh\",\"claim_start_date\":\"01/01/19\",\"ssp_received\":\"Y\",\"ssp_end_date\":\"02/10/19\",\"receiving_statutory_extra\":\"Statutory Maternity Pay\"," +
                                  "\"expected_fit_for_work_date\":\"02/02/19\",\"voluntary_work_activity\":\"Y\",\"voluntary_work_assignments\":[],\"employed_work\":\"Y\",\"employed_work_details\":[],\"receiving_pension\":\"Y\",\"pension_details\":[]," +
                                  "\"receiving_permanent_health_insurance\":\"Y\",\"health_insurance_details\":[],\"currently_in_hospital\":\"Y\",\"admission_date\":\"03/03/19\",\"pregnant\":\"Y\",\"baby_due_date\":\"02/02/19\",\"special_rule_application\":\"Y\"," +
                                  "\"ds1500_already_submitted\":\"N\",\"coronavirus\":\"Y\",\"consent_dwp_share_with_doc\":\"Y\",\"consent_doc_share_with_dwp\":\"Y\",\"coronavirus_date\":\"02/02/19\",\"other_health_condition\":\"Y\",\"medical_conditions\":[{\"condition\":\"fever\",\"condition_start_date\":\"01/01/19\"}]," +
                                  "\"gp_surgery_details\":{\"doctor_title\":\"Dr.\",\"doctor_forename\":\"Dr ABC\",\"doctor_surname\":\"\",\"surgery_name\":\"NHS NCL\",\"surgery_address\":{\"address_line_1\":\"abc\",\"address_line_2\":\"xyz\",\"town\":\"\"," +
                                  "\"county\":\"premises\",\"postcode\":\"NE7 7ND\"},\"phone_number\":\"067787787\"},\"banking_details\":{\"name\":\"Llyods\",\"account_holder_name\":\"John\",\"sort_code\":\"01-01-01\",\"account_number\":\"12312312\"}}";
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, is(equalTo(expectedResponse)));
  }

  @Test(expected = ValidationException.class)
  public void invalidRequestJsonWithValidationError() throws ParseException, IOException {
    requestJson.getDataCapture().setCoronavirusDate("01-01-0101");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
  }

  @Test
  public void validRequestJsonWithNoCorrespondenceAddress() throws ParseException, IOException {
    requestJson.getDataCapture().setCorrespondenceAddress(null);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"correspondence_address\":{\"address_line_1\":\"\",\"address_line_2\":\"\",\"town\":\"\",\"county\":\"\",\"postcode\":\"\"}"));
  }

  @Test
  public void validRequestJsonWithNoConditions() throws ParseException, IOException {
    requestJson.getDataCapture().setConditionsList(null);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"medical_conditions\":[]"));
  }

  @Test
  public void validRequestJsonWithVoluntaryWorkHours() throws ParseException, IOException {
    voluntaryWorksList.add(voluntaryWork);
    requestJson.getDataCapture().setVoluntaryWork(voluntaryWorksList);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"weekly_hours\":0"));
  }

  @Test
  public void validRequestJsonWithEmployedWorkHours() throws ParseException, IOException {
    employmentsList.add(employments);
    requestJson.getDataCapture().setEmployments(employmentsList);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"weekly_hours\":2"));
    assertThat(response, CoreMatchers.containsString("\"payment_amount\":100.0"));
  }

  @Test
  public void validRequestJsonWithCoronavirusReason() throws ParseException, IOException {
    requestJson.getDataCapture().setCoronavirus(null);
    requestJson.getDataCapture().setCoronavirusReason("high-risk");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"coronavirus\":\"Y\""));
  }

  @Test
  public void validRequestJsonWithNoCoronavirusReason() throws ParseException, IOException {
    requestJson.getDataCapture().setCoronavirus("yes");
    requestJson.getDataCapture().setCoronavirusReason(null);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"coronavirus\":\"Y\""));
  }

  @Test
  public void validRequestJsonWithNoCoronavirus() throws ParseException, IOException {
    requestJson.getDataCapture().setCoronavirus(null);
    requestJson.getDataCapture().setCoronavirusReason(null);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"coronavirus\":\"N\""));
  }

  @Test
  public void validRequestJsonWithNoBackToWorKDateAndWithClaimEndDate() throws ParseException, IOException {
    requestJson.getDataCapture().setBackToWorkDate(null);
    requestJson.getDataCapture().setClaimEndDate("2020-01-01");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"expected_fit_for_work_date\":\"01/01/20\""));
  }

  @Test
  public void validRequestJsonWithNoBackToWorKDateAndWithNoClaimEndDate() throws ParseException, IOException {
    requestJson.getDataCapture().setBackToWorkDate(null);
    requestJson.getDataCapture().setClaimEndDate(null);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"expected_fit_for_work_date\":\"\""));
  }

  @Test
  public void validRequestJsonWithBackToWorKDateAndWithClaimEndDate() throws ParseException, IOException {
    requestJson.getDataCapture().setBackToWorkDate("2020-01-01");
    requestJson.getDataCapture().setClaimEndDate(null);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"expected_fit_for_work_date\":\"01/01/20\""));
  }

  @Test
  public void validRequestJsonWithPensionAsNotsure() throws ParseException, IOException {
    requestJson.getDataCapture().setPensionQuestion("notsure");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"receiving_pension\":\"Y\""));
  }

  @Test
  public void validRequestJsonWithPensionAsNo() throws ParseException, IOException {
    requestJson.getDataCapture().setPensionQuestion("no");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"receiving_pension\":\"N\""));
  }

  @Test
  public void validRequestJsonWithPensionAsYes() throws ParseException, IOException {
    requestJson.getDataCapture().setPensionQuestion("yes");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"receiving_pension\":\"Y\""));
  }

  @Test(expected = ValidationException.class)
  public void testInvalidSubmissionRecordItem() throws ParseException, IOException {
    applicant.setDateOfBirth("2030-01-01");
    requestJson.setApplicant(applicant);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
  }

  @Test
  public void validRequestJsonWithEnLang() throws ParseException, IOException {
    requestJson.getDataCapture().setLanguage("en");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"language_completed\":\"en\""));
  }

  @Test
  public void validRequestJsonWithWelshPostcode() throws ParseException, IOException {
    requestJson.getDataCapture().setWelshPostcode("yes");
    requestJson.getDataCapture().setLangPrefSpeaking("English");
    requestJson.getDataCapture().setLangPrefWriting("Welsh");
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"welsh_postcode\":\"Y\""));
    assertThat(response, CoreMatchers.containsString("\"verbal_comms\":\"English\""));
    assertThat(response, CoreMatchers.containsString("\"written_comms\":\"Welsh\""));
  }

  @Test
  public void validRequestJsonWithNoWelshPostcode() throws ParseException, IOException {
    requestJson.getDataCapture().setWelshPostcode("no");
    requestJson.getDataCapture().setLangPrefSpeaking(null);
    requestJson.getDataCapture().setLangPrefWriting(null);
    JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    String response = jsapsTransformationHandler.transformForJsaps(claimRefClass, requestJson);
    assertThat(response, CoreMatchers.containsString("\"welsh_postcode\":\"N\""));
    assertThat(response, CoreMatchers.containsString("\"written_comms\":\"\""));
    assertThat(response, CoreMatchers.containsString("\"verbal_comms\":\"\""));
  }
}
