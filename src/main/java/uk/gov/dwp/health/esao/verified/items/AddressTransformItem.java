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
import uk.gov.dwp.health.esao.shared.models.Address;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;
import uk.gov.dwp.regex.PostCodeValidator;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressTransformItem implements BaseItem {
  private static final Logger LOG = LoggerFactory.getLogger(AddressTransformItem.class.getName());

  @NotNull
  @JsonProperty("address_line_1")
  private String addressLineOne;

  @JsonProperty("address_line_2")
  private String addressLineTwo;

  @JsonProperty("town")
  private String town;

  @JsonProperty("county")
  private String county;

  @NotNull
  @JsonProperty("postcode")
  private String postcode;

  public AddressTransformItem(Address addressItem) {
    List<String> addressLines = addressItem.getLines();

    this.addressLineOne =
        addressLines != null && !addressLines.isEmpty()
            ? addressLines.get(0) : MessageConstants.BLANK;
    this.addressLineTwo =
        addressLines != null && addressLines.size() > 1
            ? addressLines.get(1) : MessageConstants.BLANK;
    this.town =
        addressLines != null && addressLines.size() > 2
            ? addressLines.get(2) : MessageConstants.BLANK;
    this.county = addressItem.getPremises();
    this.postcode = addressItem.getPostCode();
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = !getAddressLineOne().isEmpty();
      ValidationUtils.logOutput(LOG, "address_line_1", isValid);
    }

    if (isValid) {
      isValid = PostCodeValidator.validateInput(getPostcode());
      ValidationUtils.logOutput(LOG, "postcode", isValid);
    }

    return isValid;
  }
}
