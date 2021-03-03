package uk.gov.dwp.health.esao.verified.utils;

public enum EmployeeStatusEnum {
  employee("Employee"),
  selfEmployed("Self-Employed"),
  subContractor("Sub-Contractor"),
  director("Company Director"),
  none("");

  private String value;
  EmployeeStatusEnum(String statusValue) {
    value = statusValue;
  }

  public String getJsapsValue() {
    return value;
  }
}
