package uk.gov.companieshouse.monitornotification.matcher.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyService {

    private final Logger logger;

    public CompanyService(final Logger logger) {
        this.logger = logger;
    }

    public String findCompanyNameById(final String companyId) {
        return "My Company Ltd";
    }
}
