package uk.gov.companieshouse.monitornotification.matcher.consumer;

import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

/**
 * Logs message details before and after it has been processed by
 * the {@link NotificationMatchConsumer main consumer}.<br>
 * <br>
 * Details that will be logged will include:
 * <ul>
 *     <li>The context ID of the message</li>
 *     <li>The topic the message was consumed from</li>
 *     <li>The partition of the topic the message was consumed from</li>
 *     <li>The offset number of the message</li>
 * </ul>
 */
@Component
@Aspect
public class MessageLoggingAspect {

    private static final String LOG_MESSAGE_RECEIVED = "Processing kafka message";
    private static final String LOG_MESSAGE_PROCESSED = "Processed kafka message";

    private final Logger logger;

    public MessageLoggingAspect(Logger logger) {
        this.logger = logger;
    }

    @Before("execution(* uk.gov.companieshouse.monitornotification.matcher.consumer.NotificationMatchConsumer.consume(..))")
    void logBeforeMainConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_RECEIVED, (Message<?>)joinPoint.getArgs()[0]);
    }

    @After("execution(* uk.gov.companieshouse.monitornotification.matcher.consumer.NotificationMatchConsumer.consume(..))")
    void logAfterMainConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_PROCESSED, (Message<?>)joinPoint.getArgs()[0]);
    }

    private void logMessage(String logMessage, Message<?> incomingMessage) {
        MessageHeaders messageHeaders = incomingMessage.getHeaders();

        var topic = (String) Optional.ofNullable(messageHeaders.get(KafkaHeaders.RECEIVED_TOPIC)).orElse("no topic");
        var partition = (Integer) Optional.ofNullable(messageHeaders.get(KafkaHeaders.RECEIVED_PARTITION)).orElse(0);
        var offset = (Long) Optional.ofNullable(messageHeaders.get(KafkaHeaders.OFFSET)).orElse(0L);
        var correlationId = (String) Optional.ofNullable(messageHeaders.get(KafkaHeaders.CORRELATION_ID))
                .orElse(UUID.randomUUID().toString());

        var dataMap = new DataMap.Builder()
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .kafkaMessage(incomingMessage.getPayload().toString())
                .requestId(correlationId)
                .build();

        logger.debug(logMessage, dataMap.getLogMap());
    }
}

