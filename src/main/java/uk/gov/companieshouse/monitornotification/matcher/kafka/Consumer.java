package uk.gov.companieshouse.monitornotification.matcher.kafka;

import static java.lang.String.format;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.RetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;

@Component
public class Consumer {

    private final Router router;
    private final MessageFlags messageFlags;
    private final Logger logger;

    public Consumer(Router router, MessageFlags messageFlags, Logger logger) {
        this.router = router;
        this.messageFlags = messageFlags;
        this.logger = logger;
    }

    @KafkaListener(
            id = "${consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = {"${consumer.topic}"},
            groupId = "${consumer.group-id}"
    )
    public void consume(final Message<ChsDelta> message) {
        logger.trace(format("consume(message=%s) method called.", message));
        try {
            router.route(message.getPayload());

        } catch (RetryableException ex) {
            logger.error("Retryable exception encountered while processing message. Will retry.",
                    ex, DataMapHolder.getLogMap()
            );
            messageFlags.setRetryable(true);
            throw ex;
        }
    }
}
