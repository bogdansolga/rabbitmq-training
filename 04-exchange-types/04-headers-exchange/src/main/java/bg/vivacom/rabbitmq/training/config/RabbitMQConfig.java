package bg.vivacom.rabbitmq.training.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public HeadersExchange reportExchange() {
        return new HeadersExchange("report.exchange");
    }

    @Bean
    public Queue pdfQueue() {
        return new Queue("pdf.queue");
    }

    @Bean
    public Binding pdfBinding() {
        return BindingBuilder.bind(pdfQueue())
                             .to(reportExchange())
                             .whereAll("format=pdf", "type=report").exist();
    }
}
