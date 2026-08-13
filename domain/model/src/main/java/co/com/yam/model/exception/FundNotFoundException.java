package co.com.yam.model.exception;

public class FundNotFoundException extends BusinessException {

    public FundNotFoundException(String fundId) {
        super("FUND_NOT_FOUND", "No existe el fondo con id " + fundId);
    }
}
