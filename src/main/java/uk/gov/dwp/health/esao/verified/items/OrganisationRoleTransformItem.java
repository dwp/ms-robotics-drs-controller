package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.shared.models.Employments;
import uk.gov.dwp.health.esao.shared.models.VoluntaryWork;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;


@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationRoleTransformItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(OrganisationRoleTransformItem.class.getName());

  @NotNull
  @JsonProperty("org_name")
  private String orgName;

  @NotNull
  @JsonProperty("role")
  private String orgRole;

  @NotNull
  @JsonProperty("address")
  private AddressTransformItem orgAddress;

  @NotNull
  @JsonProperty("phone_number")
  private String phone;

  @SuppressWarnings("squid:S2637") // not null properties will be part of this object
  public OrganisationRoleTransformItem(VoluntaryWork workItem) {
    setOrgName(workItem.getOrganisationName());
    setOrgRole(workItem.getRole());

    setOrgAddress(new AddressTransformItem(workItem.getOrganisationAddress()));
    setPhone(MessageConstants.BLANK); // not provided
  }

  @SuppressWarnings("squid:S2637") // not null properties will be part of this object
  public OrganisationRoleTransformItem(Employments workItem) {
    setOrgName(workItem.getEmployerName());
    setOrgRole(workItem.getJobTitle());

    setOrgAddress(new AddressTransformItem(workItem.getEmployerAddress()));
    setPhone(DataTransformation.transformToJsapsPhoneNumber(workItem.getEmployerTelephone()));
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = getOrgAddress().isContentValid();
      ValidationUtils.logOutput(LOG, "organisation", isValid);
    }

    return isValid;
  }
}
