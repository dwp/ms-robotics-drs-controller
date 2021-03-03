package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.Pensions;
import uk.gov.dwp.health.esao.verified.utils.PaymentFrequencyInputEnum;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PensionProviderTransformItemTest {
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


  @Test
  public void successWithValidPension() throws IOException {
    PensionProviderTransformItem instance =
        new PensionProviderTransformItem(new ObjectMapper().readValue(PENSION_TEST_JSON, Pensions.class));
    assertThat(
        instance.getPension().getPaymentSchedule(),
        is(equalTo(PaymentFrequencyInputEnum.valueOf("every4Weeks").getJsapsValue())));
    assertThat(
        instance.getInherited(),
        is(equalTo("Y")));
    assertTrue(instance.isContentValid());
  }

  @Test
  public void invalidWithInvalidPension() throws IOException {
    PensionProviderTransformItem instance =
        new PensionProviderTransformItem(new ObjectMapper().readValue(PENSION_TEST_JSON, Pensions.class));
    instance.setInherited(null);
    assertFalse(instance.isContentValid());
  }

}
