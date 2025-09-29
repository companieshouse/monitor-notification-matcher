package uk.gov.companieshouse.monitornotification.matcher.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyService {

    private final Logger logger;

    public CompanyService(final Logger logger) {
        this.logger = logger;
    }

    public String findCompanyDetails(final String companyId) {
        logger.trace("findCompanyDetails(id=%s) method called.".formatted(companyId));

        return "My Company: %s".formatted(companyId);
    }
}
