package uk.gov.companieshouse.monitornotification.matcher.repository.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.USER_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.KIND;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.CREATED_AT;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.MESSAGE_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.APP_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.DATA_PAYLOAD;

@ExtendWith(MockitoExtension.class)
public class MonitorMatchesDocumentTest {
    MonitorMatchDocument underTest;

    @BeforeEach
    public void setUp() {
        underTest = new MonitorMatchDocument();
    }

    @Test
    void givenValidData_whenGettersCalled_thenValuesAreCorrect() {
        underTest.setAppId(APP_ID);
        underTest.setMessageId(MESSAGE_ID);
        underTest.setMessageType(KIND);
        underTest.setData(DATA_PAYLOAD);
        underTest.setCreatedAt(CREATED_AT);
        underTest.setUserId(USER_ID);

        assertThat(underTest.getAppId(), is(APP_ID));
        assertThat(underTest.getMessageId(), is(MESSAGE_ID));
        assertThat(underTest.getMessageType(), is(KIND));
        assertThat(underTest.getData(), is(DATA_PAYLOAD));
        assertThat(underTest.getCreatedAt(), is(CREATED_AT));
        assertThat(underTest.getUserId(), is(USER_ID));
    }
}
