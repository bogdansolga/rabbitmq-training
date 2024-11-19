package bg.vivacom.rabbitmq.training.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * A simple {@link javax.sql.DataSource} configuration, which configures the JPA repositories,
 * using the {@link EnableJpaRepositories} annotation
 *
 * @author bogdan.solga
 */
@Configuration
@EnableJpaRepositories(basePackages = "bg.vivacom.rabbitmq.training.domain.repository")
@EntityScan(basePackages = "bg.vivacom.rabbitmq.training.domain.entity")
@EnableTransactionManagement
public class DataSourceConfig {
}
