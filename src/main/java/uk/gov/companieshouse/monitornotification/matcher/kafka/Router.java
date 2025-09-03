package uk.gov.companieshouse.monitornotification.matcher.kafka;

import uk.gov.companieshouse.delta.ChsDelta;

public interface Router {

    void route(ChsDelta delta);

}
