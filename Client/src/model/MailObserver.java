package model;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public interface MailObserver {

    /**
     * Implementazione del metodo update per notificare da casella di posta
     * a client l'arrivo di una nuova mail.
     * Allo stesso tempo il client dovr&agrave; fare la notify ai suoi <code>{@link java.util.Observer}</code>.
     *
     * @param extra: mail da passare all'osservatore.
     */
    void update(Object extra);
}
