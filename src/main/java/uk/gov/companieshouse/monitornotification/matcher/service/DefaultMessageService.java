package uk.gov.companieshouse.monitornotification.matcher.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.kafka.Router;

/**
 * Default message consumer to allow the build to compile. We need at least one qualifying bean with the Router
 * interface in order for the build to compile, but this can be removed later on.
 */
@Service
public class DefaultMessageService implements Router {

    private final Logger logger;

    public DefaultMessageService(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void route(final ChsDelta delta) {
        logger.debug(String.format("route(delta=%s) method called.", delta));

        // Message processing goes here.
    }
}
