package uk.gov.companieshouse.monitornotification.matcher.util;

import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;

import consumer.exception.NonRetryableErrorException;
import consumer.serialization.AvroSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import monitor.transaction;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class MonitorFilingTestUtils {

    public static final String ID = "654321";
    public static final String COMPANY_NUMBER = "00006400";
    public static final LocalDateTime CREATED_DATE = LocalDateTime.parse("2023-10-10T10:00:00");
    public static final Boolean ACTIVE = Boolean.TRUE;
    public static final LocalDateTime UPDATED_DATE = CREATED_DATE.plusDays(1);
    public static final String QUERY = "QUERY transaction WHERE company_number=\"%s\"".formatted(COMPANY_NUMBER);
    public static final String PUBLISHED_AT = "2025-03-03T15:04:03";
    public static final String TRANSACTION_ID = "158153-915517-386847";
    public static final String VERSION = "0";

    private static final String MONITOR_FILING_UPDATE_DATA = """
            {
                "company_number": "00006400",
                "data": {
                  "type": "AP01",
                  "transaction_id": "158153-915517-386847",
                  "description" : "appoint-person-director-company-with-name-date",
                  "description_values" : {
                    "appointment_date" : "1 December 2024",
                    "officer_name" : "DR AMIDAT DUPE IYIOLA"
                  },
                  "date": "2025-02-04"
                },
                "is_delete": false
            }
            """;

    private static final String MONITOR_FILING_DELETE_DATA = """
            {
                "company_number": "00006400",
                "data": {
                  "type": "AP01",
                  "transaction_id": "158153-915517-386847",
                  "description" : "appoint-person-director-company-with-name-date",
                  "description_values" : {
                    "appointment_date" : "1 December 2024",
                    "officer_name" : "DR AMIDAT DUPE IYIOLA"
                  },
                  "date": "2025-02-04"
                },
                "is_delete": true
            }
            """;

    private static final String MONITOR_FILING_DELETE_DATA_WITHOUT_TRANSACTION_ID = """
            {
                "company_number": "00006400",
                "data": {
                  "type": "AP01",
                  "description" : "appoint-person-director-company-with-name-date",
                  "description_values" : {
                    "appointment_date" : "1 December 2024",
                    "officer_name" : "DR AMIDAT DUPE IYIOLA"
                  },
                  "date": "2025-02-04"
                },
                "is_delete": true
            }
            """;

    private static final String MONITOR_FILING_DELETE_DATA_WITH_IGNORED_FIELDS = """
            {
                "company_number": "00006400",
                "data": {
                  "type": "AP01",
                  "transaction_id": "158153-915517-386847",
                  "description" : "appoint-person-director-company-with-name-date",
                  "description_values" : {
                    "appointment_date" : "1 December 2024",
                    "officer_name" : "DR AMIDAT DUPE IYIOLA",
                    "extra_field_4": "This field should be ignored"
                  },
                  "date": "2025-02-04",
                  "extra_field_3": "This field should be ignored"
                },
                "is_delete": true,
                "extra_field_2": "This field should also be ignored"
            }
            """;

    private static transaction buildTransactionWithData(final String data) {
        return transaction.newBuilder()
                .setCompanyNumber(COMPANY_NUMBER)
                .setData(data)
                .setPublishedAt(PUBLISHED_AT)
                .setVersion(VERSION)
                .build();
    }

    public static Message<transaction> buildTransactionUpdateMessage() {
        return MessageBuilder
                .withPayload(buildTransactionWithData(MONITOR_FILING_UPDATE_DATA))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<transaction> buildTransactionDeleteMessage() {
        return MessageBuilder
                .withPayload(buildTransactionWithData(MONITOR_FILING_DELETE_DATA))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<transaction> buildTransactionDeleteMessageWithoutTransactionID() {
        return MessageBuilder
                .withPayload(buildTransactionWithData(MONITOR_FILING_DELETE_DATA_WITHOUT_TRANSACTION_ID))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<transaction> buildTransactionDeleteMessageWithIgnoredFields() throws IOException {
        return MessageBuilder
                .withPayload(buildTransactionWithData(MONITOR_FILING_DELETE_DATA_WITH_IGNORED_FIELDS))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<transaction> buildTransactionInvalidMessage() {
        String dataString = "This is NOT valid JSON data";

        return MessageBuilder
                .withPayload(buildTransactionWithData(dataString))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<transaction> buildTransactionEmptyDataMessage() {
        return MessageBuilder
                .withPayload(buildTransactionWithData(""))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<transaction> buildTransactionNullDataMessage() {
        return MessageBuilder
                .withPayload(buildTransactionWithData(null))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<transaction> buildTransactionMessageWithExceptionCauseHeader() {
        String dataString = "";

        return MessageBuilder
                .withPayload(buildTransactionWithData(dataString))
                .setHeader(EXCEPTION_CAUSE_FQCN, new RecordHeader("exception-cause-key", NonRetryableErrorException.class.getName().getBytes()))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }


    public static byte[] buildTransactionRawAvroMessage() {
        return new AvroSerializer().serialize("test-topic", buildTransactionUpdateMessage().getPayload());
    }

}

