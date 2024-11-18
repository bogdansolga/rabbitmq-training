package bg.vivacom.rabbitmq.training.service;

import bg.vivacom.rabbitmq.training.exception.NonRetryableException;
import bg.vivacom.rabbitmq.training.exception.RetryableException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrdersProcessor {

    @RabbitListener(queues = "orders.queue")
    public void processOrder(Message message) {
        try {
            // Process the order
            processBusinessLogic(message);
        } catch (RetryableException e) {
            // Temporary failure - will be re-queued
            throw e;  // Will be re-queued based on defaultRequeueRejected=true
        } catch (NonRetryableException e) {
            // Permanent failure - send to DLQ
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    private void processBusinessLogic(Message message) {
        // perform magic here
    }
}
