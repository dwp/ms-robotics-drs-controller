package uk.gov.dwp.health.esao.verified.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ValidationUtilsTest {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationUtilsTest.class.getName());

  @Test
  public void testDateTransform() throws ParseException {
    String caseDate = "2019-05-17";
    String jsapsDate = "17/05/19";

    assertEquals(
        "date should be transformed",
        DataTransformation.transformToJsapsDate(LOG, "transform", caseDate),
        jsapsDate);
  }

  @Test
  public void testDateTransformDoesNothing() throws ParseException {
    String jsapsDate = "17/05/19";

    assertEquals(
        "date should be transformed",
        DataTransformation.transformToJsapsDate(LOG, "nothing", jsapsDate),
        jsapsDate);
  }

  @Test(expected = ParseException.class)
  public void testDateTransformFails() throws ParseException {
    DataTransformation.transformToJsapsDate(LOG, "fail", "17/25/1999");
  }

  @Test
  public void nullDoesNothing() throws ParseException {
    assertNull(DataTransformation.transformToJsapsDate(LOG, "null", null));
  }
}
