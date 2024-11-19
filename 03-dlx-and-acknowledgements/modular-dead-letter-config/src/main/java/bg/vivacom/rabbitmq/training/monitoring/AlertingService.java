package bg.vivacom.rabbitmq.training.monitoring;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AlertingService {

    @Async
    public void sendAlert(String message) {
        // send the alert via Teams, Telegram, OpsGenie etc
    }
}
