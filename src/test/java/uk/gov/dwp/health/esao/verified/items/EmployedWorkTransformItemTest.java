package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.Employments;
import uk.gov.dwp.health.esao.shared.models.MandatoryAddress;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmployedWorkTransformItemTest {
  private static final String SUCCESS_JSON =
      "{\n"
          + "    \"job_title\": \"Dynamic Identity Technician\",\n"
          + "    \"employer_name\": \"McLaughlin Inc\",\n"
          + "    \"employer_tel\": \"(503) 833-2059\",\n"
          + "    \"employer_address\": {\n"
          + "        \"lines\": [\n"
          + "            \"998 Beulah Way\",\n"
          + "            \"\",\n"
          + "            \"Dariusland\"\n"
          + "        ],\n"
          + "        \"premises\": \"\",\n"
          + "        \"postcode\": \"LS1 1DJ\"\n"
          + "    },\n"
          + "    \"employment_status\": [\n"
          + "        \"employee\",\n"
          + "        \"subContractor\"\n"
          + "    ],\n"
          + "    \"off_sick\": \"no\",\n"
          + "    \"same_hours\": \"yes\",\n"
          + "    \"hours\": \"12\",\n"
          + "    \"frequency\": \"every4Weeks\",\n"
          + "    \"net_pay\": \"1234\",\n"
          + "    \"support\": \"yes\",\n"
          + "    \"expenses_details\": \"Dolores vero magni dolorem sunt et molestiae excepturi. Nam blanditiis iure. Omnis magnam delectus voluptas eum suscipit sed hic voluptatem aliquid.\"\n"
          + "}";

  @Test
  public void testSuccessfulTransformation() throws IOException, ParseException {
    EmployedWorkTransformItem instance =
        new EmployedWorkTransformItem(
            new ObjectMapper().readValue(SUCCESS_JSON, Employments.class));
    assertTrue(instance.isContentValid());

    assertThat(instance.getSupportWorkerIndicator(), is(equalTo(2)));
    assertThat(instance.getEmploymentStatus(), is(equalTo("Employee")));
  }

  @Test
  public void testFailureWithBlankOrganisation() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setEmployerAddress(new MandatoryAddress());

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void testSuccessWithSupportWorker() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setSupport("no");
    item.setNetPay("100");

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());

    assertThat(instance.getSupportWorkerIndicator(), is(equalTo(3)));
  }

  @Test
  public void testSuccessWithNullSupportWorker() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setSupport(null);

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());

    assertThat(instance.getSupportWorkerIndicator(), is(equalTo(-1)));
  }

  @Test
  public void testSuccessWithDecimalNetPay() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setNetPay("10.5");

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getPaymentAmount().doubleValue(), is(equalTo(10.50)));
  }

  @Test
  public void testSuccessWithDecimalHours() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setHours("10.5");

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getWeeklyHours(), is(equalTo(10)));
  }

  @Test
  public void testSuccessWithNullHours() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setHours(null);

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getWeeklyHours(), is(equalTo(0)));
  }

  @Test
  public void testSuccessWithZeroNetPay() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setNetPay("0");

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getPaymentAmount().doubleValue(), is(equalTo(0.00)));
  }

  @Test
  public void testSuccessWithNullNetPay() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setNetPay(null);

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getPaymentAmount().doubleValue(), is(equalTo(0.00)));
  }

  @Test
  public void testInvalidWithNullEmpName() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setEmployerName(null);

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void testSuccessWithSameHoursAsNull() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setSameHours(null);

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getSameHours(), is(equalTo("")));
  }

  @Test
  public void testSuccessWithFrequencyAsNull() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setFrequency(null);

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getPaymentSchedule(), is(equalTo(-1)));
  }

  @Test
  public void testSuccessWithSupportWorkerFlagAs2() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setSupport("no");
    item.setHours("17");
    item.setNetPay("130");

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
    assertThat(instance.getSupportWorkerIndicator(), is(equalTo(2)));
  }

  @Test
  public void testSuccessWithSameHoursAsEmpty() throws IOException, ParseException {
    Employments item = new ObjectMapper().readValue(SUCCESS_JSON, Employments.class);
    item.setSameHours("");

    EmployedWorkTransformItem instance = new EmployedWorkTransformItem(item);
    assertTrue(instance.isContentValid());
  }
}
