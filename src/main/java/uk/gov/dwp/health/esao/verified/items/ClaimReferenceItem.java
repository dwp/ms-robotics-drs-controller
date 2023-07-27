package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimReferenceItem implements BaseItem {
  private static final Logger LOG = LoggerFactory.getLogger(ClaimReferenceItem.class.getName());

  @NotNull
  @JsonProperty("claim_ref")
  private String claimRef;

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    return ValidationUtils.validateAnnotations(LOG, this) && !getClaimRef().isEmpty();
  }
}
