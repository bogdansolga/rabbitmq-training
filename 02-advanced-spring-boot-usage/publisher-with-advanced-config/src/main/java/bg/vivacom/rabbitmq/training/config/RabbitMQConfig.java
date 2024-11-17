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
    public Queue firstQueue() {
        return new Queue("products-queue", true);
    }

    @Bean
    public Exchange firstExchange() {
        return new DirectExchange("products-exchange");
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
        template.setRetryTemplate(retryTemplate());  // Custom retry logic
        return template;
    }

    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new CircuitBreakerRetryPolicy());
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        return retryTemplate;
    }
}
