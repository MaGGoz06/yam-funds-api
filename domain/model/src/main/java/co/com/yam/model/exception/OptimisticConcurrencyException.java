package co.com.yam.model.exception;

public class OptimisticConcurrencyException extends BusinessException {

    public OptimisticConcurrencyException(String clientId) {
        super("OPTIMISTIC_LOCK",
                "El cliente " + clientId + " fue modificado por otra operación concurrente");
    }
}
