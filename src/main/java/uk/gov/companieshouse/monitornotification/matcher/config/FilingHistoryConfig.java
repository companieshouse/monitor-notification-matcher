package uk.gov.companieshouse.monitornotification.matcher.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.FilingHistoryDescriptions;

@Configuration
public class FilingHistoryConfig {

    private static final String FILING_HISTORY_FILE_NAME = "file:api-enumerations/filing_history_descriptions.yml";

    private final ResourceLoader loader;
    private final Logger logger;

    public FilingHistoryConfig(@Qualifier("externalYamlLoader") final ResourceLoader loader, final Logger logger) {
        this.loader = loader;
        this.logger = logger;
    }

    @Bean
    public FilingHistoryDescriptions loadFilingHistoryDescriptions(final Yaml yaml) {
        logger.trace("loadFilingDescriptions() method called.");

        // The YAML resource resides outside our project (git submodule), but is located
        // at the root of our project (i.e. is at the same level as src/, target/, pom.xml etc).
        Resource yamlResource = loader.getResource(FILING_HISTORY_FILE_NAME);

        if (!yamlResource.exists()) {
            throw new IllegalStateException("YAML file not found at: " + FILING_HISTORY_FILE_NAME);
        }

        try (InputStream in = yamlResource.getInputStream()) {
            Object raw = new Yaml().load(in);

            if (!(raw instanceof Map<?, ?>)) {
                throw new IllegalStateException("Expected YAML root to be a MAP");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) raw;

            Object descObj = root.get("description");
            if (!(descObj instanceof Map<?, ?>)) {
                throw new IllegalStateException("'description' section missing or not a map");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> descRaw = (Map<String, Object>) descObj;

            Map<String, String> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : descRaw.entrySet()) {
                String key = entry.getKey();
                String val = (entry.getValue() == null ? null : entry.getValue().toString());
                result.put(key, val);
            }

            logger.info("*** Filing Descriptions loaded: " + result.size() + " entries ***");

            return new FilingHistoryDescriptions(result);

        } catch (IOException ex) {
            logger.error("Error reading YAML file", ex);
            throw new RuntimeException("Error reading external YAML file", ex);
        }
    }

    @Bean
    public Yaml yaml(){
        return new Yaml();
    }

    @Bean("externalYamlLoader")
    public ResourceLoader externalYamlLoader(final ApplicationContext applicationContext) {
        return applicationContext;
    }
}
