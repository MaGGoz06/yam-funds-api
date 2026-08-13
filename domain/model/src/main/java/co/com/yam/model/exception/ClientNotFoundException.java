package co.com.yam.model.exception;

public class ClientNotFoundException extends BusinessException {

    public ClientNotFoundException(String clientId) {
        super("CLIENT_NOT_FOUND", "No existe el cliente con id " + clientId);
    }
}
