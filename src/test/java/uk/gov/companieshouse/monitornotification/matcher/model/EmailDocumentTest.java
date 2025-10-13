package uk.gov.companieshouse.monitornotification.matcher.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
public class EmailDocumentTest {

    EmailDocument<String> underTest;

    @BeforeEach
    void setUp() {
        underTest = EmailDocument.<String>builder()
                .withAppId("test-app-id")
                .withMessageId("test-message-id")
                .withMessageType("test-message-type")
                .withData("test-data")
                .withCreatedAt("2024-10-10T10:00:00Z")
                .withRecipientEmailAddress("test@email-test.com")
                .build();
    }

    @Test
    void testEmailDocumentCreation() {
        assertThat(underTest.getAppId(), is("test-app-id"));
        assertThat(underTest.getMessageId(), is("test-message-id"));
        assertThat(underTest.getMessageType(), is("test-message-type"));
        assertThat(underTest.getData(), is("test-data"));
        assertThat(underTest.getCreatedAt(), is("2024-10-10T10:00:00Z"));
        assertThat(underTest.getEmailAddress(), is("test@email-test.com"));
    }

    @Test
    void testEmailDocumentEquality() {
        EmailDocument<String> documentCopy = EmailDocument.<String>builder()
                .withAppId("test-app-id")
                .withMessageId("test-message-id")
                .withMessageType("test-message-type")
                .withData("test-data")
                .withCreatedAt("2024-10-10T10:00:00Z")
                .withRecipientEmailAddress("test@email-test.com")
                .build();

        assertThat(documentCopy.getAppId(), is(underTest.getAppId()));
        assertThat(documentCopy.getMessageId(), is(underTest.getMessageId()));
        assertThat(documentCopy.getMessageType(), is(underTest.getMessageType()));
        assertThat(documentCopy.getData(), is(underTest.getData()));
        assertThat(documentCopy.getCreatedAt(), is(underTest.getCreatedAt()));
        assertThat(documentCopy.getEmailAddress(), is(underTest.getEmailAddress()));

        assertEquals(documentCopy, underTest);
    }

    @Test
    void testEmailDocumentEqualsOriginal() {
        EmailDocument<String> documentCopy = EmailDocument.<String>builder()
                .withAppId("test-app-id")
                .withMessageId("test-message-id")
                .withMessageType("test-message-type")
                .withData("test-data")
                .withCreatedAt("2024-10-10T10:00:00Z")
                .withRecipientEmailAddress("test@email-test.com")
                .build();

        assertEquals(documentCopy, documentCopy);
        assertNotEquals(documentCopy, null);
        assertNotEquals(documentCopy, new NonRetryableException("different-type"));

        EmailDocument<String> document1 = new EmailDocument<>("app-id1", null, null, null, null, null);
        EmailDocument<String> document2 = new EmailDocument<>("app-id2", null, null, null, null, null);
        assertNotEquals(document1, document2);

        document1 = new EmailDocument<>(null, "message-id1", null, null, null, null);
        document2 = new EmailDocument<>(null, "message-id2", null, null, null, null);
        assertNotEquals(document1, document2);

        document1 = new EmailDocument<>(null, null, "message-type1", null, null, null);
        document2 = new EmailDocument<>(null, null, "message-type2", null, null, null);
        assertNotEquals(document1, document2);

        document1 = new EmailDocument<>(null, null, null, "data1", null, null);
        document2 = new EmailDocument<>(null, null, null, "data2", null, null);
        assertNotEquals(document1, document2);

        document1 = new EmailDocument<>(null, null, null, null, "email1", null);
        document2 = new EmailDocument<>(null, null, null, null, "email2", null);
        assertNotEquals(document1, document2);

        document1 = new EmailDocument<>(null, null, null, null, null, "created1");
        document2 = new EmailDocument<>(null, null, null, null, null, "created2");
        assertNotEquals(document1, document2);

        document1 = new EmailDocument<>(null, null, null, null, null, null);
        document2 = new EmailDocument<>(null, null, null, null, null, null);
        assertEquals(document1, document2);

        assertEquals(887503681, document1.hashCode());
        assertEquals(887503681, document2.hashCode());
        assertEquals(document1.hashCode(), document2.hashCode());

        assertEquals("EmailDocument[appId=<null>,createdAt=<null>,data=<null>,emailAddress=<null>,messageId=<null>,messageType=<null>]", document1.toString());
        assertEquals("EmailDocument[appId=<null>,createdAt=<null>,data=<null>,emailAddress=<null>,messageId=<null>,messageType=<null>]", document2.toString());
        assertEquals(document1.toString(), document2.toString());
    }
}
