package bg.vivacom.rabbitmq.training.domain.repository;

import bg.vivacom.rabbitmq.training.domain.entity.FailedMessage;
import org.springframework.data.repository.CrudRepository;

public interface FailedMessageRepository extends CrudRepository<FailedMessage, Long> {
}
