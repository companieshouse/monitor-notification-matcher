package uk.gov.companieshouse.monitornotification.matcher.filing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.FilingHistoryDescriptions;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDescriptionConverterTest {

    private static final String DESCRIPTION_KEY = "appoint-person-director-company-with-name-date";
    private static final String DESCRIPTION = "**Appointment** of {officer_name} as a director on {appointment_date}";

    @Mock
    private FilingHistoryDescriptions descriptions;

    @Mock
    private Logger logger;

    @InjectMocks
    private FilingHistoryDescriptionConverter underTest;

    @BeforeEach
    public void setUp() {
        underTest = new FilingHistoryDescriptionConverter(descriptions, logger);
    }

    @Test
    void givenDescriptionValues_whenDescriptionSupplied_thenReturnCorrectTranslation() {
        when(descriptions.getDescriptions()).thenReturn(Map.of(DESCRIPTION_KEY, DESCRIPTION));

        Map<String, String> descriptionValuesMap = new HashMap<>() {{
            put("appointment_date", "1 December 2024");
            put("officer_name", "DR AMIDAT DUPE IYIOLA");
        }};

        String result = underTest.getFilingHistoryDescription(DESCRIPTION_KEY, descriptionValuesMap);

        verify(logger, times(1)).trace(anyString());

        assertThat(result, is("Appointment of DR AMIDAT DUPE IYIOLA as a director on 1 December 2024"));
    }

    @Test
    void givenDescriptionValues_whenNullDescriptionSupplied_thenReturnCorrectTranslation() {
        Map<String, String> descriptionValuesMap = new HashMap<>() {{
            put("appointment_date", "1 December 2024");
            put("officer_name", "DR AMIDAT DUPE IYIOLA");
        }};

        String result = underTest.getFilingHistoryDescription(null, descriptionValuesMap);

        verify(logger, times(1)).trace(anyString());
        verify(logger, times(1)).error("The description key provided was not valid: null");

        assertThat(result, is(nullValue()));
    }

    @Test
    void givenDescriptionValues_whenBlankDescriptionSupplied_thenReturnCorrectTranslation() {
        Map<String, String> descriptionValuesMap = new HashMap<>() {{
            put("appointment_date", "1 December 2024");
            put("officer_name", "DR AMIDAT DUPE IYIOLA");
        }};

        String result = underTest.getFilingHistoryDescription("", descriptionValuesMap);

        verify(logger, times(1)).trace(anyString());
        verify(logger, times(1)).error("The description key provided was not valid: ");

        assertThat(result, is(nullValue()));
    }

    @Test
    void givenEmptyDescriptionValues_whenDescriptionSupplied_thenReturnCorrectTranslation() {
        when(descriptions.getDescriptions()).thenReturn(Map.of());

        Map<String, String> descriptionValuesMap = new HashMap<>() {{
            put("appointment_date", "1 December 2024");
            put("officer_name", "DR AMIDAT DUPE IYIOLA");
        }};

        String result = underTest.getFilingHistoryDescription(DESCRIPTION_KEY, descriptionValuesMap);

        verify(logger, times(1)).trace(anyString());
        verify(logger, times(1)).info("No filing history descriptions available for: %s".formatted(DESCRIPTION_KEY));

        assertThat(result, is(nullValue()));
    }
}
