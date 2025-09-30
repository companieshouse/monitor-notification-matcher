Monitor Notification Matcher
=====================

> Part of Monitor/Follow system.

### Service Overview
- Consumes messages from the `notification-match` Kafka topic.

### Incoming Messages (Consumed)
- *Incoming messages are consumed from a Kafka Topic named*: `notification-match`
  - Headers:
    - `correlation_id`: UUIDv4 string
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
    "notified_at": "1453896192000",
    "user_id": "1vKD26OwehmZI6MpGz9D02-dmCI"
    }
    ```

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |follow                                      | ECS cluster (stack) the service belongs to
**Load balancer**      |N/A <br> consumer                                            | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/chs-monitor-notification-matcher) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/chs-monitor-notification-matcher)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)




