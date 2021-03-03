package uk.gov.dwp.health.esao.verified.items.drs;

import uk.gov.dwp.components.drs.Metadata;
import uk.gov.dwp.regex.NinoValidator;

public class EsaDrsMetadata extends Metadata {

  private String postCode;
  private NinoValidator nino;
  private int benefitType;
  private String customerMobileNumber;
  private String claimRef;

  public String getClaimRef() {
    return claimRef;
  }

  public void setClaimRef(String claimRef) {
    this.claimRef = claimRef;
  }

  public String getPostCode() {
    return postCode;
  }

  public void setPostCode(String postCode) {
    this.postCode = postCode;
  }

  public NinoValidator getNino() {
    return nino;
  }

  public void setNino(NinoValidator nino) {
    this.nino = nino;
  }

  public int getBenefitType() {
    return benefitType;
  }

  public void setBenefitType(int benefitType) {
    this.benefitType = benefitType;
  }

  public String getCustomerMobileNumber() {
    return customerMobileNumber;
  }

  public void setCustomerMobileNumber(String customerMobileNumber) {
    this.customerMobileNumber = customerMobileNumber;
  }
}
