package uk.gov.dwp.health.esao.verified.constants;

public class MessageConstants {

  private MessageConstants() {
  }

  public static final String BLANK = "";
  public static final String NONE = "none";
  public static final String YES = "yes";
  public static final String NO = "no";
  public static final String ERROR_MSG_500 = "'Unable to process request\' for any "
                                             + "internal errors";
  public static final String NOT_VERIFIED = "The json ClaimRef %s does not exist or is not "
                                             + "available for robotics";
  public static final String VERIFIED_MARKER = "verified";
  public static final String DEFAULT_PREFIX = ":{";
  public static final String ERROR_MSG_400 = "'Payload contains invalid items' as the body if "
                                             + "one of the input items is invalid or badly formed";

  public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final String STATUS_SENT = "sent";
  public static final String STATUS_DELIVERED = "delivered";
  public static final String STATUS_TRUE = "true";
  public static final String MARKER_YES = "Y";
  public static final String MARKER_NO = "N";
}
