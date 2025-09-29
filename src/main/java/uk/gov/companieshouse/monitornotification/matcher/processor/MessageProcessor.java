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
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.ExternalLinksProperties;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;
import uk.gov.companieshouse.monitornotification.matcher.service.EmailService;

@Component
public class MessageProcessor {

    private final EmailService emailService;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;
    private final Logger logger;
    private final ExternalLinksProperties properties

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

        if (companyNumber.isEmpty()) {
            logger.info("No company number was detected within the notification match payload. Unable to continue.");
            return;
        }

        // Lookup the Company Details using the Company ID extracted.
        String companyName = companyService.findCompanyDetails(companyNumber.get());
        logger.debug("Company details found: %s".formatted(companyName));

        // Send the email using the payload and company details.
        EmailDocument<?> emailDocument = createEmailDocument(message, companyName, companyNumber.get());
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
            logger.info("The message does not contain a valid is_delete field.");
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
            return Optional.empty();
        }
    }

    private EmailDocument<?> createEmailDocument(final filing message, final String companyName, final String companyNumber) {
        logger.trace("createEmailDocument(message=%s, companyName=%s, companyNumber=%s) method called."
                .formatted(message, companyName, companyNumber));

        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put("CompanyName", companyName);
        dataMap.put("CompanyNumber", companyNumber);
        dataMap.put("IsDelete", isDeleteRequest(message));
        dataMap.put("MonitorURL", properties.getMonitorUrl());
        dataMap.put("ChsURL", properties.getChsUrl());
        dataMap.put("from", "Companies House <noreply@companieshouse.gov.uk>");
        dataMap.put("subject", format("Company number %s %s", companyNumber, companyName));

        return EmailDocument.<Map<String, Object>>builder()
                .withAppId("chs-monitor-notification-matcher.filing")
                .withMessageId(UUID.randomUUID().toString())
                .withMessageType("monitor_email")
                .withCreatedAt(message.getNotifiedAt())
                .withRecipientEmailAddress("")
                .withData(dataMap)
                .build();
    }
}