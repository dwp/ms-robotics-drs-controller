package uk.gov.dwp.health.esao.verified.application;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.MessageEncoder;
import uk.gov.dwp.health.esao.verified.ServiceInfoResource;
import uk.gov.dwp.health.esao.verified.SubmissionHandlerResource;
import uk.gov.dwp.health.esao.verified.consumers.IncomingMgSubscription;
import uk.gov.dwp.health.esao.verified.handlers.CaseServiceHandler;
import uk.gov.dwp.health.esao.verified.handlers.JsapsTransformationHandler;
import uk.gov.dwp.health.esao.verified.info.PropertyFileInfoProvider;
import uk.gov.dwp.health.messageq.amazon.sns.MessagePublisher;
import uk.gov.dwp.health.messageq.amazon.sqs.MessageConsumer;
import uk.gov.dwp.health.messageq.amazon.sqs.MessageReceiver;
import uk.gov.dwp.tls.TLSConnectionBuilder;

public class VerifiedSubmissionHandlerApplication
    extends Application<VerifiedSubmissionHandlerConfiguration> {
  private static final Logger LOG =
      LoggerFactory.getLogger(VerifiedSubmissionHandlerApplication.class.getName());

  @Override
  protected void bootstrapLogging() {
    // to prevent dropwizard using its own standard logger
  }

  @Override
  public void initialize(Bootstrap<VerifiedSubmissionHandlerConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)));
  }

  @Override
  public void run(VerifiedSubmissionHandlerConfiguration configuration, Environment environment)
      throws Exception {
    final TLSConnectionBuilder connectionBuilder =
        new TLSConnectionBuilder(
            configuration.getCaseServiceTruststoreFile(),
            configuration.getCaseServiceTruststorePass(),
            configuration.getCaseServiceKeystoreFile(),
            configuration.getCaseServiceKeystorePass());

    final JsapsTransformationHandler jsapsTransformationHandler = new JsapsTransformationHandler();
    final CaseServiceHandler caseServiceLookup =
        new CaseServiceHandler(configuration, connectionBuilder);

    CryptoDataManager kmsCrypto = null;
    if (null != configuration.getKmsQueueCryptoConfiguration()) {
      kmsCrypto = new CryptoDataManager(configuration.getKmsQueueCryptoConfiguration());
    }

    final MessageEncoder<MessageAttributeValue> messageEncoder =
        new MessageEncoder<>(kmsCrypto, MessageAttributeValue.class);
    final MessagePublisher snsPublisher =
        new MessagePublisher(messageEncoder, configuration.getSnsConfig());

    LOG.info(
        "creating subscription for queue {} with read frequency {}",
        configuration.getSqsQueueName(),
        configuration.getSqsConfig().getSqsReadFrequencyMillis());

    IncomingMgSubscription subscription =
        new IncomingMgSubscription(configuration, caseServiceLookup, snsPublisher);
    MessageReceiver receiver = new MessageReceiver(kmsCrypto, configuration.getSqsConfig());

    MessageConsumer consumer =
        new MessageConsumer(receiver, configuration.getSqsQueueName(), subscription);
    consumer.setReadFrequencyMillis(configuration.getSqsConfig().getSqsReadFrequencyMillis());

    final SubmissionHandlerResource instance =
        new SubmissionHandlerResource(jsapsTransformationHandler, caseServiceLookup);
    environment.jersey().register(instance);

    if (configuration.isApplicationInfoEnabled()) {
      final ServiceInfoResource infoInstance =
          new ServiceInfoResource(new PropertyFileInfoProvider("application.yml"));
      environment.jersey().register(infoInstance);
    }
  }

  public static void main(String[] args) throws Exception {
    new VerifiedSubmissionHandlerApplication().run(args);
  }
}
