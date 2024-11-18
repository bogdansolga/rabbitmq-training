package bg.vivacom.rabbitmq.training.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange("orders.exchange");
    }

    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable("orders.queue")
                           // configure the DLX and DLQ settings
                           .withArgument("x-dead-letter-exchange", "orders.dlx")
                           .withArgument("x-dead-letter-routing-key", "orders.dlq")

                           // optional: configure the TTL and queue lengths
                           .withArgument("x-message-ttl", 30000)
                           .withArgument("x-max-length", 10000)
                           .build();
    }

    @Bean
    public Binding ordersBinding() {
        return BindingBuilder
                .bind(ordersQueue())
                .to(ordersExchange())
                .with("orders.routing");
    }
}