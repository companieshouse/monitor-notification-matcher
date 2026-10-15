package uk.gov.companieshouse.monitornotification.matcher.repository;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import uk.gov.companieshouse.monitornotification.matcher.repository.model.MonitorMatchDocument;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.APP_ID;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.CREATED_AT;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.DATA_PAYLOAD;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.KIND;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.MESSAGE_ID;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.USER_ID;

@Testcontainers(disabledWithoutDocker = true)
@DataMongoTest
public class MonitorRepositoryTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MonitorMatchesRepository underTest;

    @Test
    void givenQueryDocument_whenDocumentSaved_thenRetrievedSuccessfully() {
        MonitorMatchDocument email = new MonitorMatchDocument();
        email.setAppId(APP_ID);
        email.setMessageId(MESSAGE_ID);
        email.setMessageType(KIND);
        email.setData(DATA_PAYLOAD);
        email.setCreatedAt(CREATED_AT);
        email.setUserId(USER_ID);

        MonitorMatchDocument saveResult = underTest.save(email);

        assertThat(KIND, is(saveResult.getMessageType()));
        assertThat(DATA_PAYLOAD, is(saveResult.getData()));
        assertThat(CREATED_AT, is(saveResult.getCreatedAt()));
        assertThat(MESSAGE_ID, is(saveResult.getMessageId()));
        assertThat(USER_ID, is(saveResult.getUserId()));
    }
}

