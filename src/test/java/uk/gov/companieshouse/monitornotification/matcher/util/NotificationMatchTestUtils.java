package uk.gov.companieshouse.monitornotification.matcher.util;

import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;

import consumer.exception.NonRetryableErrorException;
import consumer.serialization.AvroSerializer;
import java.time.LocalDateTime;
import monitor.filing;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class NotificationMatchTestUtils {

    public static final String KIND = "email";
    public static final String USER_ID = "1vKD26OwehmZI6MpGz9D02-dmCI";
    public static final String ID = "654321";
    public static final String COMPANY_NUMBER = "00006400";
    public static final LocalDateTime CREATED_DATE = LocalDateTime.parse("2023-10-10T10:00:00");
    public static final Boolean ACTIVE = Boolean.TRUE;
    public static final LocalDateTime UPDATED_DATE = CREATED_DATE.plusDays(1);
    public static final String QUERY = "QUERY transaction WHERE company_number=\"%s\"".formatted(COMPANY_NUMBER);
    public static final String NOTIFIED_AT = "2025-03-03T15:04:03";
    public static final String TRANSACTION_ID = "158153-915517-386847";
    public static final String VERSION = "0";

    private static final String NOTIFICATION_MATCH_DATA = """
      {
        "data": {
          "app_id": "chs-monitor-notification-matcher.filing",
          "company_number": "00006400",
          "data": {
            "type": "AP01",
            "description" : "appoint-person-director-company-with-name-date",
            "description_values" : {
              "appointment_date" : "1 December 2024",
              "officer_name" : "DR AMIDAT DUPE IYIOLA"
            },
            "links" : {
              "self" : "/transactions/158153-915517-386847/officers/67a2396e8e70c90c76a3ba62"
            },
            "category": "officers",
            "paper_filed": false,
            "subcategory": "appointments",
            "action_date": "2025-02-04",
            "date": "2025-02-04"
          },
          "is_delete": false
        },
        "kind": "email",
        "notified_at": "1453896192000",
        "user_id": "1vKD26OwehmZI6MpGz9D02-dmCI"
      }
    """;

    private static filing buildFilingWithData(final String data) {
        return filing.newBuilder()
                .setData(data)
                .setNotifiedAt(NOTIFIED_AT)
                .setKind(KIND)
                .setUserId(USER_ID)
                .build();
    }

    public static Message<filing> buildFilingUpdateMessage() {
        return MessageBuilder
                .withPayload(buildFilingWithData(NOTIFICATION_MATCH_DATA))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }
    public static Message<filing> buildFilingInvalidMessage() {
        String dataString = "This is NOT valid JSON data";

        return MessageBuilder
                .withPayload(buildFilingWithData(dataString))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<filing> buildFilingEmptyDataMessage() {
        return MessageBuilder
                .withPayload(buildFilingWithData(""))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<filing> buildFilingNullDataMessage() {
        return MessageBuilder
                .withPayload(buildFilingWithData(null))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<filing> buildFilingMessageWithExceptionCauseHeader() {
        String dataString = "";

        return MessageBuilder
                .withPayload(buildFilingWithData(dataString))
                .setHeader(EXCEPTION_CAUSE_FQCN, new RecordHeader("exception-cause-key", NonRetryableErrorException.class.getName().getBytes()))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }


    public static byte[] buildFilingRawAvroMessage() {
        return new AvroSerializer().serialize("test-topic", buildFilingUpdateMessage().getPayload());
    }

}

