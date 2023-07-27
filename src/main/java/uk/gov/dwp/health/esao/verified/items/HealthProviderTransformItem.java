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
import uk.gov.dwp.health.esao.shared.models.Insurances;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthProviderTransformItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(HealthProviderTransformItem.class.getName());

  @NotNull
  @JsonProperty("insurer")
  private ProviderTransformItem insurer;

  @JsonProperty("self_funded_premium_exceeds_50")
  private String exceedsFifty;

  public HealthProviderTransformItem(Insurances item) throws IOException {
    this.insurer = new ProviderTransformItem(item);
    this.exceedsFifty = null;
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = getInsurer().isContentValid();
      ValidationUtils.logOutput(LOG, "insurer", isValid);
    }

    return isValid;
  }
}
