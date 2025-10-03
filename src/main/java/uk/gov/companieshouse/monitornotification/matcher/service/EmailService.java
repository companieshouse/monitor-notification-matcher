package uk.gov.companieshouse.monitornotification.matcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyDetailsGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.model.MessageSend;

@Service
public class EmailService {

    final Supplier<InternalApiClient> supplier;
    private final ObjectMapper mapper;
    private final Logger logger;

    public EmailService(final Supplier<InternalApiClient> supplier, final ObjectMapper mapper, final Logger logger) {
        this.supplier = supplier;
        this.mapper = mapper;
        this.logger = logger;
    }

    public void saveMatch(final EmailDocument<?> document, final String userId) {
        logger.trace("saveMatch(document=%s, userId=%s) method called.".formatted(document, userId));
    }

    public void sendEmail(final EmailDocument<?> document, final String userId) {
        logger.trace("sendEmail(document=%s, userId=%s) method called.".formatted(document, userId));
        try {
            InternalApiClient apiClient = supplier.get();
            apiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());

            apiClient.privateNotificationSender().sendEmail()
        } catch(ApiErrorResponseException | URIValidationException ex) {
            logger.error("An error occurred while attempting to POST to chs-kafka-api: %s".formatted(ex.getMessage()));
            throw new NonRetryableException("An error occurred while attempting to POST to chs-kafka-api: ", ex);
        }
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
