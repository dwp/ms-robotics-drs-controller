package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.shared.models.VoluntaryWork;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VoluntaryWorkTransformItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(VoluntaryWorkTransformItem.class.getName());

  @NotNull
  @JsonProperty("organisation")
  private OrganisationRoleTransformItem organisation;

  @NotNull
  @JsonProperty("same_hours_every_week")
  private String sameHours;

  @NotNull
  @JsonProperty("weekly_hours")
  private int weeklyHours;

  @SuppressWarnings("squid:S2637") // not null properties will be part of this object
  public VoluntaryWorkTransformItem(VoluntaryWork voluntaryWork) {
    setOrganisation(new OrganisationRoleTransformItem(voluntaryWork));

    if (voluntaryWork.getSameHours() != null && !voluntaryWork.getSameHours().isEmpty()) {
      setSameHours(DataTransformation.yesNoToYN(voluntaryWork.getSameHours()));
    } else {
      setSameHours(MessageConstants.BLANK);
    }

    if (voluntaryWork.getHours() != null && NumberUtils.isParsable(voluntaryWork.getHours())) {
      setWeeklyHours(Double.valueOf(voluntaryWork.getHours()).intValue());
    } else {
      setWeeklyHours(-1);
    }
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = getOrganisation().isContentValid();
    }

    if (isValid) {
      isValid =
          !getSameHours().isEmpty() && ValidationUtils.isValidYN(LOG, "same_hours", getSameHours());
    }

    if (isValid && getWeeklyHours() != -1) {
      isValid = getWeeklyHours() >= 0 && getWeeklyHours() < 100;
    }

    return isValid;
  }
}
