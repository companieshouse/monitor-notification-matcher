package uk.gov.companieshouse.monitornotification.matcher.utils;

import static java.lang.Boolean.FALSE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import monitor.filing;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.model.FilingHistory;

@Component
public class NotificationMatchDataExtractor {

    private final ObjectMapper mapper;
    private final Logger logger;

    public NotificationMatchDataExtractor(final ObjectMapper mapper, final Logger logger) {
        this.mapper = mapper;
        this.logger = logger;
    }

    public Optional<String> getCompanyNumber(final filing message) {
        logger.trace("getCompanyNumber(message=%s) method called.".formatted(message));

        Optional<JsonNode> companyNumber = getOptionalNodeValue(findDataNode(message), "company_number");
        return companyNumber.map(JsonNode::asText);
    }

    public Boolean isDelete(final filing message) {
        logger.trace("isDelete(message=%s) method called.".formatted(message));

        Optional<JsonNode> isDelete = getOptionalNodeValue(findDataNode(message), "is_delete");
        return isDelete.map(JsonNode::asBoolean).orElse(FALSE);
    }

    public FilingHistory getFilingHistory(final filing message) {
        logger.trace("createFilingHistory(message=%s) method called.".formatted(message));

        String type = getFilingType(message);
        String description = getFilingDescription(message);
        String date = getFilingDate(message);

        return new FilingHistory(type, description, date);
    }

    private String getFilingType(final filing message) {
        logger.trace("getFilingType(message=%s) method called.".formatted(message));

        JsonNode filingType = getMandatoryNodeValue(findNestedDataNode(message), "type");
        return filingType.asText();
    }

    private String getFilingDescription(final filing message) {
        logger.trace("getFilingDescription(message=%s) method called.".formatted(message));

        JsonNode filingType = getMandatoryNodeValue(findNestedDataNode(message), "description");
        return filingType.asText();
    }

    private String getFilingDate(final filing message) {
        logger.trace("getFilingDescription(message=%s) method called.".formatted(message));

        JsonNode filingType = getMandatoryNodeValue(findNestedDataNode(message), "date");
        return filingType.asText();
    }

    public Optional<JsonNode> getOptionalNodeValue(final JsonNode node, final String attribute) {
        logger.trace("getOptionalNodeValue(node=%s, nodeName=%s) method called.".formatted(node, attribute));

        if(node == null || !node.has(attribute)) {
            logger.debug("The given node does not contain a valid '%s' attribute!".formatted(attribute));
            return Optional.empty();
        }

        return Optional.ofNullable(node.get(attribute));
    }

    public JsonNode getMandatoryNodeValue(final JsonNode node, final String attribute) throws IllegalArgumentException {
        logger.trace("getMandatoryNodeValue(node=%s, nodeName=%s) method called.".formatted(node, attribute));

        if(node == null || !node.has(attribute)) {
            logger.info("The given node does not contain a valid '%s' node!".formatted(attribute));
            throw new IllegalArgumentException("Supplied node does not contain a valid '%s' node!".formatted(attribute));
        }

        return node.get(attribute);
    }

    private JsonNode findNestedDataNode(final filing message) {
        logger.trace("findNestedDataNode(message=%s) method called.".formatted(message));
        try {
            JsonNode dataNode = findDataNode(message).get("data");

            if(dataNode == null || dataNode.isEmpty()) {
                logger.debug("No nested 'data' node found in message payload, result was: %s".formatted(dataNode));
                throw new IllegalArgumentException("No nested 'data' node found in message payload!");
            }

            return dataNode;

        } catch (IllegalArgumentException e) {
            logger.error("An error occurred while attempting to extract the JsonNode: %s".formatted("data"), e);
            throw new NonRetryableException("An error occurred while attempting to extract the JsonNode: %s".formatted("data"), e);
        }
    }

    private JsonNode findDataNode(final filing message) {
        logger.trace("findDataNode(message=%s) method called.".formatted(message));
        try {
            return mapper.readTree(message.getData());

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while attempting to extract the JsonNode: %s".formatted("data"), e);
            throw new NonRetryableException("An error occurred while attempting to extract the JsonNode: %s".formatted("data"), e);
        }
    }

}
