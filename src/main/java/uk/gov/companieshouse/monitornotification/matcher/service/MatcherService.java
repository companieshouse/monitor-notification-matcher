package uk.gov.companieshouse.monitornotification.matcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import monitor.filing;
import monitor.transaction;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.converter.TransactionToFilingConverter;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.producer.NotificationMatchProducer;
import uk.gov.companieshouse.monitornotification.matcher.repository.MonitorRepository;
import uk.gov.companieshouse.monitornotification.matcher.repository.model.MonitorQueryDocument;

/**
 * Default message consumer to allow the build to compile. We need at least one qualifying bean with
 * the MessageRouter interface in order for the build to compile, but this can be removed later on.
 */
@Service
public class MatcherService {

    private final MonitorRepository repository;
    private final ObjectMapper mapper;
    private final NotificationMatchProducer producer;
    private final TransactionToFilingConverter converter;
    private final Logger logger;

    public MatcherService(MonitorRepository repository, ObjectMapper mapper, NotificationMatchProducer producer,
            TransactionToFilingConverter converter, Logger logger) {
        this.repository = repository;
        this.mapper = mapper;
        this.producer = producer;
        this.converter = converter;
        this.logger = logger;
    }

    public void processMessage(final transaction message) {
        logger.trace("processMessage(message=%s) method called.".formatted(message));

        Optional<String> transactionId = getTransactionId(message);
        if(transactionId.isEmpty()) {
            logger.debug("Delete non-existent filing for company: [%s] received - attempting to match users".
                    formatted(message.getCompanyNumber()));
        } else {
            logger.debug("Received transaction for company: [%s], transaction id: [%s] - attempting to match users".
                    formatted(message.getCompanyNumber(), transactionId.get()));
        }

        // Query the repository for matching companies.
        List<MonitorQueryDocument> companies = repository.findByCompanyNumber(message.getCompanyNumber());
        logger.debug("Found %d matching companies".formatted(companies.size()));

        // Process each query document and prepare messages for the producer.
        companies.stream().map(MonitorQueryDocument::getUserId).forEach(userId -> {
            filing payload = converter.apply(message, userId);

            producer.sendMessage(payload);
        });
    }

    private Optional<String> getTransactionId(final transaction message) {
        logger.trace("getTransactionId() method called.");
        try {
            JsonNode root = mapper.readTree(message.getData());
            JsonNode data = root.get("data");

            if (data == null) {
                return Optional.empty();
            }

            JsonNode transactionNode = data.get("transaction_id");

            return Optional.ofNullable(transactionNode).map(JsonNode::asText);

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while attempting to extract the TransactionID:", e);
            throw new NonRetryableException("Error extracting transaction ID from message", e);
        }
    }
}
