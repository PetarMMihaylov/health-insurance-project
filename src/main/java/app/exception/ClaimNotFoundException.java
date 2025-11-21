package app.exception;

public class ClaimNotFoundException extends RuntimeException {

    public ClaimNotFoundException() {
    }

    public ClaimNotFoundException(String message) {
        super(message);
    }
}
