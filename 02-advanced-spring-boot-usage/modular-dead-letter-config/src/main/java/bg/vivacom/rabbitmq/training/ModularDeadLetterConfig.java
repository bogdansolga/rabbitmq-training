package bg.vivacom.rabbitmq.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ModularDeadLetterConfig {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(ModularDeadLetterConfig.class);
		springApplication.setWebApplicationType(WebApplicationType.SERVLET);
		springApplication.setLogStartupInfo(true);
		springApplication.run(args);
	}
}
