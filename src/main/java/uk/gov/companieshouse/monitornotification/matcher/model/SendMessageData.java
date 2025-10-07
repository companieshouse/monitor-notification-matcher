package uk.gov.companieshouse.monitornotification.matcher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageData {

    @JsonProperty("CompanyNumber")
    private String companyNumber;

//    @JsonProperty("CompanyName")
//    private String companyName;

    @JsonProperty("FilingType")
    private String filingType;

    @JsonProperty("FilingDescription")
    private String filingDescription;

    @JsonProperty("FilingDate")
    private String filingDate;

    @JsonProperty("IsDelete")
    private boolean delete;

//    @JsonProperty("ChsURL")
//    private String chsUrl;

    @JsonProperty("MonitorURL")
    private String monitorUrl;

    @JsonProperty("From")
    private String from;

    @JsonProperty("Subject")
    private String subject;

    public SendMessageData() {
        super();
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

//    public String getCompanyName() {
//        return companyName;
//    }
//
//    public void setCompanyName(String companyName) {
//        this.companyName = companyName;
//    }

    public String getFilingType() {
        return filingType;
    }

    public void setFilingType(String filingType) {
        this.filingType = filingType;
    }

    public String getFilingDescription() {
        return filingDescription;
    }

    public void setFilingDescription(String filingDescription) {
        this.filingDescription = filingDescription;
    }

    public String getFilingDate() {
        return filingDate;
    }

    public void setFilingDate(String filingDate) {
        this.filingDate = filingDate;
    }

    public boolean getDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

//    public String getChsUrl() {
//        return chsUrl;
//    }
//
//    public void setChsUrl(String chsUrl) {
//        this.chsUrl = chsUrl;
//    }

    public String getMonitorUrl() {
        return monitorUrl;
    }

    public void setMonitorUrl(String monitorUrl) {
        this.monitorUrl = monitorUrl;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
