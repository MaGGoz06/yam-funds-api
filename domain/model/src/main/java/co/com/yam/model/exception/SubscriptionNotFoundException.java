package co.com.yam.model.exception;

public class SubscriptionNotFoundException extends BusinessException {

    public SubscriptionNotFoundException(String fundId) {
        super("SUBSCRIPTION_NOT_FOUND",
                "El cliente no tiene una suscripción activa al fondo " + fundId);
    }
}
