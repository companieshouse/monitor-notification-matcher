package uk.gov.companieshouse.monitornotification.matcher.processor;

import java.util.Map;
import java.util.Optional;
import monitor.filing;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.MessageSend;
import uk.gov.companieshouse.api.chskafka.MessageSendData;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.ExternalLinksProperties;
import uk.gov.companieshouse.monitornotification.matcher.filing.FilingHistoryDescriptionConverter;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
import uk.gov.companieshouse.monitornotification.matcher.model.FilingHistory;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;
import uk.gov.companieshouse.monitornotification.matcher.service.EmailService;
import uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchDataExtractor;

@Component
public class MessageProcessor {

    private static final String LEGACY_FILING_HISTORY_DESCRIPTION = "legacy";

    private final EmailService emailService;
    private final CompanyService companyService;
    private final Logger logger;
    private final ExternalLinksProperties properties;
    private final FilingHistoryDescriptionConverter converter;
    private final NotificationMatchDataExtractor extractor;

    public MessageProcessor(final EmailService emailService, final CompanyService companyService, final Logger logger,
            final ExternalLinksProperties properties,
            final FilingHistoryDescriptionConverter converter,
            final NotificationMatchDataExtractor extractor) {
        this.emailService = emailService;
        this.companyService = companyService;
        this.logger = logger;
        this.properties = properties;
        this.converter = converter;
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

        // Extract the filing history details from the message supplied.
        var filingHistory = extractor.getFilingHistory(message);
        logger.debug("Filing history created: %s".formatted(filingHistory));

        // We need to convert the filing description if it is parameterised.
        var descriptionValues = extractor.getDescriptionValues(message);
        var descriptionKey = filingHistory.getDescription();

        filingHistory.setDescription(convertFilingDescription(descriptionKey, descriptionValues));

        // Prepare the email document using the payload and company details.
        var messageSend = createMessageSend(message, companyDetails.get(), filingHistory);

        // Save the email request (document) to the repository.
        emailService.saveMatch(messageSend);

        // Send the email document to the email service for processing.
        ApiResponse<Void> apiResponse = emailService.sendEmail(messageSend);
        logger.info("Message sent to CHS Kafka API successfully: (Status Code: %d)".formatted(apiResponse.getStatusCode()));

    }

    private String convertFilingDescription(final String descriptionKey, final Map<String, String> descriptionValues) {
        logger.trace("convertFilingDescription(description=%s, descriptionValues=%s) method called."
                .formatted(descriptionKey, descriptionValues));

        // Check if we are dealing with a "legacy" description, which means we do not translate it.
        if(StringUtils.equals(LEGACY_FILING_HISTORY_DESCRIPTION, descriptionKey) && descriptionValues.containsKey("description")) {
            logger.info("Legacy filing history description detected, using description value supplied.");
            return descriptionValues.get("description");
        }

        // We have description values, and a description key, so attempt to get the full description.
        return converter.getFilingHistoryDescription(descriptionKey, descriptionValues);

    }

    private MessageSend createMessageSend(final filing payload, final CompanyDetails details, final FilingHistory history) {
        logger.trace("createMessageSend(payload=%s, details=%s, history=%s) method called."
                .formatted(payload, details, history));

        var message = new MessageSend();
        message.setAppId("monitor-notification-matcher.filing");
        message.setMessageId(DataMapHolder.getRequestId());
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