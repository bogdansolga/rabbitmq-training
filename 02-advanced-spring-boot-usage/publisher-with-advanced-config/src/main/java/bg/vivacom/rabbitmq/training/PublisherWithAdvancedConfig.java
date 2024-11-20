package bg.vivacom.rabbitmq.training;

import bg.vivacom.rabbitmq.training.domain.Product;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.UUID;

@SpringBootApplication
public class PublisherWithAdvancedConfig {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(PublisherWithAdvancedConfig.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.run(args);
	}

	@Bean
	public ApplicationRunner runner(RabbitTemplate rabbitTemplate) {
		return args -> {
			final String exchangeName = "products.exchange";
			final String routingKey = "products-routing-key";

			// send without receiving a response - fire & forget
			rabbitTemplate.convertAndSend(exchangeName, routingKey, new Product(100, "Tablet", 250));
			System.out.println("Successfully sent a message on the 'products.exchange' exchange");

			// send a message with a correlation ID, receive a confirmation
			CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
			Object correlationUsageResponse = rabbitTemplate.convertSendAndReceive(exchangeName, routingKey,
					new Product(120, "Tablet with correlation ID", 250), correlationData);
			System.out.println("Got the correlation response: " + correlationUsageResponse);

			// send a message with a MessagePostProcessor object, receive a confirmation
			Object response = rabbitTemplate.convertSendAndReceive(exchangeName, routingKey,
					new Product(130, "Tablet with message post processing", 250),
                    this::displayMessageProperties);
			System.out.println("Got the MessagePostProcessor response " + response);
		};
	}

	private Message displayMessageProperties(Message message) {
		System.out.println("Received the message body: " + new String(message.getBody()));
		System.out.println("Received the message properties: ");
		MessageProperties messageProperties = message.getMessageProperties();
		messageProperties.getHeaders()
						 .keySet()
						 .forEach(key -> System.out.println("\t" + key + " - " + messageProperties.getHeader(key)));
		return message;
	}
}
