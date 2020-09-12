package model;

import proxy.RemoteMailBox;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailBox extends UnicastRemoteObject implements RemoteMailBox {

    /**
     * Parametri della casella di posta.
     */
    private UserID owner;
    private List<Mail> mailList;
    private MailObserver observer;

    MailBox(UserID owner) throws RemoteException {
        this.mailList = new ArrayList<>();
        this.owner = owner;
    }

    /**
     * Aggiunge la mail alla casella di posta. Gli <b>Observer</b> ricevendo la notify della
     * nuova mail andranno a modificare la grafica.
     *
     * @param mail: mail da scrivere all'interno della casella di posta del proprietario ricevente.
     */
    @Override
    public void write(Mail mail) {
        synchronized (mailList) {
            mailList.add(mail);
            observer.update(mail);
        }
    }

    /**
     * Ottengo il proprietario della casella di posta.
     *
     * @return un istanza di <b>UserID</b>.
     */
    @Override
    public synchronized UserID getOwner() {
        return owner;
    }

    /**
     * Ottengo la lista di mail presenti nella casella di posta.
     *
     * @return una lista di istanze di <b>Mail</b> attualmente presenti nella sua casella.
     */
    @Override
    public synchronized List<Mail> getMailList() {
        return mailList;
    }

    /**
     * Elimina la mail selezionata verificando prima se presente nella casella di posta dell'utente
     * in questione.
     * La Mail verr&agrave; allo stesso tempo eliminata dal file di testo.
     *
     * @param mail: mail da eliminare.
     * @return un valore booleano positivo se l'eliminazione &egrave; andata bene, negativo viceversa.
     */
    @Override
    public boolean deleteMail(Mail mail) {
        for (Mail entry : mailList) {
            if (entry.equals(mail)) {
                System.out.println("OWNER - " + this.owner + " TRY TO DELETING MAIL: " + mail);
                return mailList.remove(mail);
            }
        }
        return false;
    }

    /**
     * Setta come osservatore il <code>{@link MailObserver}</code> passatogli come parametro.
     *
     * @param observer: osservatore della casella di posta.
     */
    public void setObserver(MailObserver observer) {
        this.observer = observer;
    }

}