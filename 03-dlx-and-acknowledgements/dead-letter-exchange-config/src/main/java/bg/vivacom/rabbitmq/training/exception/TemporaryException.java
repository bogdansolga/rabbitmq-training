package bg.vivacom.rabbitmq.training.exception;

public class TemporaryException extends RuntimeException {
    public TemporaryException(String message) {
        super(message);
    }
}
