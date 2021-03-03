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
import uk.gov.dwp.health.esao.shared.models.Employments;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.esao.verified.utils.EmployeeStatusEnum;
import uk.gov.dwp.health.esao.verified.utils.PaymentFrequencyInputEnum;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.ParseException;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployedWorkTransformItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(EmployedWorkTransformItem.class.getName());

  @NotNull
  @JsonProperty("employment_status")
  private String employmentStatus;

  @NotNull
  @JsonProperty("organisation")
  private OrganisationRoleTransformItem organisation;

  @JsonProperty("same_hours_every_week")
  private String sameHours;

  @JsonProperty("weekly_hours")
  private int weeklyHours;

  @JsonProperty("currently_off_sick")
  private String offSick;

  @JsonProperty("last_working_day")
  private String lastWorkingDay;

  @NotNull
  @JsonProperty("payment_amount")
  private BigDecimal paymentAmount;

  @NotNull
  @JsonProperty("payment_schedule")
  private int paymentSchedule;

  @JsonProperty("paid_expenses")
  private String paidExpense;

  @NotNull
  @JsonProperty("support_worker_indicator")
  private int supportWorkerIndicator;

  public EmployedWorkTransformItem(Employments item) throws ParseException {
    String empStatus =
        item.getEmploymentStatus() != null && !item.getEmploymentStatus().isEmpty()
            ? item.getEmploymentStatus().get(0)
            : MessageConstants.NONE;

    this.employmentStatus = EmployeeStatusEnum.valueOf(empStatus).getJsapsValue();
    this.organisation = new OrganisationRoleTransformItem(item);

    if (item.getSameHours() != null) {
      this.sameHours = DataTransformation.yesNoToYN(item.getSameHours());
    } else {
      this.sameHours = MessageConstants.BLANK;
    }

    if (item.getHours() != null && NumberUtils.isParsable(item.getHours())) {
      this.weeklyHours = Double.valueOf(item.getHours()).intValue();
    }  else {
      this.weeklyHours = 0;
    }

    if (item.getOffSick() != null) {
      this.offSick = DataTransformation.yesNoToYN(item.getOffSick());
    } else {
      this.offSick = MessageConstants.BLANK;
    }

    if (item.getLastWorkedDate() != null) {
      this.lastWorkingDay =
          DataTransformation.transformToJsapsDate(LOG, "last_working_day",
              item.getLastWorkedDate());
    } else {
      this.lastWorkingDay = MessageConstants.BLANK;
    }

    if (item.getNetPay() != null && NumberUtils.isParsable(item.getNetPay())) {
      this.paymentAmount = BigDecimal.valueOf(Double.valueOf(item.getNetPay())).setScale(2);
    } else {
      this.paymentAmount = BigDecimal.valueOf(0).setScale(2);
    }

    if (item.getFrequency() != null) {
      this.paymentSchedule = PaymentFrequencyInputEnum.valueOf(item.getFrequency()).getJsapsValue();
    } else {
      this.paymentSchedule = -1;
    }

    this.paidExpense = DataTransformation.boolToYN(item.getExpensesDetails() != null);
    this.supportWorkerIndicator = decodeAndSetSupportWorkerFlag(item);
  }

  @JsonIgnore
  private int decodeAndSetSupportWorkerFlag(Employments item) {
    int supportFlag = -1;

    if (item.getSupport() != null && NumberUtils.isParsable(item.getNetPay())
                                  && NumberUtils.isParsable(item.getHours())) {
      supportFlag = 2;

      // NO & <16 hours and £125 limit = 3, otherwise 2
      if (item.getSupport().equalsIgnoreCase("no")
          && Double.valueOf(item.getHours()) < 16
          && Double.valueOf(item.getNetPay()) < 125) {
        LOG.debug(
            "change support flag to '3' with support '{}', "
                + "hours worked ({}) < 16 and net pay ({}) < 125",
            item.getSupport(),
            item.getHours(),
            item.getNetPay());
        supportFlag = 3;
      }

      this.supportWorkerIndicator = supportFlag;
    }

    return supportFlag;
  }

  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = getOrganisation().isContentValid();
      ValidationUtils.logOutput(LOG, "organisation", isValid);
    }

    if (isValid && getSupportWorkerIndicator() != -1) {
      isValid = getSupportWorkerIndicator() >= 2 && getSupportWorkerIndicator() <= 3;
      ValidationUtils.logOutput(LOG, "support_worker_indicator", isValid);
    }

    if (isValid && getSameHours() != null && !getSameHours().isEmpty()) {
      isValid =
              ValidationUtils.isValidYN(LOG, "same_hours_every_week", getSameHours());
    }

    if (isValid) {
      isValid =
          !getOffSick().isEmpty()
              && ValidationUtils.isValidYN(LOG, "currently_off_sick", getOffSick());
    }

    if (isValid) {
      isValid =
          !getPaidExpense().isEmpty()
              && ValidationUtils.isValidYN(LOG, "paid_expenses", getPaidExpense());
    }

    return isValid;
  }
}
