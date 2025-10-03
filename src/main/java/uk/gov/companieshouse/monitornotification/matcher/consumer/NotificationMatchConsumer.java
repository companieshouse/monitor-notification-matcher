package uk.gov.companieshouse.monitornotification.matcher.consumer;

import java.util.function.Consumer;
import monitor.filing;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.exception.RetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
import uk.gov.companieshouse.monitornotification.matcher.processor.MessageProcessor;

@Component
public class NotificationMatchConsumer {

    private final MessageProcessor processor;
    private final MessageFlags messageFlags;
    private final Logger logger;

    private Consumer<filing> callback;

    /**
     * Mandatory constructor.
     * @param processor the processor to delegate message processing to.
     * @param messageFlags flags to indicate the type of message being processed.
     * @param logger the logger to use for logging.
     */
    public NotificationMatchConsumer(MessageProcessor processor, MessageFlags messageFlags, Logger logger) {
        this.processor = processor;
        this.messageFlags = messageFlags;
        this.logger = logger;
    }

    /**
     * Consume a message from the main Kafka topic.
     * @param message A message containing a payload.
     */
    @KafkaListener(
            id = "${spring.kafka.consumer.notify.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = "${spring.kafka.consumer.notify.topic}",
            groupId = "${spring.kafka.consumer.notify.group-id}",
            autoStartup = "true"
    )
    @RetryableTopic(
            attempts = "${spring.kafka.consumer.notify.max-attempts}",
            autoCreateTopics = "false",
            backoff = @Backoff(delayExpression = "${spring.kafka.consumer.notify.backoff-delay}"),
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            include = RetryableException.class,
            kafkaTemplate = "kafkaTemplate"
    )

    public void consume(final Message<filing> message) {
        logger.debug("consume(message=%s) method called.".formatted(message));
        try {
            if (callback != null) {
                callback.accept(message.getPayload());
            }

            // Process the message via the message processor.
            processor.processMessage(message.getPayload());

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

    public void setCallback(final Consumer<filing> callback) {
        this.callback = callback;
    }

}

