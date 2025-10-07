package uk.gov.companieshouse.monitornotification.matcher.service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;

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
            var requestId = Optional.ofNullable(DataMapHolder.getRequestId()).orElse(UUID.randomUUID().toString());

            var apiClient = supplier.get();
            apiClient.getHttpClient().setRequestId(requestId);

            var handler = apiClient.privateCompanyDetailResourceHandler();
            var companyGet = handler.getCompanyDetails("/company/%s/company-detail".formatted(companyNumber));

            ApiResponse<CompanyDetails> response = companyGet.execute();

            logger.info(String.format("Fetch '%s' company details from Company Profile API (RequestId: %s): (Response %d)",
                    companyNumber, apiClient.getHttpClient().getRequestId(), response.getStatusCode()));

            return Optional.of(response.getData());

        } catch(ApiErrorResponseException | URIValidationException ex) {
            String exceptionMessage = ex.getMessage();

            if(ex instanceof ApiErrorResponseException apiException) {
                exceptionMessage = apiException.getStatusMessage();
                if(apiException.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                    logger.info("No company details found for company number: %s".formatted(companyNumber));
                    return Optional.empty();
                }
            }

            logger.error("An error occurred while attempting to retrieve company details: %s".formatted(ex.getMessage()));
            throw new NonRetryableException(exceptionMessage, ex);
        }
    }
}
