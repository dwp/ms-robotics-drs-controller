package uk.gov.dwp.health.esao.verified.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataTransformation {

  private DataTransformation() {
  }

  public static String transformToJsapsPhoneNumber(String phoneNumber) {
    if (phoneNumber != null) {
      return phoneNumber.trim().replace("+44", "0")
                 .replaceAll(" ", MessageConstants.BLANK)
                 .replaceAll("\\(", MessageConstants.BLANK)
                 .replaceAll("\\)", MessageConstants.BLANK)
                 .replaceAll("-", MessageConstants.BLANK);
    } else {
      return MessageConstants.BLANK;
    }
  }

  public static String transformToJsapsDate(Logger log, String fieldName, String currentValue)
      throws ParseException {
    String transformedDate = null;

    if (currentValue != null) {
      Date currentDate;
      try {
        currentDate = getJsapsDateFormat().parse(currentValue);
        log.debug("'{}' is already in jsaps format", fieldName);

      } catch (ParseException e) {

        try {
          currentDate = getDateFormat().parse(currentValue);
          log.debug("'{}' is in original database format", fieldName);

        } catch (ParseException e1) {
          throw new ParseException(
              String.format("'%s' could not be parsed : %s", fieldName, e1.getMessage()),
              e1.getErrorOffset());
        }
      }

      transformedDate = DateFormatUtils.format(currentDate, "dd/MM/yy");
    }

    return transformedDate;
  }

  private static SimpleDateFormat getJsapsDateFormat() {
    SimpleDateFormat dtFormat = new SimpleDateFormat("dd/MM/yy");
    dtFormat.setLenient(false);
    return dtFormat;
  }

  private static SimpleDateFormat getDateFormat() {
    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
    dtFormat.setLenient(false);
    return dtFormat;
  }

  public static String boolToYN(boolean state) {
    return state ? MessageConstants.MARKER_YES : MessageConstants.MARKER_NO;
  }

  public static boolean toBoolYN(String state) {
    return state.equals(MessageConstants.MARKER_YES);
  }

  public static String yesNoToYN(String state) {
    return DataTransformation
               .boolToYN(state != null && state.equalsIgnoreCase(MessageConstants.YES));
  }

  public static String yesNoToYNWithDefaultYes(String state) {
    return DataTransformation
               .boolToYN(state != null && !state.equalsIgnoreCase(MessageConstants.NO));
  }
}
