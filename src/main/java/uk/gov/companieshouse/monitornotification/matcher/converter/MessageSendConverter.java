package uk.gov.companieshouse.monitornotification.matcher.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.MessageSend;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.model.SendMessage;

@Component
public class MessageSendConverter implements Converter<SendMessage, MessageSend> {

    private final ObjectMapper mapper;
    private final Logger logger;

    public MessageSendConverter(final ObjectMapper mapper, final Logger logger) {
        this.mapper = mapper;
        this.logger = logger;
    }

    @Override
    public MessageSend convert(final SendMessage source) {
        logger.trace("convert(source=%s) method called.".formatted(source));
        MessageSend messageSend = new MessageSend();
        messageSend.setAppId(source.getAppId());
        messageSend.setMessageId(source.getMessageId());
        messageSend.setMessageType(source.getMessageType());
        try {
            var data = mapper.writeValueAsString(source.getData());
            messageSend.setData(data);

        } catch (JsonProcessingException ex) {
            logger.error("Error converting SendMessage to MessageSend: ", ex);
            throw new NonRetryableException("Error converting SendMessage to MessageSend", ex);
        }
        messageSend.setUserId(source.getUserId());
        messageSend.setCreatedAt(source.getCreatedAt());
        return messageSend;
    }
}
