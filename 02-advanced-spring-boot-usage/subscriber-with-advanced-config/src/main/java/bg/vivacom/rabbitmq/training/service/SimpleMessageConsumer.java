package bg.vivacom.rabbitmq.training.service;

import bg.vivacom.rabbitmq.training.domain.Product;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SimpleMessageConsumer {

    @RabbitListener(queues = "products.queue", ackMode = "MANUAL")
    public void handleDomainModelMessage(Product product, Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws IOException {
        System.out.println("Received the product '" + product + "'");
        channel.confirmSelect();
        channel.basicAck(deliveryTag, true);

        // could be used as a throttling / back-pressure method
        channel.basicNack(deliveryTag, true, true);
        channel.basicReject(deliveryTag, true);


        channel.basicConsume("products.queue", true,  new DefaultConsumer(channel));
    }

    @RabbitListener(queues = "products.queue")
    public String handleDomainModelMessageAndReturnResponse(Product product) {
        System.out.println("Received the product '" + product + "'");
        return "The product with the ID " + product.id() + " was processed successfully";
    }

    @RabbitListener(queues = "products.queue", ackMode = "MANUAL")
    public void handleAMQPMessage(Message message, Channel channel) throws IOException {
        System.out.println("Received the message '" + message + "'");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
    }
}
