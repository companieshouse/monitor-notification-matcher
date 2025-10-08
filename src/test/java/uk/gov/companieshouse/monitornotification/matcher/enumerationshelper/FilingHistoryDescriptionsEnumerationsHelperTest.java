package uk.gov.companieshouse.monitornotification.matcher.enumerationshelper;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDescriptionsEnumerationsHelperTest {
    private static final String FILING_HISTORY_DESCRIPTIONS_FILE_NAME = "api-enumerations/filing_history_descriptions.yml";
    private static final String DESCRIPTION_KEY = "appoint-person-director-company-with-name-date";
    private static final String DESCRIPTION_KEY_INVALID = "appoint-person-director-company-with-name";
    private static final String DESCRIPTION = "**Appointment** of {officer_name} as a director on {appointment_date}";
    private static final String PARAMETERISED_HISTORY_DESCRIPTION = "Appointment of DR AMIDAT DUPE IYIOLA as a director on 1 December 2024";
    private static final String DESCRIPTIONS_JSON_BLOB = "{\"description\" : \"appoint-person-director-company-with-name-date\",\"description_values\" : {\"appointment_date\" : \"1 December 2024\",\"officer_name\" : \"DR AMIDAT DUPE IYIOLA\"}}";
  
    @Mock
    private InputStream inputStream;

    @Mock
    private Yaml yaml;

    @Mock
    private FileHelper fileHelper;

    @InjectMocks
    private FilingHistoryDescriptionsEnumerationsHelper filingHistoryDescriptionsEnumerationsHelper;

    @BeforeEach
    public void setUp() {
    }

    @Test
    void testGetHistoryDescriptionOK() throws Exception {
        when(fileHelper.loadFile(FILING_HISTORY_DESCRIPTIONS_FILE_NAME)).thenReturn(inputStream);
        when(yaml.load(inputStream)).thenReturn(createFilingHistoryDescriptions(DESCRIPTION_KEY, DESCRIPTION));

        String result = filingHistoryDescriptionsEnumerationsHelper.getFilingHistoryDescription(DESCRIPTION_KEY, findDataNode("description_values"));
        assertEquals(PARAMETERISED_HISTORY_DESCRIPTION, result);
    }

    @Test
    void testGetHistoryDescriptionValuesIsNullOK() throws Exception {
        when(fileHelper.loadFile(FILING_HISTORY_DESCRIPTIONS_FILE_NAME)).thenReturn(inputStream);
        when(yaml.load(inputStream)).thenReturn(createFilingHistoryDescriptions(DESCRIPTION_KEY, DESCRIPTION));

        String result = filingHistoryDescriptionsEnumerationsHelper.getFilingHistoryDescription(DESCRIPTION_KEY_INVALID, findDataNode("description_values"));
        assertNull(result);
    }

    @Test
    void testGetHistoryDescriptionFileNotFoundError() throws Exception {
        when(fileHelper.loadFile(FILING_HISTORY_DESCRIPTIONS_FILE_NAME)).thenReturn(null);
        String result = filingHistoryDescriptionsEnumerationsHelper.getFilingHistoryDescription(DESCRIPTION_KEY, findDataNode("description_values"));
        assertNull(result);
    }

    private Map<String, Object> createFilingHistoryDescriptions(String key, String description) {
        Map<String, String> descriptionIdentifiers = new HashMap<>();
        descriptionIdentifiers.put(key, description);

        Map<String, Object> descriptions = new HashMap<>();
        descriptions.put(key, description);
         
        return descriptions;
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
