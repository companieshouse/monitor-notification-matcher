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
        "appointment_date",
        "officer_name"
})
public class DescriptionValues {

    @JsonProperty("appointment_date")
    private String appointmentDate;

    @JsonProperty("officer_name")
    private String officerName;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    /**
     * No args constructor for use in serialization
     */
    public DescriptionValues() {
        super();
    }

    @JsonProperty("appointment_date")
    public String getAppointmentDate() {
        return appointmentDate;
    }

    @JsonProperty("appointment_date")
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    @JsonProperty("officer_name")
    public String getOfficerName() {
        return officerName;
    }

    @JsonProperty("officer_name")
    public void setOfficerName(String officerName) {
        this.officerName = officerName;
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
        return "DescriptionValues{" +
                "appointmentDate='" + appointmentDate + '\'' +
                ", officerName='" + officerName + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}

