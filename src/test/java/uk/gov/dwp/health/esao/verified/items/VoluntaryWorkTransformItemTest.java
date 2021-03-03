package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.VoluntaryWork;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VoluntaryWorkTransformItemTest {
  private VoluntaryWork inputModel;

  @Before
  public void setup() throws IOException {
    inputModel =
        new ObjectMapper()
            .readValue(
                "{\"organisation_name\":\"Prosacco Group\",\"organisation_address\":{\"lines\":[\"3398 Trantow Plains\",\"\",\"\"],\"premises\":\"\",\"postcode\":\"LS1 1DJ\"},\"role\":\"boss\",\"same_hours\":\"yes\",\"hours\":\"1\"}",
                VoluntaryWork.class);
  }

  @Test
  public void nullIsInvalid() {
    assertFalse(new VoluntaryWorkTransformItem().isContentValid());
  }

  @Test
  public void successfulTest() {
    VoluntaryWorkTransformItem instance = new VoluntaryWorkTransformItem(inputModel);
    assertTrue(instance.isContentValid());
  }

  @Test
  public void invalidOrganisationFails() {
    inputModel.getOrganisationAddress().setPostCode(null);
    VoluntaryWorkTransformItem instance = new VoluntaryWorkTransformItem(inputModel);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void nullSameHoursFails() {
    inputModel.setSameHours(null);
    VoluntaryWorkTransformItem instance = new VoluntaryWorkTransformItem(inputModel);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void blankSameHoursFails() {
    inputModel.setSameHours("");
    VoluntaryWorkTransformItem instance = new VoluntaryWorkTransformItem(inputModel);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void successNullWeeklyHours() {
    inputModel.setHours(null);
    VoluntaryWorkTransformItem instance = new VoluntaryWorkTransformItem(inputModel);
    assertTrue(instance.isContentValid());
  }

  @Test
  public void successBlankWeeklyHours() {
    inputModel.setHours("");
    VoluntaryWorkTransformItem instance = new VoluntaryWorkTransformItem(inputModel);
    assertTrue(instance.isContentValid());
  }

}
