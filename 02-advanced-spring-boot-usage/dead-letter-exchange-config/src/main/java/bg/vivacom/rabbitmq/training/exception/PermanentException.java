package bg.vivacom.rabbitmq.training.exception;

public class PermanentException extends RuntimeException {
    public PermanentException(String message) {
        super(message);
    }
}
