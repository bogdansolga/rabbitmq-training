package bg.vivacom.rabbitmq.training;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SimplePublisher {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(SimplePublisher.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.run(args);
	}

	@Bean
	public ApplicationRunner runner(RabbitTemplate rabbitTemplate) {
		return args -> {
			rabbitTemplate.convertAndSend("first-exchange", "first-routing-key", "Our first message");
			System.out.println("Successfully sent a message on the 'first-exchange' exchange");
		};
	}
}
