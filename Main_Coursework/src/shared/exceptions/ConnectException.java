package shared.exceptions;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 12/12/2016
 */
public class ConnectException extends RuntimeException {

    public ConnectException(String message, Exception innerException){
        super(message, innerException);
    }
}
