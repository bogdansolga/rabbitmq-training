package bg.vivacom.rabbitmq.training.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.amqp.core.Message;

@Entity
public class FailedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Message message;

    public FailedMessage() {
    }

    public FailedMessage(Message message) {
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
