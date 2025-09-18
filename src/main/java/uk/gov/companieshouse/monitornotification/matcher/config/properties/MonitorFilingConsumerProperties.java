package uk.gov.companieshouse.monitornotification.matcher.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.kafka.consumer.filing")
@Component
public class MonitorFilingConsumerProperties {

    private String topic;
    private String groupId;
    private int maxAttempts;
    private long backOffDelay;
    private int concurrency;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getBackOffDelay() {
        return backOffDelay;
    }

    public void setBackOffDelay(long backOffDelay) {
        this.backOffDelay = backOffDelay;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }
}

