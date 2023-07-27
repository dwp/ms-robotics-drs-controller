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
import uk.gov.dwp.health.esao.shared.models.MedicalCentre;
import uk.gov.dwp.health.esao.verified.constants.MessageConstants;
import uk.gov.dwp.health.esao.verified.interfaces.BaseItem;
import uk.gov.dwp.health.esao.verified.utils.DataTransformation;
import uk.gov.dwp.health.esao.verified.utils.ValidationUtils;

import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalDetailsTransformItem implements BaseItem {
  private static final Logger LOG =
      LoggerFactory.getLogger(MedicalDetailsTransformItem.class.getName());

  @NotNull
  @JsonProperty("doctor_title")
  private String doctorTitle;

  @NotNull
  @JsonProperty("doctor_forename")
  private String doctorForename;

  @NotNull
  @JsonProperty("doctor_surname")
  private String doctorSurname;

  @NotNull
  @JsonProperty("surgery_name")
  private String surgeryName;

  @NotNull
  @JsonProperty("surgery_address")
  private AddressTransformItem surgeryAddress;

  @NotNull
  @JsonProperty("phone_number")
  private String phoneNumber;

  @SuppressWarnings("squid:S2637") // not null values will be set by originalItem
  public MedicalDetailsTransformItem(MedicalCentre originalItem) {
    setAndTransformDoctorField(originalItem.getDoctorName());
    this.surgeryName = originalItem.getName();
    this.surgeryAddress = new AddressTransformItem(originalItem.getAddress());
    this.phoneNumber = DataTransformation.transformToJsapsPhoneNumber(originalItem.getTelephone());
  }

  @JsonIgnore
  public void setAndTransformDoctorField(String doctorFullName) {
    if (doctorFullName == null) {
      this.doctorTitle = MessageConstants.BLANK;
      this.doctorForename = MessageConstants.BLANK;
    } else {
      // SME confirmed that this will always be entered whether blank or not
      this.doctorTitle = "Dr.";
      this.doctorForename =
          Pattern.compile("\\bDr\\.(?=\\W)|\\bDoctor\\b|\\bMr\\.(?=\\W)")
              .matcher(doctorFullName)
              .replaceAll(MessageConstants.BLANK)
              .trim(); // only full word matches
    }
    // SME confirmed empty field for surname is acceptable
    this.doctorSurname = MessageConstants.BLANK;
  }

  @Override
  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = ValidationUtils.validateAnnotations(LOG, this);

    if (isValid) {
      isValid = !getSurgeryName().isEmpty();
      ValidationUtils.logOutput(LOG, "surgery_name", isValid);
    }

    if (isValid) {
      isValid = getSurgeryAddress().isContentValid();
    }

    if (isValid) {
      isValid = !getPhoneNumber().isEmpty();
      ValidationUtils.logOutput(LOG, "phone_number", isValid);
    }

    return isValid;
  }
}
