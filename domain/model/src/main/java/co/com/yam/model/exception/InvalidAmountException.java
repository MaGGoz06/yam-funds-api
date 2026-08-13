package co.com.yam.model.exception;

public class InvalidAmountException extends BusinessException {

    public InvalidAmountException(String message) {
        super("INVALID_AMOUNT", message);
    }
}
