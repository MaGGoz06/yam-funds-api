package co.com.yam.model.exception;

public class AlreadySubscribedException extends BusinessException {

    public AlreadySubscribedException(String fundName) {
        super("ALREADY_SUBSCRIBED",
                "El cliente ya tiene una suscripción activa al fondo " + fundName);
    }
}
