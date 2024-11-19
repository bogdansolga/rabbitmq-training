package bg.vivacom.rabbitmq.training.monitoring;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DLQMonitoringService {

    private final AlertingService alertingService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${alert.threshold}")
    private int alertThreshold;

    @Autowired
    public DLQMonitoringService(AlertingService alertingService, RabbitTemplate rabbitTemplate) {
        this.alertingService = alertingService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void monitorDLQ() {
        Optional<AMQP.Queue.DeclareOk> optionalOrdersDLQ = Optional.ofNullable(
                rabbitTemplate.execute(channel -> channel.queueDeclarePassive("orders.dlq")));
        if (optionalOrdersDLQ.isEmpty()) return;

        AMQP.Queue.DeclareOk ordersDLQ = optionalOrdersDLQ.get();
        int messageCount = ordersDLQ.getMessageCount();

        if (messageCount > alertThreshold) {
            alertingService.sendAlert("High number of dead letters: " + messageCount);
        }
    }
}
