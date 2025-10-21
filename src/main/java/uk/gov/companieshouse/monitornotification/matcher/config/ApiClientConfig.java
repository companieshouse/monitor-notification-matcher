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

    @Bean("internalPrivateApiClientSupplier")
    public Supplier<InternalApiClient> internalPrivateApiClientSupplier(
            @Value("${spring.internal.private.api.url}") String apiUrl,
            @Value("${spring.internal.private.api.key}") String apiKey) {
        logger.trace("internalPrivateApiClientSupplier(url=%s) method called.".formatted(apiUrl));

        // Company Profile API
        return () -> {
            var client = new InternalApiClient(new ApiKeyHttpClient(apiKey));
            client.setInternalBasePath(apiUrl);

            return client;
        };
    }

    @Bean("internalKafkaApiClientSupplier")
    public Supplier<InternalApiClient> internalKafkaApiClientSupplier(
            @Value("${spring.internal.kafka.api.url}") String apiUrl,
            @Value("${spring.internal.kafka.api.key}") String apiKey) {
        logger.trace("internalKafkaApiClientSupplier(url=%s) method called.".formatted(apiUrl));

        // CHS Kafka API
        return () -> {
            var client = new InternalApiClient(new ApiKeyHttpClient(apiKey));
            client.setBasePath(apiUrl);

            return client;
        };
    }
}
