package model.exception;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
abstract class FlashMailException extends Exception {

    /**
     * Eccezione base dell'applicazione che estende la classe {@link Exception} ereditandone tutte le caratteristiche.
     *
     * @param message: messaggio da visualizzare al lancio dell'eccezione.
     */
    FlashMailException(String message) {
        super(message);
    }
}
