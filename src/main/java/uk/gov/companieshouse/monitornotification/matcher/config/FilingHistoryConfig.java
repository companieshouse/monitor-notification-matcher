package uk.gov.companieshouse.monitornotification.matcher.config;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.logging.Logger;

@Configuration
public class FilingHistoryConfig {

    private final ResourceLoader loader;
    private final Logger logger;

    public FilingHistoryConfig(@Qualifier("webApplicationContext") final ResourceLoader loader, final Logger logger) {
        this.loader = loader;
        this.logger = logger;
    }

    @Bean
    public Map<String, String> loadDescriptionMappings(final Yaml yaml) {
        logger.trace("loadDescriptionMappings() method called.");

        // The YAML resource resides outside our project (git submodule), but is located
        // at the root of our project (ie. is at the same level as src/, target/, pom.xml etc).
        String path = "file:api-enumerations/filing_history_descriptions.yml";

        Resource resource = loader.getResource(path);
        if (!resource.exists()) {
            throw new IllegalStateException("YAML file not found at: " + path);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            Object yamlObject = yaml.load(inputStream);
            logger.debug("YAML: %s (Class: %s)".formatted(yamlObject, yamlObject.getClass()));

            if (!(yamlObject instanceof Map)) {
                throw new IllegalArgumentException("Expected top-level YAML map");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) yamlObject;

            Object descObj = root.get("description");
            if (!(descObj instanceof Map)) {
                throw new IllegalArgumentException("Expected description node to be a map");
            }
            @SuppressWarnings("unchecked")
            Map<Object, Object> descRaw = (Map<Object, Object>) descObj;

            Map<String, String> result = new LinkedHashMap<>();
            for (Entry<Object, Object> entry : descRaw.entrySet()) {
                Object keyObj = entry.getKey();
                Object valObj = entry.getValue();
                if (keyObj == null) continue;
                String key = keyObj.toString();
                String value = (valObj == null ? null : valObj.toString());
                result.put(key, value);
            }

            return result;

        } catch(Exception ex) {
            logger.error("filingHistoryDescriptionMappings() failed. Reason: " + ex.getMessage(), ex);
        }
        return Map.of();
    }

    @Bean
    public Yaml yaml(){
        return new Yaml();
    }

}
