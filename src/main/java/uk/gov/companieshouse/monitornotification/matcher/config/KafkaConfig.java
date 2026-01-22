package uk.gov.companieshouse.monitornotification.matcher.config;

import java.util.HashMap;
import java.util.Map;
import monitor.filing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.NotificationMatchConsumerProperties;
import uk.gov.companieshouse.monitornotification.matcher.exception.RetryableException;
import uk.gov.companieshouse.monitornotification.matcher.exception.RetryableTopicErrorInterceptor;
import uk.gov.companieshouse.monitornotification.matcher.serdes.GenericSerializer;
import uk.gov.companieshouse.monitornotification.matcher.serdes.NotificationMatchDeserializer;

@Configuration
@EnableKafka
@Profile("!test")
public class KafkaConfig {

    private final NotificationMatchConsumerProperties properties;
    private final String bootstrapServers;
    private final Logger logger;

    /**
     * Constructor.
     */
    public KafkaConfig(NotificationMatchConsumerProperties newProperties,
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            Logger logger) {
        this.properties = newProperties;
        this.bootstrapServers = bootstrapServers;
        this.logger = logger;
    }

    /**
     * Kafka NotificationMatchConsumer Factory.
     */
    @Bean("kafkaConsumerFactory")
    public ConsumerFactory<String, filing> kafkaConsumerFactory() {
        logger.trace("createKafkaConsumerFactory() method called.");

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, NotificationMatchDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new NotificationMatchDeserializer()));
    }

    /**
     * Kafka Producer Factory.
     */
    @Bean("kafkaProducerFactory")
    public ProducerFactory<String, Object> kafkaProducerFactory() {
        logger.trace("createKafkaProducerFactory() method called.");

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "false");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericSerializer.class);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, RetryableTopicErrorInterceptor.class.getName());

        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new GenericSerializer());
    }

    @Bean("kafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    /**
     * Kafka Listener Container Factory.
     */
    @Bean("kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, filing> kafkaListenerContainerFactory() {
        logger.trace("kafkaListenerContainerFactory() method called.");

        ConcurrentKafkaListenerContainerFactory<String, filing> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory());
        factory.setConcurrency(properties.getConcurrency());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }

    @Bean
    public RetryTopicConfiguration retryTopicConfiguration(
            KafkaTemplate<String, Object> template,
            @Value("${spring.kafka.consumer.notify.max-attempts}") int attempts,
            @Value("${spring.kafka.consumer.notify.backoff-delay}") int delay) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .doNotAutoCreateRetryTopics()
                .maxAttempts(attempts)
                .fixedBackOff(delay)
                .useSingleTopicForSameIntervals()
                .retryTopicSuffix("-retry")
                .dltSuffix("-dlt-error")
                .dltProcessingFailureStrategy(DltStrategy.FAIL_ON_ERROR)
                .retryOn(RetryableException.class)
                .create(template);
    }
}
