package uk.gov.companieshouse.monitornotification.matcher.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
public class MongoConfigTest {

    private MongoConfig underTest;

    @BeforeEach
    public void setUp() {
        underTest = new MongoConfig("mongodb://localhost:27017", "test-database");
    }

    @Test
    public void givenConfigProvider_whenMongoTemplateCreated_thenNoErrorsAreRaised() {
        MongoTemplate result = underTest.mongoTemplate();

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void givenConfigProvider_whenMongoClientCreated_thenNoErrorsAreRaised() {
        MongoClient result = underTest.mongoClient();

        assertThat(result, is(notNullValue()));
    }
}

