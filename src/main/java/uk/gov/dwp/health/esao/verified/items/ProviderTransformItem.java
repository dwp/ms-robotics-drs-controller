package uk.gov.dwp.health.esao.verified.items;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.shared.models.Insurances;
import uk.gov.dwp.health.esao.shared.models.Pensions;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.PaymentFrequencyInputEnum;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderTransformItem implements BaseItem {
  private static final Logger LOG = LoggerFactory.getLogger(ProviderTransformItem.class.getName());

  @NotNull
  @JsonAlias("provider_ref")
  @JsonProperty("reference")
  private String reference;

  @NotNull
  @JsonAlias({"pension_provider", "insurance_provider"})
  @JsonProperty("provider")
  private String provider;

  @NotNull
  @JsonAlias("provider_address")
  @JsonProperty("address")
  private AddressTransformItem address;

  @NotNull
  @JsonAlias("provider_tel")
  @JsonProperty("phone_number")
  private String phoneNumber;

  @JsonAlias("start_date")
  @JsonProperty("first_payment_date")
  private String firstPaymentDate;

  @NotNull
  @JsonAlias("amount")
  @JsonProperty("amount_gross")
  private int grossAmount;

  @NotNull
  @JsonProperty("amount_net")
  private int netAmount;

  @NotNull
  @JsonProperty("payment_schedule")
  private int paymentSchedule;

  public ProviderTransformItem(Pensions item) throws IOException {
    ProviderTransformItem copyItem =
        new ObjectMapper()
            .readValue(new ObjectMapper().writeValueAsString(item), ProviderTransformItem.class);
    this.paymentSchedule = PaymentFrequencyInputEnum.valueOf(item.getFrequency()).getJsapsValue();
    this.address = new AddressTransformItem(item.getProviderAddress());
    this.firstPaymentDate = copyItem.getFirstPaymentDate();
    this.phoneNumber = copyItem.getPhoneNumber();
    this.grossAmount = copyItem.getGrossAmount();
    this.reference = copyItem.getReference();
    this.netAmount = copyItem.getNetAmount();
    this.provider = copyItem.getProvider();

    if (item.getDeductions().equalsIgnoreCase("no") && this.netAmount <= 0) {
      LOG.info("no deductions and no net amount, setting net amount = gross amount");
      this.netAmount = copyItem.getGrossAmount();
    }
  }

  public ProviderTransformItem(Insurances item) throws IOException {
    ProviderTransformItem copyItem =
        new ObjectMapper()
            .readValue(new ObjectMapper().writeValueAsString(item), ProviderTransformItem.class);
    this.paymentSchedule = PaymentFrequencyInputEnum.valueOf(item.getFrequency()).getJsapsValue();
    this.address = new AddressTransformItem(item.getProviderAddress());
    this.firstPaymentDate = copyItem.getFirstPaymentDate();
    this.phoneNumber = copyItem.getPhoneNumber();
    this.grossAmount = copyItem.getGrossAmount();
    this.reference = copyItem.getReference();
    this.netAmount = copyItem.getNetAmount();
    this.provider = copyItem.getProvider();

    if (this.netAmount <= 0) {
      LOG.info("no net amount, setting net amount = gross amount");
      this.netAmount = copyItem.getGrossAmount();
    }
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = getAddress().isContentValid();
      ValidationUtils.logOutput(LOG, "address", isValid);
    }

    if (isValid) {
      isValid = getGrossAmount() > 0;
      ValidationUtils.logOutput(LOG, "amount_gross", isValid);
    }

    if (isValid) {
      isValid = getNetAmount() > 0;
      ValidationUtils.logOutput(LOG, "amount_net", isValid);
    }

    if (isValid) {
      isValid = getGrossAmount() >= getNetAmount();
      ValidationUtils.logOutput(LOG, "amount_gross >= amount_net", isValid);
    }

    return isValid;
  }
}
