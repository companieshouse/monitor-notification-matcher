package uk.gov.companieshouse.monitornotification.matcher.converter;

import java.util.function.BiFunction;
import monitor.filing;
import monitor.transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionToFilingConverter implements BiFunction<transaction, String, filing> {

    public static final String EMAIL_KIND = "email";

    @Override
    public filing apply(final transaction transaction, final String userId) {
        return filing.newBuilder()
                .setData(transaction.getData())
                .setKind(EMAIL_KIND)
                .setNotifiedAt(transaction.getPublishedAt())
                .setUserId(userId)
                .build();
    }

}

