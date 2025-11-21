package app.exception;

public class InvalidCompanyException extends RuntimeException {

    public InvalidCompanyException() {
    }

    public InvalidCompanyException(String message) {
        super(message);
    }
}
