package uk.gov.companieshouse.monitornotification.matcher.utils;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingDeleteMessageWithoutCompanyNumber;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingUpdateMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Optional;
import monitor.filing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
public class NotificationMatchDataExtractorTest {

    ObjectMapper mapper;
    Logger logger;

    NotificationMatchDataExtractor underTest;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        logger = LoggerFactory.getLogger("test-logger");

        underTest = new NotificationMatchDataExtractor(mapper, logger);
    }

    @Test
    public void givenValidPayload_whenFindDataNodeExists_thenReturnValue() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findDataNode(payload);

        assertThat(dataNode, is(notNullValue()));
        assertThat(dataNode.getNodeType(), is(JsonNodeType.OBJECT));
    }

    @Test
    public void givenValidPayload_whenFindNestedDataNode_thenReturnValue() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findNestedDataNode(payload);

        assertThat(dataNode, is(notNullValue()));
        assertThat(dataNode.getNodeType(), is(JsonNodeType.OBJECT));
    }

    @Test
    public void givenMissingDataNode_whenGetOptionalValue_thenReturnEmpty() {
        Optional<JsonNode> companyNumber = underTest.getOptionalNodeValue(null, "company_number");

        assertThat(companyNumber, is(notNullValue()));
        assertThat(companyNumber.isPresent(), is(FALSE));
    }

    @Test
    public void givenOptionalDataNodeValue_whenValueExists_thenReturnValue() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findDataNode(payload);
        Optional<JsonNode> companyNumber = underTest.getOptionalNodeValue(dataNode, "company_number");

        assertThat(companyNumber, is(notNullValue()));
        assertThat(companyNumber.isPresent(), is(TRUE));
        assertThat(companyNumber.get().asText(), is("00006400"));
    }

    @Test
    public void givenOptionalDataNodeValue_whenNullValueExists_thenReturnValue() {
        Message<filing> message = buildFilingDeleteMessageWithoutCompanyNumber();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findDataNode(payload);
        Optional<JsonNode> companyNumber = underTest.getOptionalNodeValue(dataNode, "company_name");

        assertThat(companyNumber, is(notNullValue()));
        assertThat(companyNumber.isPresent(), is(FALSE));
    }

    @Test
    public void givenOptionalDataNodeValue_whenValueMissing_thenReturnEmpty() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findDataNode(payload);
        Optional<JsonNode> companyNumber = underTest.getOptionalNodeValue(dataNode, "company_name");

        assertThat(companyNumber, is(notNullValue()));
        assertThat(companyNumber.isPresent(), is(FALSE));
    }

    @Test
    public void givenMissingDataNode_whenGetMandatoryValue_thenRaiseException() {
        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            underTest.getMandatoryNodeValue(null, "company_number");
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Supplied node does not contain a valid 'company_number' node!"));
    }

    @Test
    public void givenDataNode_whenGetMandatoryValueMissing_thenRaiseException() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findDataNode(payload);

        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            underTest.getMandatoryNodeValue(dataNode, "company_name");
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Supplied node does not contain a valid 'company_name' node!"));
    }

    @Test
    public void givenOptionalNestedDataNodeValue_whenValueExists_thenReturnValue() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findNestedDataNode(payload);
        Optional<JsonNode> filingType = underTest.getOptionalNodeValue(dataNode, "type");

        assertThat(filingType, is(notNullValue()));
        assertThat(filingType.isPresent(), is(TRUE));
        assertThat(filingType.get().asText(), is("AP01"));
    }

    @Test
    public void givenOptionalNestedDataNodeValue_whenValueMissing_thenReturnEmpty() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        JsonNode dataNode = underTest.findNestedDataNode(payload);
        Optional<JsonNode> companyNumber = underTest.getOptionalNodeValue(dataNode, "company_name");

        assertThat(companyNumber, is(notNullValue()));
        assertThat(companyNumber.isPresent(), is(FALSE));
    }

    @Test
    public void givenNestedDataNode_whenNullValue_thenRaiseException() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        payload.setData("");

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.findNestedDataNode(payload);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("An error occurred while attempting to extract the JsonNode: data"));
    }

    @Test
    public void givenNestedDataNode_whenEmptyValue_thenRaiseException() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        payload.setData("{\"data\":null}");

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.findNestedDataNode(payload);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("An error occurred while attempting to extract the JsonNode: data"));
    }
}
