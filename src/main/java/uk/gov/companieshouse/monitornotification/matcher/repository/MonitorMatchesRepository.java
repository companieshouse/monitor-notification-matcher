package uk.gov.companieshouse.monitornotification.matcher.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailSend;

public interface MonitorMatchesRepository extends MongoRepository<EmailSend, String> {
}
