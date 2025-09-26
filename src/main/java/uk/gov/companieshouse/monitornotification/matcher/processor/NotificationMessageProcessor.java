package uk.gov.companieshouse.monitornotification.matcher.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import monitor.filing;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;
import uk.gov.companieshouse.monitornotification.matcher.service.EmailService;

@Component
public class NotificationMessageProcessor {

    private final EmailService emailService;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;
    private final Logger logger;

    public NotificationMessageProcessor(final EmailService emailService, final CompanyService companyService,
            final ObjectMapper objectMapper, final Logger logger) {
        this.emailService = emailService;
        this.companyService = companyService;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    public void processMessage(final filing message) {
        logger.trace("processMessage(message=%s) method called. ".formatted(message));

        Optional<String> companyId = getCompanyId(message);
        if (companyId.isEmpty()) {
            logger.info("No company number was detected within the notification match payload. Unable to send email.");
            return;
        }

        String companyName = companyService.findCompanyNameById(companyId.get());
    }

    private Optional<String> getCompanyId(final filing message) {
        logger.trace("getCompanyId() method called.");
        try {
            JsonNode root = objectMapper.readTree(message.getData());
            JsonNode transactionNode = root.get("company_number");

            logger.debug("Company ID extracted from message: %s".formatted(
                    transactionNode != null ? transactionNode.asText() : "null"));

            return Optional.ofNullable(transactionNode).map(JsonNode::asText);

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while attempting to extract the TransactionID:", e);
            //throw new NonRetryableException("Error extracting transaction ID from message", e);
            return Optional.empty();
        }
    }

}