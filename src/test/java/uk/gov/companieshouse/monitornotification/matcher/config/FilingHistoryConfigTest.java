package uk.gov.companieshouse.monitornotification.matcher.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.FilingHistoryDescriptions;

@ExtendWith(MockitoExtension.class)
class FilingHistoryConfigTest {

    private static final String FILING_HISTORY_FILE_NAME = "file:api-enumerations/filing_history_descriptions.yml";

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    Resource resource;

    @Mock
    Yaml yaml;

    @Mock
    Logger logger;

    FilingHistoryConfig underTest;

    @BeforeEach
    void setUp() {
        underTest = new FilingHistoryConfig(resourceLoader, logger);
    }

    @Test
    void givenContextLoaded_whenPropertiesPopulated_thenMapReturned() {
        FilingHistoryDescriptions descriptions = new FilingHistoryDescriptions(Map.of("key", "value"));

        assertThat(descriptions, is(notNullValue()));
        assertThat(descriptions.getDescriptions(), is(notNullValue()));
        assertThat(descriptions.getDescriptions().size(), is(1));
    }

    @Test
    void givenResourceNotExists_whenPropertiesLoaded_thenRaiseException() {
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        IllegalStateException expectedException = assertThrows(IllegalStateException.class, () -> {
            underTest.loadFilingHistoryDescriptions(yaml);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("YAML file not found at: %s".formatted(FILING_HISTORY_FILE_NAME)));
    }

    @Test
    void givenResourceExists_whenWrongStructure_thenRaiseException() throws IOException {
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(InputStream.nullInputStream());

        List<String> notAMap = List.of("This", "is", "not", "a", "map");
        when(yaml.load(any(InputStream.class))).thenReturn(notAMap);

        IllegalStateException expectedException = assertThrows(IllegalStateException.class, () -> {
            underTest.loadFilingHistoryDescriptions(yaml);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Expected YAML root to be a MAP"));
    }

    @Test
    void givenResourceExists_whenDescriptionMissing_thenRaiseException() throws IOException {
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(InputStream.nullInputStream());

        Map<String, String> isAMap = Map.of("key", "value", "anotherKey", "anotherValue");
        when(yaml.load(any(InputStream.class))).thenReturn(isAMap);

        IllegalStateException expectedException = assertThrows(IllegalStateException.class, () -> {
            underTest.loadFilingHistoryDescriptions(yaml);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("'description' section missing or not a map"));
    }

    @Test
    void givenInputStreamAvailable_whenIOExceptionRaised_thenRaiseRuntimeException() throws IOException {
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenThrow(FileNotFoundException.class);

        RuntimeException expectedException = assertThrows(RuntimeException.class, () -> {
            underTest.loadFilingHistoryDescriptions(yaml);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Error reading external YAML file"));
        assertThat(expectedException.getClass(), is(IllegalStateException.class));
    }
}
