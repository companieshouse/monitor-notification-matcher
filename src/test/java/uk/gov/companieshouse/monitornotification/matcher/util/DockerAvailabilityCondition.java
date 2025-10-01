package uk.gov.companieshouse.monitornotification.matcher.util;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DockerAvailabilityCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED =
            ConditionEvaluationResult.enabled("Docker is available");

    private static final ConditionEvaluationResult DISABLED =
            ConditionEvaluationResult.disabled("Docker is NOT available");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            Process process = new ProcessBuilder("docker", "info")
                    .redirectErrorStream(true)
                    .start();

            boolean exited = process.waitFor(3, TimeUnit.SECONDS);
            if (exited && process.exitValue() == 0) {
                return ENABLED;
            } else {
                return DISABLED;
            }
        } catch (Exception e) {
            return DISABLED;
        }
    }
}

