package model.exception;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MalformedMailException extends FlashMailException {

    /**
     * Eccezione che nasce nel momento in cui un messaggio non &egrave; ben formato.
     *
     * @param message: messaggio da visualizzare al lancio dell'eccezione.
     */
    public MalformedMailException(String message) {
        super(message);
    }
}
