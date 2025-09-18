package uk.gov.companieshouse.monitornotification.matcher.producer;

import monitor.filing;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.NotificationMatchProducerProperties;

@Component
public class NotificationMatchProducer {

    private final NotificationMatchProducerProperties properties;
    private final KafkaTemplate<String, Object> template;
    private final Logger logger;

    public NotificationMatchProducer(NotificationMatchProducerProperties properties,
            KafkaTemplate<String, Object> template,
            Logger logger) {
        this.properties = properties;
        this.template = template;
        this.logger = logger;
    }

    public void sendMessage(final filing message) {
        logger.trace("sendMessage(message=%s) method called.".formatted(message));

        // Send the converted message to the Kafka topic
        template.send(properties.getTopic(), message);
    }

}

