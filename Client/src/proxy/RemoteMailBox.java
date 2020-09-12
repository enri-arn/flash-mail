package proxy;

import model.Mail;
import model.UserID;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public interface RemoteMailBox extends Remote {

    /**
     * Aggiunge la mail alla casella di posta. Gli <b>Observer</b> ricevendo la notify della
     * nuova mail andranno a modificare la grafica.
     *
     * @param mail: mail da scrivere all'interno della casella di posta del proprietario ricevente.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    void write(Mail mail) throws RemoteException;

    /**
     * Ottengo il proprietario della casella di posta.
     *
     * @return una istanza di <b>UserID</b>.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    UserID getOwner() throws RemoteException;

    /**
     * Ottengo la lista di mail presenti nella casella di posta.
     *
     * @return una lista di istanze di <b>Mail</b> attualmente presenti nella sua casella.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    List<Mail> getMailList() throws RemoteException;

    /**
     * Elimina la mail selezionata verificando prima se presente nella casella di posta dell'utente
     * in questione.
     * La Mail verrà allo stesso tempo eliminata dal file di testo in formato json.
     *
     * @param mail: mail da eliminare.
     * @return un valore booleano positivo se l'eliminazione è andata bene, negativo viceversa.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    boolean deleteMail(Mail mail) throws RemoteException;
}
