package uk.gov.companieshouse.monitornotification.matcher.processor;

import java.util.Optional;
import monitor.filing;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.MessageSend;
import uk.gov.companieshouse.api.chskafka.MessageSendData;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.ExternalLinksProperties;
import uk.gov.companieshouse.monitornotification.matcher.model.FilingHistory;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;
import uk.gov.companieshouse.monitornotification.matcher.service.EmailService;
import uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchDataExtractor;

@Component
public class MessageProcessor {

    private final EmailService emailService;
    private final CompanyService companyService;
    private final Logger logger;
    private final ExternalLinksProperties properties;
    private final NotificationMatchDataExtractor extractor;

    public MessageProcessor(final EmailService emailService, final CompanyService companyService,
            final Logger logger, final ExternalLinksProperties properties, final NotificationMatchDataExtractor extractor) {
        this.emailService = emailService;
        this.companyService = companyService;
        this.logger = logger;
        this.properties = properties;
        this.extractor = extractor;
    }

    public void processMessage(final filing message) {
        logger.trace("processMessage(message=%s) method called. ".formatted(message));

        // Extract the Company ID from the message supplied.
        Optional<String> companyNumber = extractor.getCompanyNumber(message);
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

        var filingHistory = extractor.getFilingHistory(message);
        logger.debug("Filing history created: %s".formatted(filingHistory));

        // Prepare the email document using the payload and company details.
        var messageSend = createMessageSend(message, companyDetails.get(), filingHistory);

        // Save the email request (document) to the repository.
        emailService.saveMatch(messageSend);

        // Send the email document to the email service for processing.
        ApiResponse<Void> apiResponse = emailService.sendEmail(messageSend);
        logger.info("Message sent to CHS Kafka API successfully: (Status Code: %d)".formatted(apiResponse.getStatusCode()));

    }

    private MessageSend createMessageSend(final filing payload, final CompanyDetails details, final FilingHistory history) {
        logger.trace("createMessageSend(payload=%s, details=%s, history=%s) method called."
                .formatted(payload, details, history));

        var message = new MessageSend();
        message.setAppId("monitor-notification-matcher.filing");
        message.setMessageId("");
        message.setMessageType("monitor_email");

        var data = new MessageSendData();
        data.setCompanyNumber(details.getCompanyNumber());
        data.setCompanyName(details.getCompanyName());
        data.setFilingDate(history.getDate());
        data.setFilingDescription(history.getDescription());
        data.setFilingType(history.getType());
        data.setIsDelete(extractor.isDelete(payload));
        data.setChsURL(properties.getChsUrl());
        data.setMonitorURL(properties.getMonitorUrl());
        data.setFrom("Companies House <noreply@companieshouse.gov.uk>");
        data.setSubject("Company number %s %s".formatted(details.getCompanyNumber(), details.getCompanyName()));

        message.setData(data);
        message.setUserId(payload.getUserId());
        message.setCreatedAt(payload.getNotifiedAt());

        return message;
    }
}