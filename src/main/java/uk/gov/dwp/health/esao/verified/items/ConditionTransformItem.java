package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;

import javax.validation.constraints.NotNull;
import java.text.ParseException;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionTransformItem implements BaseItem {
  private static final Logger LOG = LoggerFactory.getLogger(ConditionTransformItem.class.getName());

  @NotNull
  @JsonAlias("name")
  @JsonProperty("condition")
  private String condition;

  @NotNull
  @JsonAlias("start_date")
  @JsonProperty("condition_start_date")
  private String conditionStartDate;

  public void setConditionStartDate(String startDate) {
    try {
      this.conditionStartDate =
          DataTransformation.transformToJsapsDate(LOG, "condition_start_date", startDate);

    } catch (ParseException e) {
      this.conditionStartDate = null;
    }
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = !getCondition().isEmpty();
    }

    if (isValid) {
      isValid =
          !getConditionStartDate().isEmpty()
              && ValidationUtils.isValidDate(LOG, "condition_start_date", getConditionStartDate());
    }

    return isValid;
  }
}
