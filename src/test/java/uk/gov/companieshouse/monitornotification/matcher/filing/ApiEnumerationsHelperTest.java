package uk.gov.companieshouse.monitornotification.matcher.filing;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ApiEnumerationsHelperTest {
    private static final String DESCRIPTION_KEY = "appoint-person-director-company-with-name-date";
    private static final String DESCRIPTION_KEY_INVALID = "appoint-person-director-company-with-name";
    private static final String DESCRIPTION = "**Appointment** of {officer_name} as a director on {appointment_date}";
    private static final String PARAMETERISED_HISTORY_DESCRIPTION = "Appointment of DR AMIDAT DUPE IYIOLA as a director on 1 December 2024";
    private static final String DESCRIPTIONS_JSON_BLOB = "{\"description\" : \"appoint-person-director-company-with-name-date\",\"description_values\" : {\"appointment_date\" : \"1 December 2024\",\"officer_name\" : \"DR AMIDAT DUPE IYIOLA\"}}";
  
    @Mock
    private FilingHistoryDescriptions descriptions;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Logger logger;

    @InjectMocks
    private ApiEnumerationsHelper underTest;

    @BeforeEach
    public void setUp() {
        underTest = new ApiEnumerationsHelper(descriptions, mapper, logger);
    }

    @Test
    void testGetHistoryDescriptionOK() {
        when(descriptions.getDescriptions()).thenReturn(Map.of(DESCRIPTION_KEY, DESCRIPTION));
        when(mapper.convertValue(any(JsonNode.class), any(Class.class))).thenReturn(new HashMap<String, Object>() {{
            put("appointment_date", "1 December 2024");
            put("officer_name", "DR AMIDAT DUPE IYIOLA");
        }});

        JsonNode descriptionValues = findDataNode("description_values");
        String result = underTest.getFilingHistoryDescription(DESCRIPTION_KEY, descriptionValues);

        assertEquals(PARAMETERISED_HISTORY_DESCRIPTION, result);
    }

    @Test
    void testGetHistoryDescriptionValuesIsNullOK() {
        when(descriptions.getDescriptions()).thenReturn(Map.of(DESCRIPTION_KEY, DESCRIPTION));

        JsonNode descriptionValues = findDataNode("description_values");
        String result = underTest.getFilingHistoryDescription(DESCRIPTION_KEY_INVALID, descriptionValues);

        assertNull(result);
    }

    private JsonNode findDataNode(final String nodeName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(DESCRIPTIONS_JSON_BLOB);
            JsonNode node = root.get(nodeName);
            return node;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
