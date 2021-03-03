Feature: Robotics query endpoint functionality

  Scenario: A 200 is returned for a valid ClaimRef along with the JSON body
    Given I stub the case service to return 200 for case application "Claim1234" with return body from file "src/test/resources/verified-esa-application.json"
      And I stub the case service to return 200 for case submission "Claim1234" with return body from file "src/test/resources/full-application-return.json"
    Then I POST claim reference "Claim1234" to "http://localhost:9501/iagQueryEsaSubmissionByClaimRef"
      And The response code is 200
      And The response body has claim reference "Claim1234"
      And The json node "medical_conditions" exists and is ARRAY
      And The json node "submitted_date" exists and is STRING
      And The json node "nino" exists and is STRING

  Scenario: I successfully process a CLEAR-TEXT verified clear-text ClaimRef event and send to DRS
    Given a TOPIC has been created called "esa-robotics-topic"
      And a queue named "esa-robotics-queue" with visibility timeout 5 is created, purged and bound to topic "esa-robotics-topic" with routing key filter policy "sns.status.verified.esa"
      And a queue named "esa-drs-queue" with visibility timeout 5 is created, purged and bound to topic "esa-robotics-topic" with routing key filter policy "sns.drs.verified.submission"
      And a queue named "verified-case-monitor" with visibility timeout 5 is created, purged and bound to topic "esa-robotics-topic" with routing key filter policy "sns.status.verified.esa"
    Then I stub the case service to return 200 for case application "Claim1234" with return body from file "src/test/resources/verified-esa-application.json"
      And I stub the case service to return 200 for case submission "Claim1234" with return body from file "src/test/resources/full-application-return.json"
    Then I publish the CLEAR-TEXT json for claim ref "Claim1234" to exchange "esa-robotics-topic" with routing key "sns.status.verified.esa", triggerItem "verified.case"
      And There are 1 pending messages in queue "verified-case-monitor"
      And I remove the next message from "verified-case-monitor" waiting up to 10 seconds
      And There are 0 pending messages in queue "verified-case-monitor"
      And The received message contains a serialised ClaimReferenceItem with claim "Claim1234" and trigger "verified.case"
    Then There are 0 pending messages in queue "esa-robotics-queue"
      And There are 1 pending messages in queue "esa-drs-queue"
      And I remove the next message from "esa-drs-queue" waiting up to 10 seconds
      And There are 0 pending messages in queue "esa-drs-queue"
      And The received message is a DRS formatted case record with claim "Claim1234" and trigger "verified-esa-claim"

  Scenario: I successfully process an ENCRYPTED verified clear-text ClaimRef event and send to DRS
    Given a TOPIC has been created called "esa-robotics-topic"
    And a queue named "esa-robotics-queue" with visibility timeout 5 is created, purged and bound to topic "esa-robotics-topic" with routing key filter policy "sns.status.verified.esa"
    And a queue named "esa-drs-queue" with visibility timeout 5 is created, purged and bound to topic "esa-robotics-topic" with routing key filter policy "sns.drs.verified.submission"
    And a queue named "verified-case-monitor" with visibility timeout 5 is created, purged and bound to topic "esa-robotics-topic" with routing key filter policy "sns.status.verified.esa"
    Then I stub the case service to return 200 for case application "Claim1234" with return body from file "src/test/resources/verified-esa-application.json"
    And I stub the case service to return 200 for case submission "Claim1234" with return body from file "src/test/resources/full-application-return.json"
    Then I publish the ENCRYPTED json for claim ref "Claim1234" to exchange "esa-robotics-topic" with routing key "sns.status.verified.esa", triggerItem "verified.case"
    And There are 1 pending messages in queue "verified-case-monitor"
    And I remove the next message from "verified-case-monitor" waiting up to 10 seconds
    And There are 0 pending messages in queue "verified-case-monitor"
    And The received message contains a serialised ClaimReferenceItem with claim "Claim1234" and trigger "verified.case"
    Then There are 0 pending messages in queue "esa-robotics-queue"
    And There are 1 pending messages in queue "esa-drs-queue"
    And I remove the next message from "esa-drs-queue" waiting up to 10 seconds
    And There are 0 pending messages in queue "esa-drs-queue"
    And The received message is a DRS formatted case record with claim "Claim1234" and trigger "verified-esa-claim"
