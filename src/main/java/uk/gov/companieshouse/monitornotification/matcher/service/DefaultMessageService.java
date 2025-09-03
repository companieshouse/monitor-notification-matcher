package uk.gov.companieshouse.monitornotification.matcher.service;

import static uk.gov.companieshouse.monitornotification.matcher.Application.NAMESPACE;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.kafka.Router;

/**
 * Default message consumer to allow the build to compile. We need at least one qualifying bean with the Router
 * interface in order for the build to compile, but this can be removed later on.
 */
@Service
public class DefaultMessageService implements Router {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public void route(final ChsDelta delta) {
        LOGGER.debug(String.format("route(delta=%s) method called.", delta));

        // Message processing goes here.
    }
}
