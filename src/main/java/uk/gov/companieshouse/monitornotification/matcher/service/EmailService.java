package uk.gov.companieshouse.monitornotification.matcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import email.email_send;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailSend;

@Service
public class EmailService {

    private final ObjectMapper mapper;
    private final Logger logger;

    public EmailService(final ObjectMapper mapper, final Logger logger) {
        this.mapper = mapper;
        this.logger = logger;
    }

    public void saveMatch(final EmailDocument<?> document, final String userId) {
        logger.trace("saveMatch(document=%s) method called.".formatted(document));
        try {
            var jsonData = mapper.writeValueAsString(document.getData());

            EmailSend email = new EmailSend();
            email.setAppId(document.getAppId());
            email.setMessageId(document.getMessageId());
            email.setMessageType(document.getMessageType());
            email.setData(jsonData);
            email.setCreatedAt(document.getCreatedAt());
            email.setUserId(userId);

            // Save the model to the mongo matches collection.

        } catch(JsonProcessingException ex) {
            logger.error("Failed to serialize email data: %s".formatted(ex.getMessage()));
            throw new NonRetryableException("Failed to serialize email data", ex);
        }
    }

    public void sendEmail(final EmailDocument<?> document) {
        logger.trace("sendEmail(document=%s) method called.".formatted(document));
        try {
            var jsonData = mapper.writeValueAsString(document.getData());

            email_send email = new email_send();
            email.setAppId(document.getAppId());
            email.setMessageId(document.getMessageId());
            email.setMessageType(document.getMessageType());
            email.setEmailAddress(document.getEmailAddress());
            email.setCreatedAt(document.getCreatedAt());
            email.setData(jsonData);

            // Send the email via the message-send producer.

        } catch(JsonProcessingException ex) {
            logger.error("Failed to serialize email data: %s".formatted(ex.getMessage()));
            throw new NonRetryableException("Failed to serialize email data", ex);
        }
    }

}
