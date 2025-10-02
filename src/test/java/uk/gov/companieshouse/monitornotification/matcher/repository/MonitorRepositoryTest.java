package uk.gov.companieshouse.monitornotification.matcher.repository;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.USER_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.KIND;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.CREATED_AT;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.MESSAGE_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.APP_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.DATA_PAYLOAD;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.companieshouse.monitornotification.matcher.model.EmailSend;

@Testcontainers(disabledWithoutDocker = true)
@DataMongoTest
public class MonitorRepositoryTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MonitorMatchesRepository underTest;

    @Test
    void givenQueryDocument_whenDocumentSaved_thenRetrievedSuccessfully() {
        EmailSend email = new EmailSend();
        email.setAppId(APP_ID);
        email.setMessageId(MESSAGE_ID);
        email.setMessageType(KIND);
        email.setData(DATA_PAYLOAD);
        email.setCreatedAt(CREATED_AT);
        email.setUserId(USER_ID);

        EmailSend saveResult = underTest.save(email);

        assertThat(KIND, is(saveResult.getMessageType()));
        assertThat(DATA_PAYLOAD, is(saveResult.getData()));
        assertThat(CREATED_AT, is(saveResult.getCreatedAt()));
        assertThat(MESSAGE_ID, is(saveResult.getMessageId()));
        assertThat(USER_ID, is(saveResult.getUserId()));
    }
}

