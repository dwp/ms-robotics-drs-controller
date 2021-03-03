package uk.gov.dwp.health.esao.verified.items;

import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.MandatoryAddress;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AddressTransformItemTest {

  @Test
  public void nullIsInvalid() {
    assertFalse(new AddressTransformItem(new MandatoryAddress()).isContentValid());
  }

  @Test
  public void testInvalidWithEmptyLines() {
    AddressTransformItem instance = new AddressTransformItem(new MandatoryAddress());
    instance.setPostcode("LS1 4RG");

    assertFalse(instance.isContentValid());
  }

  @Test
  public void testValidWithMissingTownCity() {
    MandatoryAddress address = new MandatoryAddress();
    address.setLines(Arrays.asList("line1", "line2"));
    address.setPostCode("LS6 4PT");

    AddressTransformItem instance = new AddressTransformItem(address);
    assertTrue(instance.isContentValid());
  }

  @Test
  public void testValidWithAllItems() {
    MandatoryAddress address = new MandatoryAddress();
    address.setLines(Arrays.asList("line1", "line2", "line3", "line4", "line5"));
    address.setPostCode("LS6 4PT");

    AddressTransformItem instance = new AddressTransformItem(address);
    assertTrue(instance.isContentValid());
  }

  @Test
  public void testInvalidWithBadPostcode() {
    MandatoryAddress address = new MandatoryAddress();
    address.setLines(Arrays.asList("line1", "line2"));
    address.setPostCode("LS6");

    AddressTransformItem instance = new AddressTransformItem(address);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void testInvalidWithMissingPostcode() {
    MandatoryAddress address = new MandatoryAddress();
    address.setLines(Arrays.asList("line1", "line2"));

    AddressTransformItem instance = new AddressTransformItem(address);
    assertFalse(instance.isContentValid());
  }
}
