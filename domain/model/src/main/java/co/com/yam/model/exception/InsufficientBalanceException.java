package co.com.yam.model.exception;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String fundName) {
        super("INSUFFICIENT_BALANCE",
                "No tiene saldo disponible para vincularse al fondo " + fundName);
    }
}
