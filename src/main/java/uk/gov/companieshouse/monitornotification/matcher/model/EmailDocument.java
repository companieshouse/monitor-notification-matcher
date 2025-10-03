package uk.gov.companieshouse.monitornotification.matcher.model;

import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class EmailDocument<T> {

    private final String appId;
    private final String messageId;
    private final String messageType;
    private final T data;
    private final String emailAddress;
    private final String createdAt;

    public EmailDocument(String appId, String messageId, String messageType, T data, String emailAddress, String createdAt) {
        this.appId = appId;
        this.messageId = messageId;
        this.messageType = messageType;
        this.data = data;
        this.emailAddress = emailAddress;
        this.createdAt = createdAt;
    }

    public String getAppId() {
        return appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public T getData() {
        return data;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public static <T> EmailDocumentBuilder<T> builder() {
        return new EmailDocumentBuilder<>();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EmailDocument<?> that = (EmailDocument<?>) o;
        return Objects.equals(getAppId(), that.getAppId()) && Objects
                .equals(getMessageId(), that.getMessageId()) && Objects
                .equals(getMessageType(), that.getMessageType()) && Objects
                .equals(getData(), that.getData()) && Objects
                .equals(getEmailAddress(), that.getEmailAddress()) && Objects
                .equals(getCreatedAt(), that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppId(), getMessageId(), getMessageType(), getData(), getEmailAddress(), getCreatedAt());
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

    public static class EmailDocumentBuilder<T> {

        private String appId;
        private String messageId;
        private String messageType;
        private T data;
        private String recipientAddress;
        private String createdAt;

        public EmailDocumentBuilder<T> withAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public EmailDocumentBuilder<T> withMessageId(String randomUuid) {
            this.messageId = randomUuid;
            return this;
        }

        public EmailDocumentBuilder<T> withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public EmailDocumentBuilder<T> withData(T emailData) {
            this.data = emailData;
            return this;
        }

        public EmailDocumentBuilder<T> withRecipientEmailAddress(String recipientEmailAddress) {
            this.recipientAddress = recipientEmailAddress;
            return this;
        }

        public EmailDocumentBuilder<T> withCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EmailDocument<T> build() {
            return new EmailDocument<>(appId, messageId, messageType, data, recipientAddress, createdAt);
        }
    }
}