package uk.gov.companieshouse.monitornotification.matcher.service;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyDetailsGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;

@Service
public class CompanyService {

    private final Supplier<InternalApiClient> supplier;
    private final Logger logger;

    public CompanyService(final Supplier<InternalApiClient> supplier, final Logger logger) {
        this.supplier = supplier;
        this.logger = logger;
    }

    public Optional<CompanyDetails> findCompanyDetails(final String companyNumber) {
        logger.trace("findCompanyDetails(companyNumber=%s) method called.".formatted(companyNumber));
        try {
            InternalApiClient apiClient = supplier.get();

            PrivateCompanyDetailsGet companyDetails = apiClient
                    .privateCompanyDetailResourceHandler()
                    .getCompanyDetails("/company/%s/company-detail".formatted(companyNumber));

            ApiResponse<CompanyDetails> response = companyDetails.execute();

            return Optional.of(response.getData());

        } catch(ApiErrorResponseException | URIValidationException ex) {
            if(ex instanceof ApiErrorResponseException apiException) {
                if(apiException.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                    logger.info("No company details found for company number: %s".formatted(companyNumber));
                    return Optional.empty();
                }
            }
            logger.error("An error occurred while attempting to retrieve company details: %s".formatted(ex.getMessage()));
            throw new NonRetryableException(ex.getMessage(), ex);
        }

    }
}
