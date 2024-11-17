package bg.vivacom.rabbitmq.training;

import bg.vivacom.rabbitmq.training.domain.Product;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
			Product product = new Product(100, "Tablet", 250);
			rabbitTemplate.convertAndSend("products-exchange", "products-routing-key", product);
			System.out.println("Successfully sent a message on the 'products-exchange' exchange");
		};
	}
}
