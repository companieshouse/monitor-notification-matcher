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
        "company_number",
        "data",
        "published_at",
        "version",
        "offset"
})
public class MonitorFiling {

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("data")
    private Payload data;

    @JsonProperty("published_at")
    private String publishedAt;

    @JsonProperty("version")
    private String version;

    @JsonProperty("offset")
    private String offset;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    /**
     * No args constructor for use in serialization
     */
    public MonitorFiling() {
        super();
    }

    @JsonProperty("company_number")
    public String getCompanyNumber() {
        return companyNumber;
    }

    @JsonProperty("company_number")
    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @JsonProperty("data")
    public Payload getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(Payload data) {
        this.data = data;
    }

    @JsonProperty("published_at")
    public String getPublishedAt() {
        return publishedAt;
    }

    @JsonProperty("published_at")
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("offset")
    public String getOffset() {
        return offset;
    }

    @JsonProperty("offset")
    public void setOffset(String offset) {
        this.offset = offset;
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
        return "MonitorFiling{" +
                "companyNumber='" + companyNumber + '\'' +
                ", data=" + data +
                ", publishedAt='" + publishedAt + '\'' +
                ", version='" + version + '\'' +
                ", offset='" + offset + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}

