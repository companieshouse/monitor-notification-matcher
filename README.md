# monitor-notification-matcher

> Part of Monitor/Follow system. Identifies which users want to be notified of filings taken place for a particular
company.

### Service Overview
- Consume message from `notification-match` Kafka Topic.
- Extract the `company number`, optionally contained within the message.
- Lookup the `company details` using the company number: http://api.chs.local:4001/company/00640000/company-detail
- Scan the filing (provided within the message payload) for the relevant email data.
- Create an email match with the correct filing data, ready for submission.
- Store the email match in the `matches` collection within the `monitor` Mongo DB.
- Serialize the email match and publish the message onto the `message-send` topic (to send email).
- Log the output data to the relevant topics and collections at `TRACE` level for support.

### Incoming Messages (Consumed)
- *Incoming messages are consumed from a Kafka Topic named*: `notification-match`
    - Headers:
        - `correlation_id`: (Taken and inserted from the consumed message)
        - `reply_to`: Kafka topic name to send response to
    - Body:
      ```json
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
          "notified_at": "2025-03-03T15:04:03",
          "user_id": "1vKD26OwehmZI6MpGz9D02-dmCI"
      }
      ```
      
### Outgoing Messages (Produced)
- *Outgoing messages are produced to a Kafka Topic named*: `message-send`
    - Headers:
        - `correlation_id`: (Taken and inserted from the consumed message)
        - `reply_to`: Kafka topic name to send response to
    - Body:
      ```json
      ```