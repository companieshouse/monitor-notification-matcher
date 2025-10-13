package uk.gov.companieshouse.monitornotification.matcher.filing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.FilingHistoryDescriptions;

/**
 * Helper for retrieving the history description and populating parameterised history descriptions.
 */
@Component
public class ApiEnumerationsHelper {

    private static final String EMPTY_STRING = "";

    private final FilingHistoryDescriptions descriptions;
    private final ObjectMapper mapper;
    private final Logger logger;

    public ApiEnumerationsHelper(final FilingHistoryDescriptions descriptions, final ObjectMapper mapper, final Logger logger) {
        this.descriptions = descriptions;
        this.mapper = mapper;
        this.logger = logger;
    }

    /**
     * Get parameterised filing history description string using api enumerations.
     * 
     * @param description The description key
     * @param descriptionValues The Optional<JsonNode> object containing the interpoliant descriptionValue(s)
     * @return parameterised history description
     */
    @SuppressWarnings("unchecked")
    public String getFilingHistoryDescription(final String description, final JsonNode descriptionValues) {
        logger.debug("getFilingHistoryDescription(description=%s) method called.".formatted(description));

        if(descriptions == null || descriptions.getDescriptions() == null || descriptions.getDescriptions().isEmpty()) {
            logger.error("No filing history descriptions available! (Returning: %s)".formatted(description));
            return null;
        }

        Map<String, String> filingHistoryDescriptions = descriptions.getDescriptions();

        if(!filingHistoryDescriptions.containsKey(description)) {
            logger.info("No filing history descriptions available for: %s".formatted(description));
            return null;
        }

        String descriptionValue = filingHistoryDescriptions.get(description);
        descriptionValue = descriptionValue.replaceAll("[*]", "");

        Map<String, Object> descriptionValuesMap = mapper.convertValue(descriptionValues, Map.class);

        return populateParameters(descriptionValue, descriptionValuesMap);
    }

    /**
     * Populate parameterised descriptions with the supplied parameters. The parameter
     * names are expected to be contained within curly braces e.g. {name}.
     * 
     * @param description Parameterised description of a document
     * @param parameters Key/value pairs to replace parameters within the description. The key is the parameter name.
     * @return Description with parameters populated
     */
    private String populateParameters(String description, Map<String, Object> parameters) {
        var sub = new StrSubstitutor(parameters, "{", "}");
        return sub.replace(description);
    }
}
