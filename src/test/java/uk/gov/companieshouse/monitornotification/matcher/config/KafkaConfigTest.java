package uk.gov.companieshouse.monitornotification.matcher.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import monitor.filing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.NotificationMatchConsumerProperties;

@ExtendWith(MockitoExtension.class)
public class KafkaConfigTest {

    private NotificationMatchConsumerProperties notificationMatchProperties;
    private KafkaConfig underTest;

    @BeforeEach
    public void setUp() {
        notificationMatchProperties = new NotificationMatchConsumerProperties();
        notificationMatchProperties.setTopic("test-topic");
        notificationMatchProperties.setGroupId("test-group");
        notificationMatchProperties.setConcurrency(1);
        notificationMatchProperties.setMaxAttempts(3);
        notificationMatchProperties.setBackOffDelay(1000L);

        String bootstrapServers = "localhost:9092";
        Logger logger = LoggerFactory.getLogger("test-logger");

        underTest = new KafkaConfig(notificationMatchProperties, bootstrapServers, logger);
    }

    @Test
    public void givenKafkaConfigProperties_whenLoaded_thenValuesAreSet() {
        assertThat(notificationMatchProperties, is(notNullValue()));

        assertThat(notificationMatchProperties.getTopic(), is("test-topic"));
        assertThat(notificationMatchProperties.getGroupId(), is("test-group"));
        assertThat(notificationMatchProperties.getConcurrency(), is(1));
        assertThat(notificationMatchProperties.getMaxAttempts(), is(3));
        assertThat(notificationMatchProperties.getBackOffDelay(), is(1000L));
    }

    @Test
    public void givenConfigProvider_whenKafkaTemplateCreated_thenNoErrorsAreRaised() {
        KafkaTemplate<String, Object> result = underTest.kafkaTemplate();

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void givenConfigProvider_whenKafkaConsumerFactoryCreated_thenNoErrorsAreRaised() {
        ConsumerFactory<String, filing> result = underTest.kafkaConsumerFactory();

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void givenConfigProvider_whenKafkaListenerContainerFactoryCreated_thenNoErrorsAreRaised() {
        ConcurrentKafkaListenerContainerFactory<String, filing> result = underTest.kafkaListenerContainerFactory();

        assertThat(result, is(notNullValue()));
    }
}
