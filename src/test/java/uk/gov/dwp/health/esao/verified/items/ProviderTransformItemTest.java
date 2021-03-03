package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.Insurances;
import uk.gov.dwp.health.esao.shared.models.Pensions;
import uk.gov.dwp.health.esao.verified.utils.PaymentFrequencyInputEnum;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ProviderTransformItemTest {
  private static final String PENSION_TEST_JSON =
      "      {\n"
          + "        \"pension_provider\": \"Balistreri - Schneider\",\n"
          + "        \"provider_ref\": \"1-211-247-3552\",\n"
          + "        \"provider_tel\": \"(663) 846-1722\",\n"
          + "        \"provider_address\": {\n"
          + "          \"lines\": [\n"
          + "            \"66636 Jakob Haven\",\n"
          + "            \"\",\n"
          + "            \"Framiburgh\"\n"
          + "          ],\n"
          + "          \"premises\": \"\",\n"
          + "          \"postcode\": \"LS1 1DJ\"\n"
          + "        },\n"
          + "        \"start_date\": \"2018-03-27\",\n"
          + "        \"deductions\": \"no\",\n"
          + "        \"amount_gross\": \"1423\",\n"
          + "        \"frequency\": \"every4Weeks\",\n"
          + "        \"inherited\": \"yes\"\n"
          + "      }";

  private static final String PENSION_TEST_JSON_MISSING_ELEMENT =
      "      {\n"
          + "        \"pension_provider\": \"Balistreri - Schneider\",\n"
          + "        \"provider_tel\": \"(663) 846-1722\",\n"
          + "        \"provider_address\": {\n"
          + "          \"lines\": [\n"
          + "            \"66636 Jakob Haven\",\n"
          + "            \"\",\n"
          + "            \"Framiburgh\"\n"
          + "          ],\n"
          + "          \"premises\": \"\",\n"
          + "          \"postcode\": \"LS1 1DJ\"\n"
          + "        },\n"
          + "        \"start_date\": \"2018-03-27\",\n"
          + "        \"deductions\": \"no\",\n"
          + "        \"amount_gross\": \"0\",\n"
          + "        \"frequency\": \"every4Weeks\",\n"
          + "        \"inherited\": \"yes\"\n"
          + "      }";

  private static final String INSURANCE_TEST_JSON =
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
  public void successWithValidInsurance() throws IOException {
    ProviderTransformItem instance =
        new ProviderTransformItem(
            new ObjectMapper().readValue(INSURANCE_TEST_JSON, Insurances.class));
    assertThat(
        instance.getPaymentSchedule(),
        is(equalTo(PaymentFrequencyInputEnum.valueOf("quarterly").getJsapsValue())));
    assertTrue(instance.isContentValid());
  }

  @Test
  public void successWithValidPension() throws IOException {
    ProviderTransformItem instance =
        new ProviderTransformItem(new ObjectMapper().readValue(PENSION_TEST_JSON, Pensions.class));
    assertThat(
        instance.getPaymentSchedule(),
        is(equalTo(PaymentFrequencyInputEnum.valueOf("every4Weeks").getJsapsValue())));
    assertTrue(instance.isContentValid());
  }

  @Test
  public void invalidWithZeroAmount() throws IOException {
    ProviderTransformItem instance =
        new ProviderTransformItem(new ObjectMapper().readValue(PENSION_TEST_JSON, Pensions.class));
    instance.setGrossAmount(0);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void invalidWithInValidPension() throws IOException {
    ProviderTransformItem instance =
        new ProviderTransformItem(new ObjectMapper().readValue(PENSION_TEST_JSON, Pensions.class));
    instance.setProvider(null);
    assertFalse(instance.isContentValid());
  }

}
