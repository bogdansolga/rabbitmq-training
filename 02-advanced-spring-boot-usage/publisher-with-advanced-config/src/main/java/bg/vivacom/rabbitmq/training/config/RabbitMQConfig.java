package bg.vivacom.rabbitmq.training.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue productsQueue() {
        return new Queue("products.queue", true);
    }

    @Bean
    public Exchange productsExchange() {
        return new DirectExchange("products.exchange");
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue)
                             .to(exchange)
                             .with("products-routing-key")
                             .noargs();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setReplyTimeout(60000);  // 60 seconds
        template.setReceiveTimeout(3000); // 3 seconds
        template.setRetryTemplate(retryTemplate());  // Custom retry logic
        template.setConfirmCallback(((correlationData, ack, cause) -> {
            System.out.println("The cause: " + cause);
            if (!ack) {
                //TODO implement reprocessing logic
            }
        }));
        template.setReturnsCallback(returnCallback -> {
            System.out.println("Got the message " + returnCallback.getMessage());
            System.out.println("The exchange: " + returnCallback.getExchange());
        });
        return template;
    }

    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new CircuitBreakerRetryPolicy());

        final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(200);
        backOffPolicy.setMultiplier(1.1);
        backOffPolicy.setMaxInterval(2000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
