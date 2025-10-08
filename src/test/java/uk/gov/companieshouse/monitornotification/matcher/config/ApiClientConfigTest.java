package uk.gov.companieshouse.monitornotification.matcher.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.logging.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class ApiClientConfigTest {

    ApiClientConfig underTest;

    @BeforeEach
    public void setUp() {
        underTest = new ApiClientConfig(LoggerFactory.getLogger("test-logger"));
    }

    @Test
    public void testApiClientConfig() {
        Supplier<InternalApiClient> result = underTest.internalApiClientSupplier(
                "http://example.com", "test-api-key");

        assertThat(result, is(notNullValue()));
        assertThat(result.get(), is(notNullValue()));

        assertThat(result.get().getInternalBasePath(), is("http://example.com"));
        assertThat(result.get().getHttpClient().getClass(), is(ApiKeyHttpClient.class));
    }
}