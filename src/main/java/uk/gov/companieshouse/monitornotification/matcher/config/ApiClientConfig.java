package uk.gov.companieshouse.monitornotification.matcher.config;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.logging.Logger;

@Configuration
public class ApiClientConfig {

    private final Logger logger;

    /**
     * Constructor.
     */
    public ApiClientConfig(final Logger logger) {
        this.logger = logger;
    }

    @Bean("internalApiClientSupplier")
    public Supplier<InternalApiClient> internalApiClientSupplier(
            @Value("${spring.internal.api.url}") String internalApiUrl,
            @Value("${spring.internal.api.key}") String internalApiKey) {
        logger.trace("internalApiClientSupplier(url=%s) method called.".formatted(internalApiUrl));

        return () -> {
            InternalApiClient internalApiClient = new InternalApiClient(new ApiKeyHttpClient(internalApiKey));
            internalApiClient.setInternalBasePath(internalApiUrl);

            return internalApiClient;
        };
    }
}
