package uk.gov.dwp.health.esao.verified.utils;

@SuppressWarnings("squid:S00115")
public enum PaymentFrequencyInputEnum {
  daily(1),
  weekly(2),
  every2Weeks(3),
  every4Weeks(4),
  monthly(5),
  quarterly(6),
  every6Months(7),
  annually(8);

  private int level;

  PaymentFrequencyInputEnum(int jsapsValue) {
    level = jsapsValue;
  }

  public int getJsapsValue() {
    return level;
  }
}
