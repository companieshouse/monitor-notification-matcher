package uk.gov.companieshouse.monitornotification.matcher.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.chskafka.MessageSend;

@ExtendWith(MockitoExtension.class)
public class MessageSendTest {

    MessageSend underTest;

    @BeforeEach
    public void setUp() {
        underTest = new MessageSend();

        underTest.setAppId("test-app-id");
        underTest.setMessageId("test-message-id");
        underTest.setMessageType("test-message-type");
        underTest.setData("{\"data\": \"value\"}");
        underTest.setCreatedAt("test-created-at");
        underTest.setUserId("test-user-id");
    }

    @Test
    public void givenValidData_whenGettersCalled_thenValuesAreCorrect() {
        assertThat(underTest.getAppId(), is("test-app-id"));
        assertThat(underTest.getMessageId(), is("test-message-id"));
        assertThat(underTest.getMessageType(), is("test-message-type"));
        assertThat(underTest.getData(), is("{\"data\": \"value\"}"));
        assertThat(underTest.getCreatedAt(), is("test-created-at"));
        assertThat(underTest.getUserId(), is("test-user-id"));
    }
}
