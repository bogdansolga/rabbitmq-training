package bg.vivacom.rabbitmq.training;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;
import java.util.stream.IntStream;

@SpringBootApplication
public class FanoutExchange {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(FanoutExchange.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}

	@Bean
	public ApplicationRunner runner(RabbitTemplate rabbitTemplate) {
		return args -> {
			Thread.sleep(2000);

			IntStream.range(0, 500)
					 .forEach(item -> {
						 sleepALittle();
						 rabbitTemplate.convertAndSend("notification.exchange", "","The message with the number " + item);
					 });

			System.out.println("Successfully sent a message on the 'notification.exchange' exchange");
		};
	}

	private static void sleepALittle() {
        try {
            Thread.sleep(Math.abs(new Random(300).nextInt(300)));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
