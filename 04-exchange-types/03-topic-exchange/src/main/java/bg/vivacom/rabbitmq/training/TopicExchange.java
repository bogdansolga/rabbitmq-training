package bg.vivacom.rabbitmq.training;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TopicExchange {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(TopicExchange.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}

	@Bean
	public ApplicationRunner runner(RabbitTemplate rabbitTemplate) {
		return args -> {
			rabbitTemplate.convertAndSend("log.exchange", "A logging message");
			System.out.println("Successfully sent a message on the 'log.exchange' exchange");
		};
	}
}
