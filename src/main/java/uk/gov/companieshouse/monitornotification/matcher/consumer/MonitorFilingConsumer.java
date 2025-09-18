package uk.gov.companieshouse.monitornotification.matcher.consumer;

import java.util.function.Consumer;
import monitor.transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.converter.TransactionToFilingConverter;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.exception.RetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
import uk.gov.companieshouse.monitornotification.matcher.service.MatcherService;

@Component
public class MonitorFilingConsumer {

    private final MatcherService service;
    private final TransactionToFilingConverter converter;
    private final MessageFlags messageFlags;
    private final Logger logger;

    private Consumer<transaction> callback;

    /**
     * Mandatory constructor.
     * @param service the service to delegate message processing to.
     * @param messageFlags flags to indicate the type of message being processed.
     * @param logger the logger to use for logging.
     */
    public MonitorFilingConsumer(MatcherService service, TransactionToFilingConverter converter, MessageFlags messageFlags, Logger logger) {
        this.service = service;
        this.converter = converter;
        this.messageFlags = messageFlags;
        this.logger = logger;
    }

    /**
     * Consume a message from the main Kafka topic.
     * @param message A message containing a payload.
     */
    @KafkaListener(
            id = "${spring.kafka.consumer.filing.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = "${spring.kafka.consumer.filing.topic}",
            groupId = "${spring.kafka.consumer.filing.group-id}",
            autoStartup = "true"
    )
    @RetryableTopic(
            attempts = "${spring.kafka.consumer.filing.max-attempts}",
            autoCreateTopics = "false",
            backoff = @Backoff(delayExpression = "${spring.kafka.consumer.filing.backoff-delay}"),
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            include = RetryableException.class,
            kafkaTemplate = "kafkaTemplate"
    )

    public void consume(final Message<transaction> message) {
        logger.debug("consume(message=%s) method called.".formatted(message));
        try {
            if (callback != null) {
                callback.accept(message.getPayload());
            }

            // Process the message via the matcher service.
            service.processMessage(message.getPayload());

        } catch(NonRetryableException ex) {
            logger.error("Non-Retryable exception encountered processing message!", ex, DataMapHolder.getLogMap());
            messageFlags.setRetryable(false);
            throw ex;

        } catch (RetryableException ex) {
            logger.error("Retryable exception encountered processing message.", ex, DataMapHolder.getLogMap());
            messageFlags.setRetryable(true);
            throw ex;
        }
    }

    public void setCallback(final Consumer<transaction> callback) {
        this.callback = callback;
    }

}
