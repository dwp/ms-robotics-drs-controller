package uk.gov.dwp.health.esao.verified.integration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import uk.gov.dwp.health.esao.verified.application.VerifiedSubmissionHandlerApplication;
import uk.gov.dwp.health.esao.verified.application.VerifiedSubmissionHandlerConfiguration;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@RunWith(Cucumber.class)
@SuppressWarnings({"squid:S2187", "squid:S1118"}) // no tests needed to kick of cucumber
@CucumberOptions(plugin = "json:target/cucumber-report.json")
public class RunCukesTest {

  private static final String CONFIG_FILE = "test.yml";

  @ClassRule
  public static final DropwizardAppRule<VerifiedSubmissionHandlerConfiguration> RULE =
      new DropwizardAppRule<>(VerifiedSubmissionHandlerApplication.class, resourceFilePath(CONFIG_FILE));
}
