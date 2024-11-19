package bg.vivacom.rabbitmq.training;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HeadersExchange {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(HeadersExchange.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}

	@Bean
	public ApplicationRunner runner(RabbitTemplate rabbitTemplate) {
		return args -> {
			rabbitTemplate.convertAndSend("report.exchange", "A reports related message");
			System.out.println("Successfully sent a message on the 'report.exchange' exchange");
		};
	}
}
