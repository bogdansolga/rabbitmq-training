package bg.vivacom.rabbitmq.training.config;

import bg.vivacom.rabbitmq.training.exception.TemporaryException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {

    // main exchange setup
    @Bean
    public DirectExchange mainExchange() {
        return new DirectExchange("main-exchange");
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable("main-queue")
                           // configure the DLX settings
                           .withArgument("x-dead-letter-exchange", "dlx")         // When a message is rejected/expires, send it to this exchange
                           .withArgument("x-dead-letter-routing-key", "dlq")      // Use this routing key for dead letters

                           // optional additional settings
                           .withArgument("x-message-ttl", 300000)                 // Messages expire after 300 seconds
                           .withArgument("x-max-length", 10000)                   // Queue max length
                           .build();
    }

    // Dead Letter Exchange and queue setup
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx");
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dead-letter-queue")
                           // Optional: Configure TTL for dead letters
                           .withArgument("x-message-ttl", 1000 * 60 * 60 * 24)   // Keep dead letters for 24 hours
                           .build();
    }

    // Binding Setup
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlq");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setRetryTemplate(retryTemplate());
        template.setReplyTimeout(60000);  // 60 seconds
        template.setReceiveTimeout(3000); // 3 seconds
        return template;
    }

    // 1. Retry with backoff
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                                      .maxAttempts(3)
                                      .backOffOptions(1000, 2.0, 10000) // Initial, multiplier, max interval
                                      .recoverer((message, cause) -> {
                                          // After retry exhaustion, send to DLQ
                                          throw new AmqpRejectAndDontRequeueException("Retry exhausted", cause);
                                      })
                                      .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // DEFAULT BEHAVIOR:
        // defaultRequeueRejected = true (will requeue messages on exception)
        factory.setDefaultRequeueRejected(true);

        // Configure acknowledgment mode
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        // Add retry capability
        factory.setRetryTemplate(retryTemplate());

        // Configure error handler
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(
                throwable -> {
                    if (throwable.getCause() instanceof TemporaryException) {
                        return false;  // Will be re-queued based on defaultRequeueRejected
                    }
                    return true;      // Reject and don't requeue (send to DLQ)
                }
        ));

        return factory;
    }

    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new CircuitBreakerRetryPolicy());
        retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());
        return retryTemplate;
    }
}
