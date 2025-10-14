package uk.gov.companieshouse.monitornotification.matcher.filing;

import java.util.Map;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.FilingHistoryDescriptions;

/**
 * Helper for retrieving the history description and populating parameterised history descriptions.
 */
@Component
public class FilingHistoryDescriptionConverter {

    private final FilingHistoryDescriptions descriptions;
    private final Logger logger;

    public FilingHistoryDescriptionConverter(final FilingHistoryDescriptions descriptions, final Logger logger) {
        this.descriptions = descriptions;
        this.logger = logger;
    }

    /**
     * Get parameterised filing history description string using api enumerations.
     * 
     * @param descriptionKey The description key
     * @param descriptionValues The Optional<JsonNode> object containing the interpoliant descriptionValue(s)
     * @return parameterised history description
     */
    public String getFilingHistoryDescription(final String descriptionKey, final Map<String, String> descriptionValues) {
        logger.trace("getFilingHistoryDescription(descriptionKey=%s, descriptionValues=%s) method called."
                .formatted(descriptionKey, descriptionValues));

        if(descriptionKey == null || descriptionKey.isEmpty()) {
            logger.error("The description key provided was not valid: %s".formatted(descriptionKey));
            return null;
        }

        String descriptionValue = descriptions.getDescriptions().getOrDefault(descriptionKey, null);
        if(descriptionValue == null) {
            logger.info("No filing history descriptions available for: %s".formatted(descriptionKey));
            return descriptionValue;
        }

        descriptionValue = descriptionValue.replaceAll("[*]", "");

        return populateParameters(descriptionValue, descriptionValues);
    }

    /**
     * Populate parameterised descriptions with the supplied parameters. The parameter
     * names are expected to be contained within curly braces e.g. {name}.
     * 
     * @param description Parameterised description of a document
     * @param parameters Key/value pairs to replace parameters within the description. The key is the parameter name.
     * @return Description with parameters populated
     */
    private String populateParameters(String description, Map<String, String> parameters) {
        var sub = new StrSubstitutor(parameters, "{", "}");
        return sub.replace(description);
    }
}
