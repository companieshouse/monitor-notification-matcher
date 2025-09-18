package uk.gov.companieshouse.monitornotification.matcher.consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageFlagsTest {

    MessageFlags underTest;

    @BeforeEach
    void setUp() {
        underTest = new MessageFlags();
    }

    @Test
    void givenMessageFlags_whenRetryable_thenReturnIsRetryable() {
        underTest.setRetryable(true);

        assertThat(underTest.isRetryable(), is(true));
    }

    @Test
    void givenMessageFlags_whenNonRetryable_thenReturnIsNonRetryable() {
        underTest.setRetryable(false);

        assertThat(underTest.isRetryable(), is(false));
    }

    @Test
    void givenMessageFlags_whenThreadDestroyed_thenReturnNonRetryable() {
        underTest.setRetryable(true);
        underTest.destroy();

        assertThat(underTest.isRetryable(), is(false));
    }
}

