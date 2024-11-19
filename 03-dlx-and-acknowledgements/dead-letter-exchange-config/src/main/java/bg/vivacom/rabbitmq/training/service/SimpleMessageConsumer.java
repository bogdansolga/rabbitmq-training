package bg.vivacom.rabbitmq.training.service;

import bg.vivacom.rabbitmq.training.domain.entity.FailedMessage;
import bg.vivacom.rabbitmq.training.domain.repository.FailedMessageRepository;
import bg.vivacom.rabbitmq.training.exception.PermanentException;
import bg.vivacom.rabbitmq.training.exception.TemporaryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMessageConsumer.class);

    private final FailedMessageRepository failedMessageRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public SimpleMessageConsumer(FailedMessageRepository failedMessageRepository, RabbitTemplate rabbitTemplate) {
        this.failedMessageRepository = failedMessageRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "main-queue")
    public void processMessage(Message message) {
        try {
            processBusinessLogic(message);
        }
        catch (TemporaryException e) {
            // REQUEUE APPROACH OPTIONS:

            // Option 1: Simply throw the exception
            // By default, Spring AMQP will requeue the message
            //throw e;

            // Option 2: Explicit rejection with requeue
            throw new ImmediateRequeueAmqpException(e);

            // Option 3: Using Channel
            // channel.basicReject(deliveryTag, true); // true = requeue
        }
        catch (PermanentException e) {
            // SEND TO DLQ:
            // Reject without requeue - message will go to DLQ
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = "dead-letter-queue")
    public void processDeadLetter(Message failedMessage) {
        // Access original routing information
        MessageProperties props = failedMessage.getMessageProperties();
        String originalExchange = props.getHeader("exchange").toString();
        String originalRoutingKey = props.getHeader("routing-keys").toString();

        // Access failure information
        String reason = props.getHeader("reason").toString();

        // Implement recovery logic
        try {
            // Example: Log failure
            LOGGER.error("Message failed: exchange={}, routingKey={}, reason={}",
                    originalExchange, originalRoutingKey, reason);

            // Example: Retry processing
            if (reason.equals("expired")) {
                rabbitTemplate.send(originalExchange, originalRoutingKey, failedMessage);
            }

            // Example: Store in database for manual review
            failedMessageRepository.save(new FailedMessage(failedMessage));
        } catch (Exception e) {
            // Handle recovery failure
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void processBusinessLogic(Message message) {
        // perform magic here
    }
}
