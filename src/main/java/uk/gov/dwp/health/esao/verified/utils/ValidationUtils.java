package uk.gov.dwp.health.esao.verified.utils;

import org.slf4j.Logger;
import uk.gov.dwp.health.esao.shared.util.StatutoryExtraPaymentEnum;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ValidationUtils {

  private ValidationUtils() {
    // prevent instantiation
  }

  public static <T> boolean validateAnnotations(Logger log, T clazz) {
    Set<ConstraintViolation<T>> validations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(clazz);
    for (ConstraintViolation<T> item : validations) {
      log.debug("{}.{} {}", clazz.getClass().getName(), item.getPropertyPath(), item.getMessage());
    }

    ValidationUtils.logOutput(log, clazz.getClass().getName(), validations.isEmpty());
    return validations.isEmpty();
  }

  public static boolean isValidDate(Logger log, String fieldName, String startDate) {
    boolean isValidDate = true;
    try {
      getJsapsDateFormat().parse(startDate);

    } catch (ParseException e) {
      log.debug("{} is invalid :: {}", fieldName, e.getMessage());
      isValidDate = false;
    }

    ValidationUtils.logOutput(log, fieldName, isValidDate);
    return isValidDate;
  }

  public static boolean validDateNotInTheFuture(Logger log, String fieldName, String dob) {
    boolean dateValid;

    try {
      dateValid = new Date().after(getJsapsDateFormat().parse(dob));
      if (!dateValid) {
        log.debug("the date cannot be in the future for {}", fieldName);
      }

    } catch (ParseException e) {
      log.debug("{} is invalid :: {}", fieldName, e.getMessage());
      dateValid = false;
    }

    ValidationUtils.logOutput(log, fieldName, dateValid);
    return dateValid;
  }

  public static boolean validateStatutoryExtras(Logger log, String fieldName, String fieldValue) {
    boolean contentsValid = false;

    for (StatutoryExtraPaymentEnum item : StatutoryExtraPaymentEnum.values()) {
      contentsValid = item.getJsapsValue().equals(fieldValue);
      if (contentsValid) {
        break;
      }
    }

    ValidationUtils.logOutput(log, fieldName, contentsValid);
    return contentsValid;
  }

  public static boolean isValidYN(Logger log, String item, String value) {
    boolean isValid =
        value.equals(MessageConstants.MARKER_YES) || value.equals(MessageConstants.MARKER_NO);
    ValidationUtils.logOutput(log, item, isValid);
    return isValid;
  }

  public static void logOutput(Logger log, String item, boolean state) {
    if (state) {
      log.debug("checked item '{}' with validated status {}", item, state);
    } else {
      log.warn("checked item '{}' with validated status {}", item, state);
    }
  }

  private static SimpleDateFormat getJsapsDateFormat() {
    SimpleDateFormat dtFormat = new SimpleDateFormat("dd/MM/yy");
    dtFormat.setLenient(false);
    return dtFormat;
  }

}
