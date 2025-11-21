package app.exception;

public class UserAlreadyFoundException extends RuntimeException {

    public UserAlreadyFoundException() {
    }

    public UserAlreadyFoundException(String message) {
        super(message);
    }
}
