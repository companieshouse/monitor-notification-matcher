package uk.gov.companieshouse.monitornotification.matcher.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InvalidPayloadExceptionTest {

    InvalidPayloadException underTest;

    @BeforeEach
    void setUp() {
        underTest = new InvalidPayloadException("message", new Throwable());
    }

    @Test
    void testConstructor() {
        assertThat(underTest.getMessage(), is("message"));
    }
}
