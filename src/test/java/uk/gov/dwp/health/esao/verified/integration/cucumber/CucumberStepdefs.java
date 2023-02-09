package uk.gov.dwp.health.esao.verified.integration.cucumber;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.crypto.CryptoConfig;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.esao.verified.items.ClaimReferenceItem;
import uk.gov.dwp.health.messageq.EventConstants;
import uk.gov.dwp.health.messageq.amazon.items.AmazonConfigBase;
import uk.gov.dwp.health.messageq.amazon.items.messages.SnsMessageClassItem;
import uk.gov.dwp.health.messageq.amazon.utils.AmazonQueueUtilities;
import uk.gov.dwp.health.messageq.items.event.EventMessage;
import uk.gov.dwp.health.messageq.items.event.MetaData;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CucumberStepdefs {
  private static final Logger LOG = LoggerFactory.getLogger(CucumberStepdefs.class.getName());
  private static final String VALID_INPUT_JSON = "{\"claim_ref\":\"%s\"}";

  private HttpClient httpClient;
  private HttpResponse response;
  private String jsonResponse;

  private AmazonQueueUtilities queueUtilities;
  private CryptoDataManager awsKmsCryptoClass;
  private List<Message> messageList;

  private static final String LOCALSTACK_CONTAINER_HOST = "http://localstack";

  @Rule
  public WireMockRule caseServiceMock = new WireMockRule(wireMockConfig().port(3010));

  @Before
  public void setup() throws CryptoException, IllegalBlockSizeException, NoSuchPaddingException, IOException, NoSuchAlgorithmException, InvalidKeyException {

    // create local properties to negate KMS & SQS from needing to access Metadata Service for IAM role privs
    System.setProperty("aws.accessKeyId", "dummyaccess");
    System.setProperty("aws.secretKey", "dummysecret");

    AmazonConfigBase snsConfig = new AmazonConfigBase();
    snsConfig.setEndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4566");
    snsConfig.setPathStyleAccessEnabled(true);
    snsConfig.setS3BucketName(null);
    snsConfig.setRegion(Regions.US_EAST_1);

    AmazonConfigBase sqsConfig = new AmazonConfigBase();
    sqsConfig.setEndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4566");
    sqsConfig.setPathStyleAccessEnabled(true);
    sqsConfig.setS3BucketName(null);
    sqsConfig.setRegion(Regions.US_EAST_1);

    queueUtilities = new AmazonQueueUtilities(sqsConfig, snsConfig);

    CryptoConfig cryptoConfig = new CryptoConfig("alias/test_request_id");
    cryptoConfig.setKmsEndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4566");
    cryptoConfig.setRegion(Regions.US_EAST_1);
    awsKmsCryptoClass = new CryptoDataManager(cryptoConfig);

    httpClient = HttpClientBuilder.create().build();
    caseServiceMock.start();
  }

  @After
  public void tearDown() {
    caseServiceMock.stop();
  }

  @Given("^I POST claim reference \"([^\"]*)\" to \"([^\"]*)\"$")
  public void iPOSTClaimReferenceTo(String claimRef, String endpoint) throws IOException {
    performHttpPostWithUriOf(endpoint, String.format(VALID_INPUT_JSON, claimRef));
  }

  private void performHttpPostWithUriOf(String uri, String body) throws IOException {
    HttpPost httpUriRequest = new HttpPost(uri);
    httpUriRequest.setEntity(new StringEntity(body));

    response = httpClient.execute(httpUriRequest);
    HttpEntity responseEntity = response.getEntity();
    jsonResponse = EntityUtils.toString(responseEntity);
  }

  @And("^The response code is (\\d+)$")
  public void theResponseCodeIs(int responseCode) {
    assertThat(response.getStatusLine().getStatusCode(), is(equalTo(responseCode)));
  }

  @And("^The response body has claim reference \"([^\"]*)\"$")
  public void theResponseBodyHasClaimReference(String claimRef) throws IOException {
    JsonNode json = new ObjectMapper().readTree(jsonResponse);
    assertNotNull(json);

    assertThat(json.get("claim_ref").asText(), is(equalTo(claimRef)));
  }

  @And("^The json node \"([^\"]*)\" exists and is (ARRAY|STRING|INTEGER)?$")
  public void theJsonNodeExistsAndIsAn(String nodeName, String dataType) throws IOException {
    JsonNode json = new ObjectMapper().readTree(jsonResponse);
    assertNotNull(json);

    assertNotNull(json.get(nodeName));

    if (dataType.equalsIgnoreCase("ARRAY")) {
      assertTrue(json.get(nodeName).isArray());

    } else if (dataType.equalsIgnoreCase("STRING")) {
      assertTrue(json.get(nodeName).isTextual());

    } else if (dataType.equalsIgnoreCase("INTEGER")) {
      assertTrue(json.get(nodeName).isInt());
    }
  }

  @And(
      "^I stub the case service to return (\\d+) for case submission \"([^\"]*)\" with return body from file \"([^\"]*)\"$")
  public void iStubTheCaseServiceSubmissionToReturnWithReturnBodyFromFile(
      int returnVal, String claimRef, String returnContents) throws Throwable {

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s/application", claimRef)))
            .willReturn(aResponse().withStatus(returnVal).withBody(String.format(FileUtils.readFileToString(new File(returnContents)), claimRef))));
  }

  @And(
      "^I stub the case service to return (\\d+) for case application \"([^\"]*)\" with return body from file \"([^\"]*)\"$")
  public void iStubTheCaseServiceApplicationToReturnWithReturnBodyFromFile(
      int returnVal, String claimRef, String returnContents) throws Throwable {

    caseServiceMock.stubFor(
        get(urlEqualTo(String.format("/v1/cases/%s", claimRef)))
            .willReturn(aResponse().withStatus(returnVal).withBody(String.format(FileUtils.readFileToString(new File(returnContents)), claimRef))));
  }


  @Given("^a TOPIC has been created called \"([^\"]*)\"$")
  public void thatARabbitMQTOPICExchangeHasBeenCreatedCalled(String topicName) {
    queueUtilities.createTopic(topicName);
  }

  @Then("^a queue named \"([^\"]*)\" with visibility timeout (\\d+) is created, purged and bound to topic \"([^\"]*)\" with routing key filter policy \"([^\"]*)\"$")
  public void aQueueNamedHasBeenCreatedAndBoundToWithBindingKey(String queueName, int timeout, String topicName, String routingKey) {
    queueUtilities.createQueue(queueName, timeout);

    queueUtilities.purgeQueue(queueName);

    queueUtilities.subscribeQueueToTopicWithRoutingKeyPolicy(queueName, topicName, routingKey);
  }

  @Given("^There are (\\d+) pending messages in queue \"([^\"]*)\"$")
  public void thereArePendingMessagesInQueue(int msgCount, String queueName) throws IOException {
    int msgSize = queueUtilities.receiveMessages(queueName, queueUtilities.getS3Sqs()).size();
    assertThat(String.format("there should be nothing in the queue %s", queueName), msgSize, is(equalTo(msgCount)));
  }

  @And("^I remove the next message from \"([^\"]*)\" waiting up to (\\d+) seconds$")
  public void iTakeTheNextMessageFromAndThereArePendingMessagesLeftOnTheQueue(String queueName, int seconds) throws IOException, InterruptedException {
    for (int index = 0; index < (seconds * 2); index++) {
      messageList = queueUtilities.receiveMessages(queueName, queueUtilities.getS3Sqs());
      if (!messageList.isEmpty()) {
        break;
      }

      TimeUnit.MILLISECONDS.sleep(500);

      if ((index % 2) == 0) {
        LOG.debug("waiting for message visibility after {} seconds", (index / 2));
      }
    }

    assertNotNull("queue contents are nulL", messageList);
    queueUtilities.deleteMessageFromQueue(queueName, messageList.get(0).getReceiptHandle());
  }

  @Then(
      "^I publish the (ENCRYPTED|CLEAR-TEXT)? json for claim ref \"([^\"]*)\" to exchange \"([^\"]*)\" with routing key \"([^\"]*)\", triggerItem \"([^\"]*)\"$")
  public void iPublishACONTROLLERStyleJsonPayloadToExchangeWithRoutingKey(
      String encrypt, String claimRef, String topicName, String routingKey, String trigger)
      throws IOException, CryptoException {
    EventMessage msg = new EventMessage();
    msg.setMetaData(new MetaData(Collections.singletonList(trigger), routingKey));
    msg.setBodyContents(new ClaimReferenceItem(claimRef));
    publishMessageFor(encrypt, msg, topicName);
  }

  private void publishMessageFor(String encrypt, EventMessage messageObject, String topicName)
      throws IOException, CryptoException {
    CryptoMessage cryptoMessage = new CryptoMessage();

    if ((encrypt != null) && (encrypt.equalsIgnoreCase("encrypted"))) {
      cryptoMessage = awsKmsCryptoClass.encrypt(messageObject.serialisedBodyContentsToJson());
    }

    queueUtilities.publishMessageToTopic(topicName, messageObject, "test-subject", cryptoMessage.getKey(), cryptoMessage.getMessage());
  }

  @And(
      "^The received message contains a serialised ClaimReferenceItem with claim \"([^\"]*)\" and trigger \"([^\"]*)\"$")
  public void thereIsOnlyOneMessageAndItContainsASerialisedClaimReferenceItemWithClaim(
      String claimRef, String trigger) throws IOException, CryptoException {
    SnsMessageClassItem snsMessageClassItem = testDecodeAndReturnFirstMessageBody();

    assertThat(snsMessageClassItem.getMessageAttributes().get(EventConstants.TRIGGERED_BY_SERIALISED_LIST).getStringValue(), containsString(trigger));
    assertTrue(snsMessageClassItem.getMessageAttributes().containsKey(EventConstants.TRIGGERED_BY_SERIALISED_LIST));
    assertTrue(snsMessageClassItem.getMessageAttributes().containsKey(EventConstants.MESSAGE_CREATED_DT_STRING));

    ClaimReferenceItem item = new ObjectMapper().readValue(snsMessageClassItem.getMessage(), ClaimReferenceItem.class);
    assertNotNull(item);

    assertThat(item.getClaimRef(), is(equalTo(claimRef)));
  }

  @And(
      "^The received message is a DRS formatted case record with claim \"([^\"]*)\" and trigger \"([^\"]*)\"$")
  public void thereIsOnlyOneMessageAndItContainsASerialisedDrsRecordWithClaim(
      String claimRef, String trigger) throws IOException, CryptoException {
    SnsMessageClassItem snsMessageClassItem = testDecodeAndReturnFirstMessageBody();

    assertThat(snsMessageClassItem.getMessageAttributes().get(EventConstants.TRIGGERED_BY_SERIALISED_LIST).getStringValue(), containsString(trigger));
    assertTrue(snsMessageClassItem.getMessageAttributes().containsKey(EventConstants.TRIGGERED_BY_SERIALISED_LIST));
    assertTrue(snsMessageClassItem.getMessageAttributes().containsKey(EventConstants.MESSAGE_CREATED_DT_STRING));

    JsonNode jsonNode = new ObjectMapper().readTree(snsMessageClassItem.getMessage());

    assertTrue(jsonNode.get("metadata").isObject());
    assertTrue(jsonNode.get("payload").isObject());
    assertThat(jsonNode.get("metadata").get("claimRef").asText(), is(equalTo(claimRef)));
  }

  private SnsMessageClassItem testDecodeAndReturnFirstMessageBody()
      throws IOException, CryptoException {
    assertNotNull(messageList);
    assertThat(messageList.size(), is(equalTo(1)));
    assertNotNull(messageList.get(0));

    SnsMessageClassItem snsMessageClass = new SnsMessageClassItem().buildMessageClassItem(messageList.get(0).getBody());

    String msgContents = snsMessageClass.getMessage();
    if (snsMessageClass.getMessageAttributes().get(EventConstants.KMS_DATA_KEY_MARKER) != null) {
      CryptoMessage cryptoMessage = new CryptoMessage();
      cryptoMessage.setKey(snsMessageClass.getMessageAttributes().get(EventConstants.KMS_DATA_KEY_MARKER).getStringValue());
      cryptoMessage.setMessage(snsMessageClass.getMessage());
      msgContents = awsKmsCryptoClass.decrypt(cryptoMessage);
    }

    snsMessageClass.setMessage(msgContents);
    return snsMessageClass;
  }

  @And("^I wait (\\d+) seconds for the message to be processed$")
  public void iWaitSecondsForTheMessageToBeProcessed(int seconds) throws InterruptedException {
    TimeUnit.SECONDS.sleep(seconds);
  }
}
