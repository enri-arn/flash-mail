package proxy;


import model.Mail;
import model.User;
import model.UserID;
import model.exception.MailBoxException;
import model.exception.MalformedMailException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public interface RemoteMailServer extends Remote {

    /**
     * Autenticazione dell'utente tramite lettura dei dati e verifica della corrispondenza su file
     * di testo <b>user.json</b>.
     *
     * @param user: utente da autenticare.
     * @return valore booleano positivo in caso di autenticazione riuscita, falso viceversa.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    boolean authenticate(User user) throws RemoteException;

    /**
     * Registra la casella di posta dell'utente all'interno del server.
     * La registrazione della casella di posta indica che l'utente proprietario della casella di posta è online.
     *
     * @param mailBox: casella di posta dell'utente che intende registrarsi.
     * @throws RemoteException:  eccezione che occorre durante l'esecuzione di un metodo remoto.
     * @throws MailBoxException: eccezione che nasce nel caso in cui l'utente sia già registrato.
     */
    void registerMailbox(RemoteMailBox mailBox) throws RemoteException, MailBoxException;

    /**
     * Rimuove dalle caselle di posta registrate nel server la casella di posta dell'utente passatogli.
     *
     * @param owner: utente che deve registrare la propria casella di posta.
     * @throws RemoteException:  eccezione che occorre durante l'esecuzione di un metodo remoto.
     * @throws MailBoxException: eccezione che nasce nel caso in cui l'utente sia già registrato.
     */
    void unregisterMailBox(UserID owner) throws RemoteException, MailBoxException;

    /**
     * Manda la mail aggiungendola alla coda sincronizzata del server.(<b>SynchronousQueue<Mail> buffer</b>)
     * Nel momento in cui la coda si riempie il thread del server si risveglia e ed esegue la send vera e
     * propria, andando ad aggiungere alla casella di posta del ricevente la mail.
     * Nel caso in cui la la casella di posta dell'utente non sia registrata nel server la mail viene messa
     * nella lista d'attesa per poi essere inviata al ricevente appena torna online.
     *
     * @param mail: mail da inviare al ricevente.
     * @throws RemoteException:        eccezione che occorre durante l'esecuzione di un metodo remoto.
     * @throws MalformedMailException: eccezione che occorre nel caso in cui la mail inviata sia malformata.
     */
    void send(Mail mail) throws RemoteException, MalformedMailException;

    /**
     * Ritorna la lista di mail non lette all'utente. Le mail non lette (<b>pendingMails</b>) sono tutte quelle
     * che sono state inviate all'utente <b>owner</b> durante la sua assenza, ovvero in assenza della sua
     * casella di posta registrata sul server.
     *
     * @param owner: proprietario della casella di posta.
     * @return una lista di occorrenze di <b>Mail</b>.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    List<Mail> getPendingMails(UserID owner) throws RemoteException;

    /**
     * Elimina le mail in attesa dell'utente <b>owner</b> nel momento in cui le apre.
     *
     * @param owner: proprietario della casella di posta.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    void deletePending(UserID owner, Mail mail) throws RemoteException;

    /**
     * Ritorna la lista di mail dell'utente.
     *
     * @param owner: proprietario della casella di posta.
     * @return una lista di occorrenze di <b>Mail</b>.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    List<Mail> getMailsOfUser(UserID owner) throws RemoteException;

    /**
     * Elimina la mail selezionata andando a verificare la presenza o meno dell'utente in primis
     * nelle caselle di posta registrate sul server e poi andando a verificare la presenza della
     * mail all'interno della sua casella di posta.
     * A quel punto andrà a richiamare il metodo del client <code>{@link RemoteMailBox}</code>
     * <code>deleteMail</code>.
     *
     * @param owner: proprietario della casella di posta.
     * @param mail:  mail da eliminare.
     * @return valore booleano positivo in caso di successo, negativo viceversa.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    boolean deleteMail(UserID owner, Mail mail) throws RemoteException;

    /**
     * Ritorna la lista di mail eliminate dall'utente andando in primis a verificare la presenza
     * di mail eliminate dell'utente <b>owner</b>.
     *
     * @param owner: proprietario della casella di posta.
     * @return lista di occorrenze di <b>Mail</b>.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    List<Mail> getDeletedMail(UserID owner) throws RemoteException;

    /**
     * Ritorna la lista di bozze dell'utente andando in primis a verificare la presenza
     * di bozze dell'utente <b>owner</b>.
     *
     * @param owner: proprietario della casella di posta.
     * @return lista di occorrenze di <b>Mail</b>.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    List<Mail> getDrafts(UserID owner) throws RemoteException;

    /**
     * Ritorna la lista di mail inviate dall'utente andando a prelevare tutte le mail in ogni casella di posta,
     * poi andando a verificare per ogni mail quelle inviate dall'utente <b>owner</b>.
     *
     * @param owner: <b>Sender</b> della mail.
     * @return lista di occorrenze di <b>Mail</b>.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    List<Mail> getSentMails(UserID owner) throws RemoteException;

    /**
     * Ritorna l'istanza di AtomicInteger per la generazione di interi autoincrementanti.
     *
     * @return una istanza di <code>{@link AtomicInteger}</code>
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    int getID_GENERATOR() throws RemoteException;

    /**
     * Aggiunge la mail <b>mail</b> alle bozze del proprietario della casella di posta <b>owner</b>.
     *
     * @param mail: mail da aggiungere alle bozze.
     * @param owner: proprietario della casella di posta.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    void addToDrafts(Mail mail, UserID owner) throws RemoteException;

    /**
     * Toglie la mail <b>mail</b> dalle bozze del proprietario della casella di posta <b>owner</b>.
     *
     * @param mail: mail da aggiungere alle bozze.
     * @param owner: proprietario della casella di posta.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    void deleteToDrafts(Mail mail, UserID owner) throws RemoteException;

    /**
     * Elimina definitivamente la mail selezionata del proprietario andando a verificare dapprima la
     * presenza della mail.
     *
     * @param mail: mail da eliminare.
     * @param owner: proprietario della casella di posta.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    void deleteForever(Mail mail, UserID owner) throws RemoteException;

    /**
     * Recupera una mail precedentemente eliminata dal proprietario della casella di posta andando a verificare dapprima
     * la presenza della mail.
     *
     * @param mail: mail da recuperare.
     * @param owner: proprietario della casella di posta.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    void restore(Mail mail, UserID owner) throws RemoteException;
}
