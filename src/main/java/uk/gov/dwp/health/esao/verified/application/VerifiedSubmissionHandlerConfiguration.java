package uk.gov.dwp.health.esao.verified.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import jakarta.validation.constraints.NotNull;
import uk.gov.dwp.crypto.SecureStrings;
import uk.gov.dwp.health.crypto.CryptoConfig;
import uk.gov.dwp.health.messageq.amazon.items.AmazonConfigBase;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class VerifiedSubmissionHandlerConfiguration extends Configuration {
  private SecureStrings cipher;

  public VerifiedSubmissionHandlerConfiguration()
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    cipher = new SecureStrings();
  }

  @NotNull
  @JsonProperty("sqsConfiguration")
  private AmazonConfigBase sqsConfig;

  @NotNull
  @JsonProperty("snsConfiguration")
  private AmazonConfigBase snsConfig;

  @JsonProperty("kmsQueueCryptoConfiguration")
  private CryptoConfig kmsQueueCryptoConfiguration;

  @JsonProperty("sqsQueueName")
  private String sqsQueueName;

  @JsonProperty("snsTopicName")
  private String snsTopicName;

  @JsonProperty("snsRoutingKey")
  private String snsRoutingKey;

  @JsonProperty("snsSubject")
  private String snsSubject;

  @JsonProperty("snsEncryptMessages")
  private boolean snsEncryptMessages;

  @NotNull
  @JsonProperty("caseServiceApplicationEndpoint")
  private URL caseServiceApplicationEndpoint;

  @NotNull
  @JsonProperty("caseServiceQueryEndpoint")
  private URL caseServiceQueryEndpoint;

  @NotNull
  @JsonProperty("caseServiceParameter")
  private String caseServiceParameter;

  @JsonProperty("caseServiceTruststoreFile")
  private String caseServiceTruststoreFile;

  @JsonProperty("caseServiceTruststorePass")
  private SealedObject caseServiceTruststorePass;

  @JsonProperty("caseServiceKeystoreFile")
  private String caseServiceKeystoreFile;

  @JsonProperty("caseServiceKeystorePass")
  private SealedObject caseServiceKeystorePass;

  @JsonProperty("applicationInfoEnabled")
  private boolean applicationInfoEnabled;

  public URL getCaseServiceQueryEndpoint() {
    return caseServiceQueryEndpoint;
  }

  public String getCaseServiceParameter() {
    return caseServiceParameter;
  }

  public String getCaseServiceTruststoreFile() {
    return caseServiceTruststoreFile;
  }

  public String getCaseServiceKeystoreFile() {
    return caseServiceKeystoreFile;
  }

  public String getCaseServiceTruststorePass() {
    return getCipher().revealString(caseServiceTruststorePass);
  }

  public void setCaseServiceTruststorePass(String caseServiceTruststorePass)
      throws IllegalBlockSizeException, IOException {
    this.caseServiceTruststorePass = getCipher().sealString(caseServiceTruststorePass);
  }

  public String getCaseServiceKeystorePass() {
    return getCipher().revealString(caseServiceKeystorePass);
  }

  public void setCaseServiceKeystorePass(String caseServiceKeystorePass)
      throws IllegalBlockSizeException, IOException {
    this.caseServiceKeystorePass = getCipher().sealString(caseServiceKeystorePass);
  }

  public URL getCaseServiceApplicationEndpoint() {
    return caseServiceApplicationEndpoint;
  }

  public SecureStrings getCipher() {
    return cipher;
  }

  public AmazonConfigBase getSqsConfig() {
    return sqsConfig;
  }

  public CryptoConfig getKmsQueueCryptoConfiguration() {
    return kmsQueueCryptoConfiguration;
  }

  public String getSqsQueueName() {
    return sqsQueueName;
  }

  public AmazonConfigBase getSnsConfig() {
    return snsConfig;
  }

  public String getSnsTopicName() {
    return snsTopicName;
  }

  public String getSnsRoutingKey() {
    return snsRoutingKey;
  }

  public String getSnsSubject() {
    return snsSubject;
  }

  public boolean isSnsEncryptMessages() {
    return snsEncryptMessages;
  }

  public boolean isApplicationInfoEnabled() {
    return applicationInfoEnabled;
  }
}
