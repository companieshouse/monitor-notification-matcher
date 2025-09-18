package uk.gov.companieshouse.monitornotification.matcher.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "transaction_id",
        "description",
        "description_values",
        "date"
})
public class Data {

    @JsonProperty("type")
    private String type;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("description_values")
    private DescriptionValues descriptionValues;

    @JsonProperty("date")
    private String date;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    /**
     * No args constructor for use in serialization
     */
    public Data() {
        super();
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("transaction_id")
    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("transaction_id")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("description_values")
    public DescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    @JsonProperty("description_values")
    public void setDescriptionValues(DescriptionValues descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "Data{" +
                "type='" + type + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", description='" + description + '\'' +
                ", descriptionValues=" + descriptionValues +
                ", date='" + date + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}

