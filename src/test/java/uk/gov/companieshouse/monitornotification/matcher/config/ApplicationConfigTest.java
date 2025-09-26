package uk.gov.companieshouse.monitornotification.matcher.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class ApplicationConfigTest {

    ApplicationConfig underTest;

    @BeforeEach
    void setUp() {
        underTest = new ApplicationConfig();
    }

    @Test
    void givenStartupOk_whenGetLogger_thenLoggerReturned() {
        Logger result = underTest.logger("test-application");

        assertThat(result, is(notNullValue()));
    }

    @Test
    void givenStartupOk_whenSerializerFactory_thenSerializerFactoryReturned() {
        SerializerFactory result = underTest.serializerFactory();

        assertThat(result, is(notNullValue()));
    }

    @Test
    void givenStartupOk_whenEnvironmentReader_thenEnvironmentReaderReturned() {
        EnvironmentReader result = underTest.environmentReader();

        assertThat(result, is(notNullValue()));
    }

    @Test
    void givenStartupOk_whenObjectMapper_thenObjectMapperReturned() {
        ObjectMapper result = underTest.objectMapper();

        assertThat(result, is(notNullValue()));
    }
}
