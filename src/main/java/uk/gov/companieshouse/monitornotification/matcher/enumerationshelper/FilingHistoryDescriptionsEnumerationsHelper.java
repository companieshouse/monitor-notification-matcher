package uk.gov.companieshouse.monitornotification.matcher.enumerationshelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Helper for retrieving the history description and populating parameterised history descriptions.
 */
@Component
public class FilingHistoryDescriptionsEnumerationsHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger("enumerationshelper");

    private static final String FILING_HISTORY_DESCRIPTIONS_FILE_NAME = "api-enumerations/filing_history_descriptions.yml";

    @Autowired
    private Yaml yaml;

    @Autowired
    private FileHelper fileHelper;

    /**
     * Get parameterised filing history description string using api enumerations.
     * 
     * @param description The description key
     * @param descriptionValues The Optional<JsonNode> object containing the interpoliant descriptionValue(s)
     * @return parameterised history description
     * @throws IOException Exception thrown if an error occurs when accessing the file containing the descriptions
     */
    @SuppressWarnings("unchecked")
    public String getFilingHistoryDescription(String description, JsonNode descriptionValues) throws IOException {
        var inputStream = fileHelper.loadFile(FILING_HISTORY_DESCRIPTIONS_FILE_NAME);
        if(inputStream != null) {
            Map<String, Object> filingHistoryDescriptions = (Map<String, Object>)yaml.load(inputStream);
            if(filingHistoryDescriptions != null) {
                if(filingHistoryDescriptions.containsKey(description)) {
                    String descriptionValue = (String)filingHistoryDescriptions.get(description);
                    descriptionValue = descriptionValue.replaceAll("[*]", "");
                    var objectMapper = new ObjectMapper();
                    Map<String, Object> descriptionValuesMap = objectMapper.convertValue(descriptionValues, Map.class);
                    return populateParameters(descriptionValue, descriptionValuesMap);
                } else {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("file", FILING_HISTORY_DESCRIPTIONS_FILE_NAME);
                    dataMap.put("key", description);
                    LOG.trace("Value not found in file history descriptions", dataMap);
                }
            }
        } else {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("file", FILING_HISTORY_DESCRIPTIONS_FILE_NAME);
            LOG.trace("File history descriptions not found", dataMap);
        }

        return null;
    }

    /**
     * Populate parameterised descriptions with the supplied parameters. The parameter
     * names are expected to be contained within curly braces e.g. {name}.
     * 
     * @param description Parameterised description of a document
     * @param parameters Key/value pairs to replace parameters within the description. The key is the parameter name.
     * @return description Description with parameters populated
     */
    private String populateParameters(String description, Map<String, Object> parameters) {
        var sub = new StrSubstitutor(parameters, "{", "}");
        return sub.replace(description);
    }
}
