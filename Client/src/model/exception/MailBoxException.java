package model.exception;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailBoxException extends Exception {

    /**
     * Eccezione che nasce nel momento in cui la casella di posta sia gi&agrave; registrata.
     *
     * @param message: messaggio da visualizzare al lancio dell'eccezione.
     */
    public MailBoxException(String message) {
        super( message );
    }
}
