package uk.gov.companieshouse.monitornotification.matcher.service;

import monitor.filing;
import uk.gov.companieshouse.logging.Logger;

public class NotificationMatchService {

    private final Logger logger;

    public NotificationMatchService(Logger logger) {
        this.logger = logger;
    }

    public void processMessage(final filing message) {
        logger.trace("processMessage(message=%s) method called.".formatted(message));
    }
}
