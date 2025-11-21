package app.exception;

public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException() {
    }

    public PolicyNotFoundException(String message) {
        super(message);
    }
}
