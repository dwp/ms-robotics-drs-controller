package uk.gov.dwp.health.esao.verified.items;

import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.DataCapture;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BankDetailsTransformItemTest {

  private static final String INVALID_INPUT_JSON =
                                     "      {"
                                   + "        \"name\": \"Lexus Maggio\","
                                   + "        \"account_holder_name\": \"Wunsch, Stokes and Kulas\","
                                   + "        \"sort_code\": \"01193\","
                                   + "        \"account_number\": \"12345678\""
                                   + "      }";

  BankDetailsTransformItem instance;

  @Before
  public void setup() throws IOException {
    instance = new ObjectMapper().readValue(INVALID_INPUT_JSON, BankDetailsTransformItem.class);
  }

  @Test
  public void testValidInput() {
    assertFalse(instance.isContentValid());
  }

  @Test
  public void nullIsInvalid() {
    assertFalse(new BankDetailsTransformItem(new DataCapture()).isContentValid());
  }

  @Test
  public void testMissingItemIsInvalid() {
    DataCapture item = new DataCapture();
    item.setBankAccountName("John Smith");
    item.setBankSortCode("010101");
    item.setBankName("LLoyds");

    BankDetailsTransformItem instance = new BankDetailsTransformItem(item);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void testMissingSortCodeIsInvalid() {
    DataCapture item = new DataCapture();
    item.setBankAccountName("John Smith");
    item.setBankName("LLoyds");

    BankDetailsTransformItem instance = new BankDetailsTransformItem(item);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void testMissingBankAccountIsInvalid() {
    DataCapture item = new DataCapture();
    item.setBankAccountName("John Smith");
    item.setBankName("LLoyds");
    item.setBankSortCode("01010101");

    BankDetailsTransformItem instance = new BankDetailsTransformItem(item);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void testSuccess() {
    DataCapture item = new DataCapture();
    item.setBankAccountName("John Smith");
    item.setBankAccountNumber("12345667");
    item.setBankSortCode("010101");
    item.setBankName("LLoyds");

    BankDetailsTransformItem instance = new BankDetailsTransformItem(item);
    assertTrue(instance.isContentValid());
  }

}
