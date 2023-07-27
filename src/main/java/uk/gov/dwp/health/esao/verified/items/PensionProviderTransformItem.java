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
import uk.gov.dwp.health.esao.shared.models.Pensions;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PensionProviderTransformItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(PensionProviderTransformItem.class.getName());

  @NotNull
  @JsonProperty("pension")
  private ProviderTransformItem pension;

  @NotNull
  @JsonProperty("inherited")
  private String inherited;

  public PensionProviderTransformItem(Pensions item) throws IOException {
    this.inherited =
        DataTransformation.boolToYN(item.getInherited().equalsIgnoreCase(MessageConstants.YES));
    this.pension = new ProviderTransformItem(item);
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = getPension().isContentValid();
      ValidationUtils.logOutput(LOG, "pensions", isValid);
    }

    if (isValid) {
      isValid = ValidationUtils.isValidYN(LOG, "inherited", getInherited());
    }

    return isValid;
  }
}
