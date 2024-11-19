package bg.vivacom.rabbitmq.training.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeadLetterProcessor {

    @RabbitListener(queues = "orders.dlq")
    public void processDLQ(Message failedMessage) {
        MessageProperties props = failedMessage.getMessageProperties();
        Map<String, Object> headers = props.getHeaders();
        Optional<Object> optionalValues = Optional.ofNullable(headers.get("x-death"));
        if (optionalValues.isEmpty() || !(optionalValues.get() instanceof List)) return;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> properties = (List<Map<String, Object>>) optionalValues.get();
        if (properties.isEmpty()) return;

        // extract failure information
        Map<String, Object> firstMap = properties.getFirst();
        String reason = firstMap.get("reason").toString();
        String queue = firstMap.get("queue").toString();
        int count = (int) firstMap.get("count");

        // Implement recovery strategy
        switch (reason) {
            case "rejected" -> handleRejectedMessage(failedMessage);
            case "expired" -> handleExpiredMessage(failedMessage);
            case "maxlen" -> handleQueueOverflow(failedMessage);
        }
    }

    private void handleRejectedMessage(Message failedMessage) {
        // 1. Log failure details
        logFailureDetails(failedMessage);

        // 2. Store in database
        saveFailedMessage(failedMessage);

        // 3. Optional: Retry after delay
        if (shouldRetry(failedMessage)) {
            scheduleRetry(failedMessage);
        }

        // 4. Notify the support team
        notifySupport(failedMessage);
    }

    private void logFailureDetails(Message failedMessage) {
    }

    private void saveFailedMessage(Message failedMessage) {
    }

    private boolean shouldRetry(Message failedMessage) {
        return false;
    }

    private void scheduleRetry(Message failedMessage) {
    }

    private void notifySupport(Message failedMessage) {
    }

    private void handleExpiredMessage(Message failedMessage) {
    }

    private void handleQueueOverflow(Message failedMessage) {
    }
}