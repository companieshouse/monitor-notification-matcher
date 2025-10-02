package uk.gov.companieshouse.monitornotification.matcher.model;

public class EmailSend {

    private String appId;
    private String messageId;
    private String messageType;
    private String data;
    private String createdAt;
    private String userId;

    public EmailSend(String appId, String messageId, String messageType, String data, String createdAt, String userId) {
        this.appId = appId;
        this.messageId = messageId;
        this.messageType = messageType;
        this.data = data;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public EmailSend() {
        this(null, null, null, null, null, null);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
