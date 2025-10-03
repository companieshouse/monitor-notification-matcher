package uk.gov.companieshouse.monitornotification.matcher.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.COMPANY_NUMBER;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildCompanyDetails;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.PrivateCompanyDetailResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyDetailsGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @Mock
    Supplier<InternalApiClient> supplier;

    @Mock
    Logger logger;

    CompanyService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CompanyService(supplier, logger);
    }

    @Test
    void givenCompanyExists_whenCompanyLookup_thenReturnCompany() throws ApiErrorResponseException, URIValidationException {
        CompanyDetails companyDetails = buildCompanyDetails();

        InternalApiClient client = mock(InternalApiClient.class);
        HttpClient httpClient = mock(HttpClient.class);
        PrivateCompanyDetailResourceHandler handler = mock(PrivateCompanyDetailResourceHandler.class);
        PrivateCompanyDetailsGet getter = mock(PrivateCompanyDetailsGet.class);

        when(supplier.get()).thenReturn(client);
        when(client.getHttpClient()).thenReturn(httpClient);
        when(client.privateCompanyDetailResourceHandler()).thenReturn(handler);
        when(handler.getCompanyDetails("/company/%s/company-detail".formatted(COMPANY_NUMBER))).thenReturn(getter);
        when(getter.execute()).thenReturn(new ApiResponse<>(200, Map.of(), companyDetails));

        Optional<CompanyDetails> result = underTest.findCompanyDetails(COMPANY_NUMBER);

        assertThat(result, is(notNullValue()));
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(companyDetails));
    }

    @Test
    void givenCompanyNotExists_whenCompanyLookup_thenReturnEmpty() throws ApiErrorResponseException, URIValidationException {
        InternalApiClient client = mock(InternalApiClient.class);
        HttpClient httpClient = mock(HttpClient.class);
        PrivateCompanyDetailResourceHandler handler = mock(PrivateCompanyDetailResourceHandler.class);
        PrivateCompanyDetailsGet getter = mock(PrivateCompanyDetailsGet.class);

        ApiErrorResponseException exceptionToRaise = ApiErrorResponseException.fromHttpResponseException(
                new HttpResponseException.Builder(404, "Not Found", new HttpHeaders()).build()
        );

        when(supplier.get()).thenReturn(client);
        when(client.getHttpClient()).thenReturn(httpClient);
        when(client.privateCompanyDetailResourceHandler()).thenReturn(handler);
        when(handler.getCompanyDetails("/company/%s/company-detail".formatted(COMPANY_NUMBER))).thenReturn(getter);
        when(getter.execute()).thenThrow(exceptionToRaise);

        Optional<CompanyDetails> result = underTest.findCompanyDetails(COMPANY_NUMBER);

        verify(logger, times(1)).trace("findCompanyDetails(companyNumber=%s) method called.".formatted(COMPANY_NUMBER));
        verify(logger, times(1)).info("No company details found for company number: %s".formatted(COMPANY_NUMBER));

        assertThat(result, is(notNullValue()));
        assertThat(result.isPresent(), is(false));
    }

    @Test
    void givenUnauthorized_whenCompanyLookup_thenExceptionRaised() throws ApiErrorResponseException, URIValidationException {
        InternalApiClient client = mock(InternalApiClient.class);
        HttpClient httpClient = mock(HttpClient.class);
        PrivateCompanyDetailResourceHandler handler = mock(PrivateCompanyDetailResourceHandler.class);
        PrivateCompanyDetailsGet getter = mock(PrivateCompanyDetailsGet.class);

        ApiErrorResponseException exceptionToRaise = ApiErrorResponseException.fromHttpResponseException(
                new HttpResponseException.Builder(401, "Unauthorized", new HttpHeaders()).build()
        );

        when(supplier.get()).thenReturn(client);
        when(client.getHttpClient()).thenReturn(httpClient);
        when(client.privateCompanyDetailResourceHandler()).thenReturn(handler);
        when(handler.getCompanyDetails("/company/%s/company-detail".formatted(COMPANY_NUMBER))).thenReturn(getter);
        when(getter.execute()).thenThrow(exceptionToRaise);

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.findCompanyDetails(COMPANY_NUMBER);
        });

        verify(logger, times(1)).trace("findCompanyDetails(companyNumber=%s) method called.".formatted(COMPANY_NUMBER));
        verify(logger, times(1)).error("An error occurred while attempting to retrieve company details: %s".formatted(exceptionToRaise.getMessage()));

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Unauthorized"));
        assertThat(expectedException.getCause(), is(exceptionToRaise));
    }

    @Test
    void givenInvalidURIProvided_whenCompanyLookup_thenExceptionRaised() throws ApiErrorResponseException, URIValidationException {
        InternalApiClient client = mock(InternalApiClient.class);
        HttpClient httpClient = mock(HttpClient.class);
        PrivateCompanyDetailResourceHandler handler = mock(PrivateCompanyDetailResourceHandler.class);
        PrivateCompanyDetailsGet getter = mock(PrivateCompanyDetailsGet.class);

        URIValidationException exceptionToRaise = new URIValidationException("Invalid URI");

        when(supplier.get()).thenReturn(client);
        when(client.getHttpClient()).thenReturn(httpClient);
        when(client.privateCompanyDetailResourceHandler()).thenReturn(handler);
        when(handler.getCompanyDetails("/company/%s/company-detail".formatted(COMPANY_NUMBER))).thenReturn(getter);
        when(getter.execute()).thenThrow(exceptionToRaise);

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.findCompanyDetails(COMPANY_NUMBER);
        });

        verify(logger, times(1)).trace("findCompanyDetails(companyNumber=%s) method called.".formatted(COMPANY_NUMBER));
        verify(logger, times(1)).error("An error occurred while attempting to retrieve company details: %s".formatted(exceptionToRaise.getMessage()));

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Invalid URI"));
        assertThat(expectedException.getCause(), is(exceptionToRaise));
    }
}
