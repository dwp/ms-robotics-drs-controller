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
import uk.gov.dwp.health.esao.shared.models.DataCapture;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;


@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankDetailsTransformItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(BankDetailsTransformItem.class.getName());

  @NotNull
  @JsonProperty("name")
  private String name;

  @NotNull
  @JsonProperty("account_holder_name")
  private String holder;

  @NotNull
  @JsonProperty("sort_code")
  private String sortCode;

  @NotNull
  @JsonProperty("account_number")
  private String accountNumber;

  public BankDetailsTransformItem(DataCapture dataCaptureItem) {
    this.accountNumber = dataCaptureItem.getBankAccountNumber();
    this.holder = dataCaptureItem.getBankAccountName();
    this.sortCode = convertToSortCodeFormat(dataCaptureItem.getBankSortCode());
    this.name = dataCaptureItem.getBankName();
  }

  @JsonIgnore
  private String convertToSortCodeFormat(String sortCode) {
    if (sortCode != null) {
      char sortCodeSeparator = '-';
      StringBuffer sbSortCode = new StringBuffer(sortCode);
      sbSortCode.insert(4, sortCodeSeparator).insert(2, sortCodeSeparator);
      return sbSortCode.toString();
    } else {
      return null;
    }
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = sortCode != null && sortCode.length() == 8;
      ValidationUtils.logOutput(LOG, "sort_code", isValid);
    }

    if (isValid) {
      isValid = accountNumber != null && accountNumber.length() == 8;
      ValidationUtils.logOutput(LOG, "account_number", isValid);
    }

    return isValid;
  }
}
