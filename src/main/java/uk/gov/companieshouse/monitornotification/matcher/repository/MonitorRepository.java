package uk.gov.companieshouse.monitornotification.matcher.repository;


import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.monitornotification.matcher.repository.model.MonitorQueryDocument;

public interface MonitorRepository extends MongoRepository<MonitorQueryDocument, String> {

    List<MonitorQueryDocument> findByCompanyNumber(String companyNumber);

}
