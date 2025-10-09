package uk.gov.companieshouse.monitornotification.matcher.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.chskafka.MessageSend;
import uk.gov.companieshouse.api.chskafka.MessageSendData;

@ExtendWith(MockitoExtension.class)
public class MessageSendTest {

    MessageSend underTest;

    @BeforeEach
    void setUp() {
        underTest = new MessageSend();

        MessageSendData messageSendData = getMessageSendData();

        underTest.setAppId("test-app-id");
        underTest.setMessageId("test-message-id");
        underTest.setMessageType("test-message-type");
        underTest.setData(messageSendData);
        underTest.setCreatedAt("test-created-at");
        underTest.setUserId("test-user-id");
    }

    @Test
    void givenValidData_whenGettersCalled_thenValuesAreCorrect() {
        assertThat(underTest.getAppId(), is("test-app-id"));
        assertThat(underTest.getMessageId(), is("test-message-id"));
        assertThat(underTest.getMessageType(), is("test-message-type"));
        assertThat(underTest.getData(), is(getMessageSendData()));
        assertThat(underTest.getCreatedAt(), is("test-created-at"));
        assertThat(underTest.getUserId(), is("test-user-id"));
    }

    private MessageSendData getMessageSendData() {
        MessageSendData messageSendData = new MessageSendData();
        messageSendData.setCompanyName("test");
        messageSendData.setCompanyNumber("01234567");
        messageSendData.setFilingDate("2025-03-03");
        messageSendData.setFilingDescription("filing-description");
        messageSendData.setFilingType("AP01");
        messageSendData.setIsDelete(false);
        messageSendData.setChsURL("http://test.chs-url-test");
        messageSendData.setMonitorURL("http://test.monitor-url-test");
        messageSendData.setFrom("recipient@chtest.gov.uk");
        messageSendData.setSubject("Test Email");
        return messageSendData;
    }

}
