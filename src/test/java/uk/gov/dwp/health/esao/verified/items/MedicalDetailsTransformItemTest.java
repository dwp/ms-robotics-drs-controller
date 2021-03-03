package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.MedicalCentre;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MedicalDetailsTransformItemTest {
  private static final String MEDICAL_JSON =
      "{\n"
          + "      \"name\": \"Braun, Collins and Hirthe\",\n"
          + "      \"tel\": \"(925) 648-1131\",\n"
          + "      \"address\": {\n"
          + "        \"lines\": [\n"
          + "          \"34809 O'Hara Dam\",\n"
          + "          \"\",\n"
          + "          \"Smithfurt\"\n"
          + "        ],\n"
          + "        \"premises\": \"\",\n"
          + "        \"postcode\": \"LS1 1DJ\"\n"
          + "      },\n"
          + "      \"doctor\": \"Dr. Tianna Lynch\"\n"
          + "    }";

  private MedicalCentre medicalCentre;

  @Before
  public void init() throws IOException {
    medicalCentre = new ObjectMapper().readValue(MEDICAL_JSON, MedicalCentre.class);
  }

  @Test
  public void successfulTransformationAndValidation() {
    MedicalDetailsTransformItem instance = new MedicalDetailsTransformItem(medicalCentre);
    assertTrue(instance.isContentValid());
  }

  @Test
  public void successfulTransformationAndValidationWithNestedDoctor() {
    String doctorName = "Chris Doctorson";

    MedicalDetailsTransformItem instance = new MedicalDetailsTransformItem(medicalCentre);
    instance.setAndTransformDoctorField(String.format("Dr. %s", doctorName));

    assertTrue(instance.isContentValid());
    assertThat(instance.getDoctorForename(), is(equalTo(doctorName)));
  }

  @Test
  public void failureWithMissingDoctorElements() {
    MedicalDetailsTransformItem instance = new MedicalDetailsTransformItem(medicalCentre);
    instance.setDoctorForename(null);

    assertFalse(instance.isContentValid());
  }

  @Test
  public void failureWithBadAddressElement() {
    MedicalDetailsTransformItem instance = new MedicalDetailsTransformItem(medicalCentre);
    instance.getSurgeryAddress().setPostcode(null);

    assertFalse(instance.isContentValid());
  }

  @Test
  public void failureWithBadPostcodeElement() {
    MedicalDetailsTransformItem instance = new MedicalDetailsTransformItem(medicalCentre);
    instance.getSurgeryAddress().setPostcode("LS2");

    assertFalse(instance.isContentValid());
  }
}
