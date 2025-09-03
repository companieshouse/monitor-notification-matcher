package uk.gov.companieshouse.monitornotification.matcher.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
