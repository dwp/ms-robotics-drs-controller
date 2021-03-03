package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.Insurances;
import uk.gov.dwp.health.esao.verified.utils.PaymentFrequencyInputEnum;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HealthProviderTransformItemTest {
  private static final String GOOD_JSON =
      "      {\n"
          + "        \"insurance_provider\": \"Boyle - Lesch\",\n"
          + "        \"provider_ref\": \"1-419-980-0031\",\n"
          + "        \"provider_tel\": \"(678) 701-0072\",\n"
          + "        \"provider_address\": {\n"
          + "          \"lines\": [\n"
          + "            \"9117 Ellis Locks\",\n"
          + "            \"\",\n"
          + "            \"North Hillardchester\"\n"
          + "          ],\n"
          + "          \"premises\": \"\",\n"
          + "          \"postcode\": \"LS1 1DJ\"\n"
          + "        },\n"
          + "        \"amount\": \"7861\",\n"
          + "        \"frequency\": \"quarterly\",\n"
          + "        \"premiums\": \"no\",\n"
          + "        \"employment_end_date\": \"2019-01-30\"\n"
          + "      }";

  @Test
  public void testSuccessfulData() throws IOException {
    HealthProviderTransformItem instance =
        new HealthProviderTransformItem(new ObjectMapper().readValue(GOOD_JSON, Insurances.class));
    assertThat(
        instance.getInsurer().getPaymentSchedule(),
        is(equalTo(PaymentFrequencyInputEnum.valueOf("quarterly").getJsapsValue())));
    assertThat(
        instance.getExceedsFifty(),
        is(equalTo(null)));
    assertTrue(instance.isContentValid());
  }

  @Test
  public void testInvalidData() throws IOException {
    HealthProviderTransformItem instance =
        new HealthProviderTransformItem(new ObjectMapper().readValue(GOOD_JSON, Insurances.class));
    instance.setInsurer(null);
    assertFalse(instance.isContentValid());
  }
}
