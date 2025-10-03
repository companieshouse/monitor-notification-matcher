package uk.gov.companieshouse.monitornotification.matcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.model.MessageSend;
import uk.gov.companieshouse.monitornotification.matcher.repository.MonitorMatchesRepository;
import uk.gov.companieshouse.monitornotification.matcher.repository.model.MonitorMatchDocument;

@Service
public class EmailService {

    private final Supplier<InternalApiClient> supplier;
    private final MonitorMatchesRepository repository;
    private final ObjectMapper mapper;
    private final Logger logger;

    public EmailService(final Supplier<InternalApiClient> supplier, final MonitorMatchesRepository repository,
            final ObjectMapper mapper, final Logger logger) {
        this.supplier = supplier;
        this.repository = repository;
        this.mapper = mapper;
        this.logger = logger;
    }

    public void saveMatch(final EmailDocument<?> document, final String userId) {
        logger.trace("saveMatch(document=%s, userId=%s) method called.".formatted(document, userId));
        try {
            var jsonData = mapper.writeValueAsString(document.getData());

            MonitorMatchDocument email = new MonitorMatchDocument();
            email.setAppId(document.getAppId());
            email.setMessageId(document.getMessageId());
            email.setMessageType(document.getMessageType());
            email.setData(jsonData);
            email.setCreatedAt(document.getCreatedAt());
            email.setUserId(userId);

            // Save the model to the mongo matches collection.
            repository.save(email);

        } catch(JsonProcessingException ex) {
            logger.error("Failed to serialize email data: %s".formatted(ex.getMessage()));
            throw new NonRetryableException("Failed to serialize email data", ex);
        }
    }

    public void sendEmail(final EmailDocument<?> document, final String userId) {
        logger.trace("sendEmail(document=%s, userId=%s) method called.".formatted(document, userId));

        Supplier<MessageSend> messageSend = createMessageSend(document, userId);
    }

    private Supplier<MessageSend> createMessageSend(final EmailDocument<?> document, final String userId) {
        logger.trace("createMessageSendPayload(document=%s, userId=%s) method called.".formatted(document, userId));
        try {
            var jsonData = mapper.writeValueAsString(document.getData());

            return () -> {
                MessageSend email = new MessageSend();
                email.setAppId(document.getAppId());
                email.setMessageId(document.getMessageId());
                email.setMessageType(document.getMessageType());
                email.setData(jsonData);
                email.setCreatedAt(document.getCreatedAt());
                email.setUserId(userId);
                return email;
            };

        } catch(JsonProcessingException ex) {
            logger.error("Failed to serialize email data: %s".formatted(ex.getMessage()));
            throw new NonRetryableException("Failed to serialize email data", ex);
        }
    }
}
