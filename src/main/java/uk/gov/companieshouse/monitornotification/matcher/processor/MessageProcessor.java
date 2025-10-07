package uk.gov.companieshouse.monitornotification.matcher.processor;

import static java.lang.Boolean.FALSE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import monitor.filing;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.ExternalLinksProperties;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.model.FilingHistory;
import uk.gov.companieshouse.monitornotification.matcher.model.SendMessageData;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;
import uk.gov.companieshouse.monitornotification.matcher.service.EmailService;

@Component
public class MessageProcessor {

    private final EmailService emailService;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;
    private final Logger logger;
    private final ExternalLinksProperties properties;

    public MessageProcessor(final EmailService emailService, final CompanyService companyService,
            final ObjectMapper objectMapper, final Logger logger, final ExternalLinksProperties properties) {
        this.emailService = emailService;
        this.companyService = companyService;
        this.objectMapper = objectMapper;
        this.logger = logger;
        this.properties = properties;
    }

    public void processMessage(final filing message) {
        logger.trace("processMessage(message=%s) method called. ".formatted(message));

        // Extract the Company ID from the message supplied.
        Optional<String> companyNumber = getCompanyNumber(message);
        if (companyNumber.isEmpty() || companyNumber.get().isBlank()) {
            logger.info("No company number was detected within the notification match payload. Processing aborted!");
            return;
        }

        // Lookup the Company Details using the Company ID extracted.
        Optional<CompanyDetails> companyDetails = companyService.findCompanyDetails(companyNumber.get());
        if (companyDetails.isEmpty()) {
            logger.info("No company details were found with company number: [%s]. Processing aborted!".formatted(companyNumber.get()));
            return;
        }

        FilingHistory filingHistory = createFilingHistory(message);
        logger.debug("Filing history created: %s".formatted(filingHistory));

        // Prepare the email document using the payload and company details.
        EmailDocument<SendMessageData> emailDocument = createEmailDocument(message, companyDetails.get(), filingHistory);

        // Save the email request (document) to the repository.
        emailService.saveMatch(emailDocument, message.getUserId());

        // Send the email document to the email service for processing.
        ApiResponse<Void> apiResponse = emailService.sendEmail(emailDocument, message.getUserId());
        logger.error("Message sent to CHS Kafka API successfully: (Status Code: %d)".formatted(apiResponse.getStatusCode()));

    }

    private Optional<String> getCompanyNumber(final filing message) {
        logger.trace("getCompanyNumber(message=%s) method called.".formatted(message));

        Optional<JsonNode> companyNumber = getOptionalNodeValue(findDataNode(message), "company_number");
        return companyNumber.map(JsonNode::asText);
    }

    private Boolean isDelete(final filing message) {
        logger.trace("isDelete(message=%s) method called.".formatted(message));

        Optional<JsonNode> isDelete = getOptionalNodeValue(findDataNode(message), "is_delete");
        return isDelete.map(JsonNode::asBoolean).orElse(FALSE);
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

    private Optional<JsonNode> getOptionalNodeValue(final JsonNode node, final String attribute) {
        logger.trace("getOptionalNodeValue(node=%s, nodeName=%s) method called.".formatted(node, attribute));

        if(node == null || !node.has(attribute)) {
            logger.debug("The given node does not contain a valid '%s' attribute!".formatted(attribute));
            return Optional.empty();
        }

        return Optional.ofNullable(node.get(attribute));
    }

    private JsonNode getMandatoryNodeValue(final JsonNode node, final String attribute) throws IllegalArgumentException {
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
            return objectMapper.readTree(message.getData());

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while attempting to extract the JsonNode: %s".formatted("data"), e);
            throw new NonRetryableException("An error occurred while attempting to extract the JsonNode: %s".formatted("data"), e);
        }
    }

    private FilingHistory createFilingHistory(final filing message) {
        logger.trace("createFilingHistory(message=%s) method called.".formatted(message));

        String type = getFilingType(message);
        String description = getFilingDescription(message);
        String date = getFilingDate(message);

        return new FilingHistory(type, description, date);
    }

    private EmailDocument<SendMessageData> createEmailDocument(final filing message, final CompanyDetails details, final FilingHistory history) {
        logger.trace("createEmailDocument(message=%s, details=%s, history=%s) method called."
                .formatted(message, details, history));

        // Maintain the correlation ID if one exists, otherwise generate a new one.
        var messageId = Optional.ofNullable(DataMapHolder.getRequestId()).orElse(UUID.randomUUID().toString());

        // Prepare the email data payload.
        SendMessageData data = new SendMessageData();
        data.setCompanyNumber(details.getCompanyNumber());
        //data.setCompanyName(details.getCompanyName());
        data.setFilingDate(history.getDate());
        data.setFilingDescription(history.getDescription());
        data.setFilingType(history.getType());
        data.setDelete(isDelete(message));
        data.setMonitorUrl(properties.getMonitorUrl());
        //data.setChsUrl(properties.getChsUrl());
        data.setFrom("Companies House <noreply@companieshouse.gov.uk>");
        data.setSubject("Company number %s %s".formatted(details.getCompanyNumber(), details.getCompanyName()));

        return EmailDocument.<SendMessageData>builder()
                .withAppId("monitor-notification-matcher.filing")
                .withMessageId(messageId)
                .withMessageType("monitor_email")
                .withCreatedAt(message.getNotifiedAt())
                .withRecipientEmailAddress(null)
                .withData(data)
                .build();
    }
}