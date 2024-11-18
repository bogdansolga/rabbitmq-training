package bg.vivacom.rabbitmq.training.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange("notification.exchange");
    }

    @Bean
    public Queue emailQueue() {
        return new Queue("email.queue");
    }

    @Bean
    public Queue smsQueue() {
        return new Queue("sms.queue");
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                             .to(notificationExchange());  // no routing key is needed
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue())
                             .to(notificationExchange());
    }
}
