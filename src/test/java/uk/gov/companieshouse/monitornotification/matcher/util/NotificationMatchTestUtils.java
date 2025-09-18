package uk.gov.companieshouse.monitornotification.matcher.util;

import static uk.gov.companieshouse.monitornotification.matcher.converter.TransactionToFilingConverter.EMAIL_KIND;

import monitor.filing;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class NotificationMatchTestUtils {

    public static final String KIND = EMAIL_KIND;
    public static final String NOTIFIED_AT = "1453896192000";
    public static final String USER_ID = "1vKD26OwehmZI6MpGz9D02-dmCI";

    private static final String NOTIFICATION_MATCH_UPDATE_DATA = """
            {
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
            }
            """;

    private static final String NOTIFICATION_MATCH_DELETE_DATA = """
            {
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
                "is_delete": true
            }
            """;

    private static filing buildFilingWithData(final String data) {
        return filing.newBuilder()
                .setData(data)
                .setKind(KIND)
                .setNotifiedAt(NOTIFIED_AT)
                .setUserId(USER_ID)
                .build();
    }

    public static Message<filing> buildFilingUpdateMessage() {
        return MessageBuilder
                .withPayload(buildFilingWithData(NOTIFICATION_MATCH_UPDATE_DATA))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();

    }

    public static Message<filing> buildFilingDeleteMessage() {
        return MessageBuilder
                .withPayload(buildFilingWithData(NOTIFICATION_MATCH_DELETE_DATA))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();

    }

}

