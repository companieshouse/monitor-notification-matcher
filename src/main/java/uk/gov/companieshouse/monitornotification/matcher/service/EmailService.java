package uk.gov.companieshouse.monitornotification.matcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.converter.MessageSendConverter;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.model.SendMessage;
import uk.gov.companieshouse.monitornotification.matcher.model.SendMessageData;
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

    public ApiResponse<Void> sendEmail(final EmailDocument<SendMessageData> document, final String userId) {
        logger.trace("sendEmail(document=%s, userId=%s) method called.".formatted(document, userId));
        try {
            Supplier<SendMessage> sendMessageSupplier = createMessage(document, userId);
            SendMessage sendMessage = sendMessageSupplier.get();

            var requestId = Optional.ofNullable(DataMapHolder.getRequestId()).orElse(UUID.randomUUID().toString());

            var apiClient = supplier.get();
            apiClient.getHttpClient().setRequestId(requestId);

            var converter = new MessageSendConverter(mapper, logger);
            var messageSend = converter.convert(sendMessage);

            var messageHandler = apiClient.messageSendHandler();
            var messagePost = messageHandler.postMessageSend("/message-send", messageSend);

            ApiResponse<Void> response = messagePost.execute();

            logger.info(String.format("Posted '%s' message to CHS Kafka API (RequestId: %s): (Response %d)",
                    messageSend.getMessageType(), apiClient.getHttpClient().getRequestId(), response.getStatusCode()));

            return response;

        } catch (ApiErrorResponseException ex) {
            logger.error("An error occurred while attempting to POST to CHS Kafka API: %s".formatted(ex.getMessage()));
            throw new NonRetryableException(ex.getMessage(), ex);
        }
    }

    private Supplier<SendMessage> createMessage(final EmailDocument<SendMessageData> document, final String userId) {
        logger.trace("createMessage(document=%s, userId=%s) method called.".formatted(document, userId));

        return () -> {
            SendMessage message = new SendMessage();
            message.setAppId(document.getAppId());
            message.setMessageId(document.getMessageId());
            message.setMessageType(document.getMessageType());
            message.setData(document.getData());
            message.setCreatedAt(document.getCreatedAt());
            message.setUserId(userId);
            return message;
        };
    }

}
