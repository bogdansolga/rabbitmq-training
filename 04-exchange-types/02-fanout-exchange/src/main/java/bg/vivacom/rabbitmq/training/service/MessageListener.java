package bg.vivacom.rabbitmq.training.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {

    @RabbitListener(queues = "email.queue")
    public void listenForEmail(String message) {
        System.out.println("Received the email: " + message);
    }

    @RabbitListener(queues = "sms.queue")
    public void listenForSMS(String message) {
        System.out.println("Received the SMS: " + message);
    }
}
