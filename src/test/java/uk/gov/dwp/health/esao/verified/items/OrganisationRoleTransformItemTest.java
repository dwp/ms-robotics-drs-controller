package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.Employments;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrganisationRoleTransformItemTest {
  private static final String EMPLOYMENTS_SUCCESS_JSON =
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
          + "    \"hours\": \"12\",\n"
          + "    \"frequency\": \"every4Weeks\",\n"
          + "    \"net_pay\": \"1234\",\n"
          + "    \"support\": \"yes\",\n"
          + "    \"expenses_details\": \"Dolores vero magni dolorem sunt et molestiae excepturi. Nam blanditiis iure. Omnis magnam delectus voluptas eum suscipit sed hic voluptatem aliquid.\"\n"
          + "}";

  private ObjectMapper mapper = new ObjectMapper();
  private Employments employmentsItem;

  @Before
  public void init() throws IOException {
    employmentsItem = mapper.readValue(EMPLOYMENTS_SUCCESS_JSON, Employments.class);
  }

  @Test
  public void successfulLoadAndVerificationEmployments() {
    OrganisationRoleTransformItem instance = new OrganisationRoleTransformItem(employmentsItem);
    assertTrue(instance.isContentValid());
  }

  @Test
  public void invalidVerificationWithMissingNotNullItem() {
    employmentsItem.setEmployerName(null);

    OrganisationRoleTransformItem instance = new OrganisationRoleTransformItem(employmentsItem);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void invalidVerificationWithMissingNestedItem() {
    employmentsItem.getEmployerAddress().setPostCode(null);

    OrganisationRoleTransformItem instance = new OrganisationRoleTransformItem(employmentsItem);
    assertFalse(instance.isContentValid());
  }

  @Test
  public void invalidVerificationWithMissingAddressLineItem() {
    employmentsItem.getEmployerAddress().setLines(Collections.emptyList());

    OrganisationRoleTransformItem instance = new OrganisationRoleTransformItem(employmentsItem);
    assertFalse(instance.isContentValid());
  }

  // todo Voluntary work transformation tests
}
