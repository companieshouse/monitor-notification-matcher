package uk.gov.companieshouse.monitornotification.matcher.repository.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.ACTIVE;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.COMPANY_NUMBER;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.CREATED_DATE;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.QUERY;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.UPDATED_DATE;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.USER_ID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MonitorQueryDocumentTest {

    MonitorQueryDocument underTest;

    @BeforeEach
    void setUp() {
        underTest = new MonitorQueryDocument();
    }

    @Test
    void givenEmptyModel_whenValuesAreSet_thenTheyAreRetrievedSuccessfully() {
        underTest.setId(ID);
        underTest.setCompanyNumber(COMPANY_NUMBER);
        underTest.setCreatedAt(CREATED_DATE);
        underTest.setActive(ACTIVE);
        underTest.setUpdatedAt(UPDATED_DATE);
        underTest.setQuery(QUERY);
        underTest.setUserId(USER_ID);

        assertThat(underTest.getId(), is(ID));
        assertThat(underTest.getCompanyNumber(), is(COMPANY_NUMBER));
        assertThat(underTest.getCreatedAt(), is(CREATED_DATE));
        assertThat(underTest.getActive(), is(ACTIVE));
        assertThat(underTest.getUpdatedAt(), is(UPDATED_DATE));
        assertThat(underTest.getQuery(), is(QUERY));
        assertThat(underTest.getUserId(), is(USER_ID));
    }
}

