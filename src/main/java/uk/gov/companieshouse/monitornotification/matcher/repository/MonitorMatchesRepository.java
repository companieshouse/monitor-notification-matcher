package uk.gov.companieshouse.monitornotification.matcher.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import uk.gov.companieshouse.monitornotification.matcher.repository.model.MonitorMatchesDocument;

public interface MonitorMatchesRepository extends MongoRepository<MonitorMatchesDocument, String> {
}
