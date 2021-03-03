package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConditionTransformItemTest {
  private static final String INPUT_JSON =
      "{\"name\":\"back-end application\",\"start_date\":\"2003-02-01\"}";
  ConditionTransformItem instance;

  @Before
  public void setup() throws IOException {
    instance = new ObjectMapper().readValue(INPUT_JSON, ConditionTransformItem.class);
  }

  @Test
  public void nullIsInvalid() {
    assertFalse(new ConditionTransformItem().isContentValid());
  }

  @Test
  public void missingConditionIsInvalid() {
    instance.setCondition("");
    assertFalse("condition must be present", instance.isContentValid());
  }

  @Test
  public void missingStartDateIsInvalid() throws ParseException {
    instance.setConditionStartDate("");
    assertFalse("start_date must be present", instance.isContentValid());
  }

  @Test
  public void fullyPopulatedReturnsTrue() throws IOException {
    ConditionTransformItem instance =
        new ObjectMapper().readValue(INPUT_JSON, ConditionTransformItem.class);
    assertTrue(instance.isContentValid());
  }
}
