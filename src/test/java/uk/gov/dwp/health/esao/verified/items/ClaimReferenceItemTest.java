package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ClaimReferenceItemTest {
  private static final Logger LOG = LoggerFactory.getLogger(ClaimReferenceItemTest.class.getName());
  private Validator validator = Validation.byDefaultProvider()
          .configure()
          .messageInterpolator(new ParameterMessageInterpolator())
          .buildValidatorFactory().getValidator();

  @Test
  public void nullItemShouldError() throws IOException {
    ClaimReferenceItem item =
        new ObjectMapper().readValue("{\"claim_ref\":null}", ClaimReferenceItem.class);
    testViolations(validator.validate(item), 1);
    assertFalse(item.isContentValid());
  }

  @Test
  public void emptyItemShouldReject() throws IOException {
    ClaimReferenceItem item =
        new ObjectMapper().readValue("{\"claim_ref\":\"\"}", ClaimReferenceItem.class);
    testViolations(validator.validate(item), 0);
    assertFalse(item.isContentValid());
  }

  @Test
  public void standardItemSerialisesOk() throws IOException {
    String claimRef = "xxxCLAIMxxx";

    ClaimReferenceItem item =
        new ObjectMapper()
            .readValue(String.format("{\"claim_ref\":\"%s\"}", claimRef), ClaimReferenceItem.class);
    testViolations(validator.validate(item), 0);
    assertTrue(item.isContentValid());

    assertThat(item.getClaimRef(), is(equalTo(claimRef)));
  }

  private void testViolations(
          Set<ConstraintViolation<ClaimReferenceItem>> violations, int expectedViolations) {
    for (ConstraintViolation<ClaimReferenceItem> violation : violations) {
      LOG.error(violation.getMessage());
    }

    assertThat(violations.size(), is(equalTo(expectedViolations)));
  }
}
