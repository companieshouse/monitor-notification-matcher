package uk.gov.companieshouse.monitornotification.matcher.util;

import static java.lang.String.format;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import monitor.filing;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.serdes.GenericSerializer;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;

public class NotificationMatchTestUtils {

    public static final String COMPANY_NUMBER = "00006400";
    public static final String COMPANY_NAME = "THE GIRLS DAY SCHOOL TRUST";
    public static final String COMPANY_STATUS = "active";

    public static final String CHS_URL = "https://test.chs-url.gov.uk";
    public static final String MONITOR_URL = "https://test.follow-url.gov.uk";

    public static final String KIND = "email";
    public static final String NOTIFIED_AT = "1453896192000";
    public static final String USER_ID = "1vKD26OwehmZI6MpGz9D02-dmCI";

    private static final String NOTIFICATION_MATCH_UPDATE_DATA = """
            {
                "app_id": "chs-monitor-notification-matcher.filing",
                "company_number": "%s",
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
                "company_number": "%s",
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

    private static final String NOTIFICATION_MATCH_DELETE_DATA_WITHOUT_IS_DELETE = """
            {
                "app_id": "chs-monitor-notification-matcher.filing",
                "company_number": "%s",
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
                }
            }
            """;

    private static final String NOTIFICATION_MATCH_DELETE_DATA_WITHOUT_COMPANY_NUMBER = """
            {
                "app_id": "chs-monitor-notification-matcher.filing",
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
                .withPayload(buildFilingWithData(NOTIFICATION_MATCH_UPDATE_DATA.formatted(COMPANY_NUMBER)))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<filing> buildFilingDeleteMessageWithoutCompanyNumber() {
        return MessageBuilder
                .withPayload(buildFilingWithData(NOTIFICATION_MATCH_DELETE_DATA_WITHOUT_COMPANY_NUMBER))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<filing> buildFilingDeleteMessageWithoutIsDelete() {
        return MessageBuilder
                .withPayload(buildFilingWithData(NOTIFICATION_MATCH_DELETE_DATA_WITHOUT_IS_DELETE.formatted(COMPANY_NUMBER)))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static Message<filing> buildFilingDeleteMessageWithBlankCompanyNumber() {
        return MessageBuilder
                .withPayload(buildFilingWithData(NOTIFICATION_MATCH_DELETE_DATA.formatted("")))
                .setHeader("kafka_receivedTopic", "test-topic")
                .setHeader("kafka_offset", 42L)  // optional
                .build();
    }

    public static byte[] buildFilingRawAvroMessage() {
        return new GenericSerializer().serialize("test-topic", buildFilingUpdateMessage().getPayload());
    }

    public static CompanyDetails buildCompanyDetails() {
        CompanyDetails companyDetails = new CompanyDetails();
        companyDetails.setCompanyNumber(COMPANY_NUMBER);
        companyDetails.setCompanyName(COMPANY_NAME);
        companyDetails.setCompanyStatus(COMPANY_STATUS);
        return companyDetails;
    }

    public static EmailDocument<Map<String, Object>> buildValidEmailDocument(Boolean isDelete) {
        CompanyDetails details = buildCompanyDetails();

        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put("CompanyName", details.getCompanyName());
        dataMap.put("CompanyNumber", details.getCompanyNumber());
        dataMap.put("IsDelete", isDelete);
        dataMap.put("MonitorURL", MONITOR_URL);
        dataMap.put("ChsURL", CHS_URL);
        dataMap.put("from", "Companies House <noreply@companieshouse.gov.uk>");
        dataMap.put("subject", format("Company number %s %s", details.getCompanyNumber(), details.getCompanyName()));

        return EmailDocument.<Map<String, Object>>builder()
                .withAppId("monitor-notification-matcher.filing")
                .withMessageId(UUID.randomUUID().toString())
                .withMessageType("monitor_email")
                .withCreatedAt(NOTIFIED_AT)
                .withRecipientEmailAddress(null)
                .withData(dataMap)
                .build();
    }
}
