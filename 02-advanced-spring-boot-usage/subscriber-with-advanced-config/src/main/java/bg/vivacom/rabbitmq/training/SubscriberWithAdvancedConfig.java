package bg.vivacom.rabbitmq.training;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SubscriberWithAdvancedConfig {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(SubscriberWithAdvancedConfig.class);
		springApplication.setWebApplicationType(WebApplicationType.SERVLET);
		springApplication.setLogStartupInfo(true);
		springApplication.run(args);
	}

	@Bean
	public ApplicationRunner applicationRunner(AmqpAdmin amqpAdmin) {
		return args -> {
			if (true) return;
			amqpAdmin.deleteExchange("log.exchange");
		};
	}
}
