package uk.gov.companieshouse.monitornotification.matcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ApplicationTest {

    @LocalServerPort
    private int serverPort;

    @Test
    void mainShouldRunWithoutErrors() {
        Application.main(new String[]{});
    }

}

