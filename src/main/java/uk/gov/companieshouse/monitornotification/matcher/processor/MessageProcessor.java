package uk.gov.companieshouse.monitornotification.matcher.processor;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import monitor.filing;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.ExternalLinksProperties;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
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

        logger.debug("Company details found: %s".formatted(companyDetails.get()));

        // Prepare the email document using the payload and company details.
        EmailDocument<?> emailDocument = createEmailDocument(message, companyDetails.get());

        // Save the email request (document) to the repository.
        emailService.saveMatch(emailDocument, message.getUserId());

        // Send the email document to the email service for processing.
        emailService.sendEmail(emailDocument);

    }

    private Optional<String> getCompanyNumber(final filing message) {
        logger.trace("getCompanyNumber(message=%s) method called.".formatted(message));

        Optional<JsonNode> node = findDataNode(message, "company_number");
        if(node.isEmpty()) {
            logger.info("The message does not contain a valid company_number field.");
            return Optional.empty();
        }
        return Optional.ofNullable(node.get().asText());
    }

    private Boolean isDeleteRequest(final filing message) {
        logger.trace("isDeleteRequest(message=%s) method called.".formatted(message));

        Optional<JsonNode> node = findDataNode(message, "is_delete");
        if(node.isEmpty()) {
            logger.info("The message does not contain a valid is_delete field (defaulting to FALSE).");
            return FALSE;
        }
        return node.get().asBoolean(FALSE);
    }

    private Optional<JsonNode> findDataNode(final filing message, final String nodeName) {
        logger.trace("findDataNode(nodeName=%s) method called.".formatted(nodeName));
        try {
            JsonNode root = objectMapper.readTree(message.getData());
            JsonNode node = root.get(nodeName);

            logger.debug("Search for Node(%s), returned result: %s".formatted(nodeName, node));

            return Optional.ofNullable(node);

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while attempting to extract the JsonNode: %s".formatted(nodeName), e);
            throw new NonRetryableException("An error occurred while attempting to extract the JsonNode: %s".formatted(nodeName), e);
        }
    }

    private EmailDocument<?> createEmailDocument(final filing message, final CompanyDetails details) {
        logger.trace("createEmailDocument(message=%s, details=%s) method called.".formatted(message, details));

        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put("CompanyName", details.getCompanyName());
        dataMap.put("CompanyNumber", details.getCompanyNumber());
        dataMap.put("IsDelete", isDeleteRequest(message));
        dataMap.put("MonitorURL", properties.getMonitorUrl());
        dataMap.put("ChsURL", properties.getChsUrl());
        dataMap.put("from", "Companies House <noreply@companieshouse.gov.uk>");
        dataMap.put("subject", format("Company number %s %s", details.getCompanyNumber(), details.getCompanyName()));

        return EmailDocument.<Map<String, Object>>builder()
                .withAppId("monitor-notification-matcher.filing")
                .withMessageId(UUID.randomUUID().toString())
                .withMessageType("monitor_email")
                .withCreatedAt(message.getNotifiedAt())
                .withRecipientEmailAddress(null)
                .withData(dataMap)
                .build();
    }
}