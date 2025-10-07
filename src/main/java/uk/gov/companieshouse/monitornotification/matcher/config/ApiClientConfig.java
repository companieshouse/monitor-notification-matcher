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

    private final String internalApiUrl;
    private final String internalApiKey;

    private final Logger logger;

    /**
     * Constructor.
     */
    public ApiClientConfig(@Value("${spring.internal.api.url}") String internalApiUrl,
            @Value("${spring.internal.api.key}") String internalApiKey,
            final Logger logger) {
        this.internalApiUrl = internalApiUrl;
        this.internalApiKey = internalApiKey;
        this.logger = logger;
    }

    @Bean("internalApiClientSupplier")
    public Supplier<InternalApiClient> internalApiClientSupplier() {
        logger.trace("internalApiClientSupplier(url=%s) method called.".formatted(internalApiUrl));

        return () -> {
            InternalApiClient client = new InternalApiClient(new ApiKeyHttpClient(internalApiKey));
            client.setBasePath(internalApiUrl); // CHS Kafka API
            client.setInternalBasePath(internalApiUrl); // Company Profile API

            return client;
        };
    }
}
