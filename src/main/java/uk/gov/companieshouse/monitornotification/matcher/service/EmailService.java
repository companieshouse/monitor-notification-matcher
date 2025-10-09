package uk.gov.companieshouse.monitornotification.matcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.MessageSend;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
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

    public void saveMatch(final MessageSend message) {
        logger.trace("saveMatch(message=%s) method called.".formatted(message));
        try {
            var jsonData = mapper.writeValueAsString(message.getData());

            var document = new MonitorMatchDocument();
            document.setAppId(message.getAppId());
            document.setMessageId(message.getMessageId());
            document.setMessageType(message.getMessageType());
            document.setData(jsonData);
            document.setCreatedAt(message.getCreatedAt());
            document.setUserId(document.getUserId());

            // Save the model to the mongo matches collection.
            repository.save(document);

        } catch(JsonProcessingException ex) {
            logger.error("Failed to serialize email data: %s".formatted(ex.getMessage()));
            throw new NonRetryableException("Failed to serialize email data", ex);
        }
    }

    public ApiResponse<Void> sendEmail(final MessageSend message) {
        logger.trace("sendEmail(message=%s) method called.".formatted(message));
        try {
            var requestId = Optional.ofNullable(DataMapHolder.getRequestId()).orElse(UUID.randomUUID().toString());

            var apiClient = supplier.get();
            apiClient.getHttpClient().setRequestId(requestId);

            var messageHandler = apiClient.messageSendHandler();
            var messagePost = messageHandler.postMessageSend("/message-send", message);

            ApiResponse<Void> response = messagePost.execute();

            logger.info(String.format("Posted '%s' message to CHS Kafka API (RequestId: %s): (Response %d)",
                    message.getMessageType(), apiClient.getHttpClient().getRequestId(), response.getStatusCode()));

            return response;

        } catch (ApiErrorResponseException ex) {
            logger.error("An error occurred while attempting to POST to CHS Kafka API: %s".formatted(ex.getMessage()));
            throw new NonRetryableException(ex.getMessage(), ex);
        }
    }
}
