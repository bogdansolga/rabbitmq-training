package bg.vivacom.rabbitmq.training.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);

        // Enable publisher confirms
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE);
        connectionFactory.setPublisherReturns(true);

        return connectionFactory;
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // Configure mandatory message handling
        rabbitTemplate.setMandatory(true);

        // Configure confirm callback
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Message confirmed: " +
                        (correlationData != null ? correlationData.getId() : ""));
            } else {
                System.out.println("Message not confirmed: " +
                        (correlationData != null ? correlationData.getId() : "") +
                        " Cause: " + cause);
                // TODO implement the retry logic here
            }
        });

        // Configure returns callback for unroutable messages
        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("Message returned: " +
                    "\n Message: " + returned.getMessage() +
                    "\n Reply Code: " + returned.getReplyCode() +
                    "\n Reply Text: " + returned.getReplyText() +
                    "\n Exchange: " + returned.getExchange() +
                    "\n Routing Key: " + returned.getRoutingKey());
            // TODO handle the returned message here
        });

        return rabbitTemplate;
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder
                .directExchange(exchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder
                .durable(queueName)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", routingKey + ".dlq")
                .build();
    }
}