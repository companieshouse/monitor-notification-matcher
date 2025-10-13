package uk.gov.companieshouse.monitornotification.matcher.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.FilingHistoryDescriptions;

@ExtendWith(MockitoExtension.class)
public class FilingHistoryConfigTest {

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    Logger logger;

    FilingHistoryConfig underTest;

    @BeforeEach
    void setUp() {
        underTest = new FilingHistoryConfig(resourceLoader, logger);
    }

    @Test
    public void givenContextLoaded_whenPropertiesPopulated_thenMapReturned() {
        FilingHistoryDescriptions descriptions = new FilingHistoryDescriptions(Map.of("key", "value"));

        assertThat(descriptions, is(notNullValue()));
        assertThat(descriptions.getDescriptions(), is(notNullValue()));
        assertThat(descriptions.getDescriptions().size(), is(1));
    }
}
