package bg.vivacom.rabbitmq.training.service;

import bg.vivacom.rabbitmq.training.domain.Product;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SimpleMessageConsumer {

    @RabbitListener(queues = "products-queue")
    public void handleDomainModelMessage(Product product) {
        System.out.println("Received the product '" + product + "'");
    }

    @RabbitListener(queues = "products-queue")
    public String handleDomainModelMessageAndReturnResponse(Product product) {
        System.out.println("Received the product '" + product + "'");
        return "The product with the ID " + product.id() + " was processed successfully";
    }
}
