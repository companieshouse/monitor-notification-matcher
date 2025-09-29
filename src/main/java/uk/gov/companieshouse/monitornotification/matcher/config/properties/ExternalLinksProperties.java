package uk.gov.companieshouse.monitornotification.matcher.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.external.links")
@Component
public class ExternalLinksProperties {

    private String chsUrl;
    private String monitorUrl;

    public String getMonitorUrl() {
        return monitorUrl;
    }

    public void setMonitorUrl(String monitorUrl) {
        this.monitorUrl = monitorUrl;
    }

    public String getChsUrl() {
        return chsUrl;
    }

    public void setChsUrl(String chsUrl) {
        this.chsUrl = chsUrl;
    }

}
