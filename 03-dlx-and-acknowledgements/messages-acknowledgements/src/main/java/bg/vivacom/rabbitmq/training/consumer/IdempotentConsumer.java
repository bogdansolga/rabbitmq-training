package bg.vivacom.rabbitmq.training.consumer;

import bg.vivacom.rabbitmq.training.exception.BusinessException;
import bg.vivacom.rabbitmq.training.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotentConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentConsumer.class);

    private final MessageProcessor messageProcessor;
    private final ObjectMapper objectMapper;

    @Autowired
    public IdempotentConsumer(MessageProcessor messageProcessor, ObjectMapper objectMapper) {
        this.messageProcessor = messageProcessor;
        this.objectMapper = objectMapper;
    }

    // Thread-safe set for tracking processed messages
    private final Set<String> processedMessageIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final String QUEUE_NAME = "${rabbitmq.queue.name}";

    @RabbitListener(
            queues = QUEUE_NAME,
            ackMode = "MANUAL",
            concurrency = "3-5"
    )
    public void consume(Message message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        @Header(AmqpHeaders.MESSAGE_ID) String messageId) throws IOException {

        try {
            // Check if message was already processed
            if (processedMessageIds.add(messageId)) {
                processMessage(message);
                acknowledgeMessage(channel, deliveryTag);
                LOGGER.info("Successfully processed and acknowledged message: {}", messageId);
            } else {
                // Message already processed, just acknowledge
                acknowledgeMessage(channel, deliveryTag);
                LOGGER.info("Message {} already processed, acknowledged duplicate", messageId);
            }
        } catch (Exception e) {
            handleProcessingError(channel, deliveryTag, messageId, e);
        }
    }

    private void processMessage(Message message) throws Exception {
        String payload = new String(message.getBody());
        MessagePayload messagePayload = objectMapper.readValue(payload, MessagePayload.class);

        // Process the message using the business logic service
        messageProcessor.process(messagePayload);
    }

    private void acknowledgeMessage(Channel channel, long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    private void handleProcessingError(Channel channel, long deliveryTag, String messageId, Exception e) {
        try {
            LOGGER.error("Error processing message {}: {}", messageId, e.getMessage(), e);

            // Remove message from processed set in case of error
            processedMessageIds.remove(messageId);

            // Determine if message should be requeued based on header or exception type
            boolean shouldRequeue = shouldRequeueMessage(e);

            // Negative acknowledge with requeue decision
            channel.basicNack(deliveryTag, false, shouldRequeue);

            LOGGER.info("Message {} negative acknowledged with requeue={}", messageId, shouldRequeue);
        } catch (IOException nackError) {
            LOGGER.error("Error while negative acknowledging message {}", messageId, nackError);
        }
    }

    private boolean shouldRequeueMessage(Exception e) {
        // Add logic to determine if message should be requeued based on exception type
        // For example, don't requeue on validation errors, but do requeue on temporary failures
        return !(e instanceof ValidationException || e instanceof BusinessException);
    }
}