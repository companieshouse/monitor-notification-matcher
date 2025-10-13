package uk.gov.companieshouse.monitornotification.matcher.config.properties;

import java.util.Map;

public class FilingHistoryDescriptions {

    private final Map<String, String> descriptions;

    public FilingHistoryDescriptions(final Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }

    public Map<String, String> getDescriptions() {
        return this.descriptions;
    }

}
