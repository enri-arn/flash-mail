package model;

import model.exception.MailBoxException;
import model.exception.MalformedMailException;
import proxy.RemoteMailServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailClient extends Observable implements MailObserver {

    /**
     * Parametri del client.
     */
    private MailBox mailBox;
    private RemoteMailServer server;
    private String ip;
    private int port;
    private String name;

    public MailClient() {
        this.ip = "127.0.0.1";
        this.port = 2000;
        this.name = "server";
    }

    public MailClient(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    /**
     * Chiede al server l'id da aggiungere ad una nuova mail.
     *
     * @return un intero che rappresenta l'id.
     */
    public int getID() throws RemoteException {
        return server.getID_GENERATOR();
    }

    /**
     * Richiama la send del server per fare in modo di aggiungere alla casella di posta del ricevente
     * la mail inviata.
     *
     * @param mail: mail da inviare.
     */
    public void send(Mail mail) throws RemoteException, MalformedMailException {
        server.send(mail);
    }

    /**
     * Elimina la mail selezionata verificando prima se presente nella casella di posta dell'utente
     * in questione.
     * La Mail verr&agrave; allo stesso tempo eliminata dal file di testo.
     *
     * @param mail: mail da eliminare.
     * @return un valore booleano positivo se l'eliminazione &egrave; andata bene, negativo viceversa.
     */
    public boolean deleteMail(Mail mail) throws RemoteException {
        return server.deleteMail(this.mailBox.getOwner(), mail);
    }

    /**
     * Inizializza il client andando a effettuare l'operazione di lookup, la quale ritorner&agrave; il
     * riferimento allo stub dell'oggetto remoto associato al nome <code>name</code>.
     * Una volta inizializzato il client andr&agrave; a chiedere al server di registrare la sua casella
     * di posta <code>mailBox</code>.
     */
    public void initClient() throws RemoteException, NotBoundException, MalformedURLException {
        server = (RemoteMailServer) Naming.lookup("//" + this.ip + ":" + this.port + "/" + this.name);
    }

    /**
     * Chiede al server di togliere dal registro la <code>mailBox</code> inizializzata in fase di creazione
     * del client.
     *
     * @param email: proprietrio della casella di posta da eliminare dal registro del server.
     */
    public void stopClient(UserID email) throws RemoteException, MailBoxException {
        server.unregisterMailBox(email);
    }

    /**
     * Chiede al server di verificare la presenza di mail non lette.
     *
     * @return una lista di istanze di <b>Mail</b>.
     */
    public List<Mail> checkPendingMails() throws RemoteException {
        List<Mail> pendingMails = null;
        pendingMails = server.getPendingMails(mailBox.getOwner());
        if (pendingMails == null || pendingMails.isEmpty()) {
            pendingMails = new ArrayList<>();
        }
        return pendingMails;
    }

    /**
     * Elimina la mail selezionata <b>mail</b>.
     *
     * @param mail: mail da eliminare.
     */
    public void deletePending(Mail mail) throws RemoteException {
        System.out.println("2 - " + mailBox.getOwner().getValue());
        server.deletePending(mailBox.getOwner(), mail);
    }

    /**
     * Ritorna la lista di mail dell'utente.
     *
     * @return una lista di ocorrenze di <b>Mail</b>.
     */
    public List<Mail> checkMails() throws RemoteException {
        List<Mail> mails = null;
        mails = server.getMailsOfUser(this.mailBox.getOwner());
        if (mails == null) {
            mails = new ArrayList<>();
        }
        return mails;
    }

    /**
     * Autenticazione dell'utente tramite lettura dei dati e verifica della corrispondenza su file
     * di testo <b>user.json</b>.
     *
     * @param user: utente da autenticare.
     * @return valore booleano positivo in caso di autenticazione riuscita, falso viceversa.
     */
    public boolean authenticate(User user) throws RemoteException, MailBoxException {
        if (server.authenticate(user)) {
            mailBox = new MailBox(user.getEmail());
            mailBox.setObserver(this);
            server.registerMailbox(mailBox);
            return true;
        }
        return false;
    }

    /**
     * Implementazione del metodo update per notificare da casella di posta
     * a client larrivo di una nuova mail.
     * Allo stesso tempo il client dovr&agrave; fare la notify ai suoi <code>{@link java.util.Observer}</code>.
     *
     * @param extra: mail da passare all'osservatore.
     */
    @Override
    public void update(Object extra) {
        setChanged();
        notifyObservers(extra);
    }

    /**
     * Ritorna la lista di mail eliminate dall'utente.
     *
     * @return lista di occorrenze di <b>Mail</b>.
     */
    public List<Mail> getDeletedMails() {
        List<Mail> mails;
        try {
            mails = server.getDeletedMail(mailBox.getOwner());
        } catch (RemoteException | NullPointerException e) {
            System.err.println("=== user deleted mail not present ===");
            return new ArrayList<>();
        }
        return mails;
    }

    /**
     * Ritorna la lista di bozze dell'utente.
     *
     * @return lista di occorrenze di <b>Mail</b>.
     */
    public List<Mail> getDrafts() throws RemoteException {
        List<Mail> mails = null;
        mails = server.getDrafts(mailBox.getOwner());
        if (mails == null) {
            mails = new ArrayList<>();
        }
        return mails;
    }

    /**
     * Ritorna la lista di mail inviate dall'utente.
     *
     * @return lista di occorrenze di <b>Mail</b>.
     */
    public List<Mail> getSentMails() throws RemoteException {
        List<Mail> mails = null;
        mails = server.getSentMails(mailBox.getOwner());
        if (mails == null) {
            mails = new ArrayList<>();
        }
        return mails;
    }

    /**
     * Ritorna la lista di mail gi&agrave; lette dall'utente.
     *
     * @return lista di occorrenze di <b>Mail</b>.
     */
    public List<Mail> getReadMails() throws RemoteException {
        List<Mail> mails;
        mails = server.getMailsOfUser(mailBox.getOwner());
        if (mails == null) {
            mails = new ArrayList<>();
        }
        return mails;
    }

    /**
     * Agiunge la mail <b>mail</b> alle bozze del proprietario della casella di posta <b>owner</b>.
     *
     * @param mail: mail da aggiungere alle bozze.
     */
    public void addToDrafts(Mail mail) throws RemoteException {
        server.addToDrafts(mail, mailBox.getOwner());
    }

    /**
     * Agiunge la mail <b>mail</b> alle bozze del proprietario della casella di posta <b>owner</b>.
     *
     * @param mail: mail da aggiungere alle bozze.
     */
    public void deleteToDrafts(Mail mail) throws RemoteException {
        server.deleteToDrafts(mail, mailBox.getOwner());
    }

    /**
     * Elimina definitivamente la mail selezionata andando a chiedere al server di eliminare la mail del proprietario
     * di questa <b>mailbox</b>.
     *
     * @param mail: mail da eliminare.
     */
    public void deleteForever(Mail mail) throws RemoteException {
        server.deleteForever(mail, mailBox.getOwner());
    }

    /**
     * Recupera la mail non ancora eliminata definitivamente andando a chiedre al server di recuperare la mail.
     * La mail verr&agrave; reinserita nella mailbox e segnata come già letta.
     *
     * @param mail: mail da recuperare.
     */
    public void restore(Mail mail) throws RemoteException {
        server.restore(mail, mailBox.getOwner());
    }
}
