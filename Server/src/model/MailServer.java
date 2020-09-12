package model;


import model.exception.MailBoxException;
import model.exception.MalformedMailException;
import persistence.JsonFileWriter;
import persistence.LogWriter;
import proxy.RemoteMailBox;
import proxy.RemoteMailServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailServer extends UnicastRemoteObject implements RemoteMailServer {

    /**
     * Elementi del server.
     */
    private Map<UserID, RemoteMailBox> mailBoxes;
    private Map<UserID, List<Mail>> pendingMails;
    private SynchronousQueue<Mail> bufferPendings;

    private String ip;
    private int port;
    private String name;
    private boolean isActiveServer;
    private ServerWorker worker;
    private static MailServer instance;
    private static AtomicInteger ID_GENERATOR;

    public MailServer() throws RemoteException {
        this.mailBoxes = new HashMap<>();
        this.pendingMails = new HashMap<>();
        this.ip = "127.0.0.1";
        this.port = 2000;
        this.name = "server";
        this.worker = new ServerWorker(this);
        this.bufferPendings = new SynchronousQueue<>();
        java.rmi.registry.LocateRegistry.createRegistry(this.port);
    }

    public MailServer(String ip, int port, String name) throws RemoteException {
        this.mailBoxes = new HashMap<>();
        this.pendingMails = new HashMap<>();
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.worker = new ServerWorker(this);
        this.bufferPendings = new SynchronousQueue<>();
        java.rmi.registry.LocateRegistry.createRegistry(this.port);
    }

    /**
     * Metodo derivato dal pattern SIngleton che èermette di ricavare l'istanza del server.
     *
     * @return un istanza di {@link MailServer}.
     */
    public static MailServer getInstance() {
        if (instance == null) {
            try {
                instance = new MailServer();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * Autenticazione dell'utente tramite lettura dei dati e verifica della corrispondenza su file
     * di testo <b>user.json</b>.
     *
     * @param user: utente da autenticare.
     * @return valore booleano positivo in caso di autenticazione riuscita, falso viceversa.
     */
    @Override
    public boolean authenticate(User user) {
        System.out.println("=== client " + user.getEmail().getValue() + " è partito... ===");
        List<User> users = JsonFileWriter.readUsers() != null ? JsonFileWriter.readUsers() : new ArrayList<>();
        for (User entry : Objects.requireNonNull(users)) {
            if (entry.getEmail().getValue().equals(user.getEmail().getValue()) && entry.getPassword().equals(user.getPassword())) {
                System.out.println("=== client " + user.getEmail().getValue() + " è autenticato... ===");
                return true;
            }
        }
        return false;
    }

    /**
     * Registra la casella di posta dell'utente all'interno del server. La registrazione della casella di posta
     * indica anche che l'utente proprietario della casella di posta è andato online.
     *
     * @param mailBox: casella di posta dell'utente che intende registrarsi.
     * @throws RemoteException:  eccezione che occorre durante l'esecuzione di un metodo remoto.
     * @throws MailBoxException: eccezione che nasce nel caso in cui l'utente sia già registrato.
     */
    @Override
    public void registerMailbox(RemoteMailBox mailBox) throws RemoteException, MailBoxException {
        synchronized (mailBoxes) {
            if (mailBoxes.containsKey(mailBox.getOwner())) {
                throw new MailBoxException("Mailbox already registered");
            }
            mailBoxes.put(mailBox.getOwner(), mailBox);
            LogWriter.getInstance().addToLog("user " + mailBox.getOwner() + " go online");
        }
    }

    /**
     * Rimuove dalle caselle di posta registrate nel server la casella di posta dell'utente passatogli.
     *
     * @param owner: utente che deve registrare la propria casella di posta.
     * @throws MailBoxException: eccezione che nasce nel caso in cui l'utente sia già registrato.
     */
    @Override
    public void unregisterMailBox(UserID owner) throws MailBoxException {
        synchronized (mailBoxes) {
            if (!mailBoxes.containsKey(owner)) {
                throw new MailBoxException("Mailbox not present");
            }
            mailBoxes.remove(owner);
            LogWriter.getInstance().addToLog("user " + owner + " go offline");
        }
    }

    /**
     * Manda la mail aggiungendola alla coda sincronizzata del server.(<b>SynchronousQueue<Mail> bufferPendings</b>)
     * Nel momento in cui la coda si rimepie il thread del server si risveglia e ed esegue la send vera e
     * propria, andando ad aggiungere alla casella di posta del ricevente la mail.
     * Nel caso in cui la la casella di posta dell'utente non sia registrata nel server la mail viene messa
     * nella lista d'attesa per poi essere inviata al ricevente appena torna online.
     *
     * @param mail: mail da inviare al ricevente.
     * @throws MalformedMailException: eccezione che occorre nel caso in cui la mail inviata sia priva
     *                                 di ricevente.
     */
    @Override
    public void send(Mail mail) throws MalformedMailException {
        if (!mail.hasReceiver()) {
            throw new MalformedMailException("Missing mail receiver");
        }
        LogWriter.getInstance().addToLog("user [ " + mail.getSender() + " ] send mail to user [ " + mail.getReceiver() + " ] ");
        try {
            bufferPendings.put(mail);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ritorna la lista di mail non lette all'utente. Le mail non lette (<b>pendingMails</b>) sono tutte qelle
     * che sono state inviate all'utente <b>owner</b> durante la sua assenza, ovvero in assenxa della sua
     * casella di posta registrata sul server.
     *
     * @param owner: proprietario della casella di posta.
     * @return una lista di ocorrenze di <b>Mail</b>.
     */
    @Override
    public synchronized List<Mail> getPendingMails(UserID owner) {
        Map<String, List<Mail>> mailsOfUser = JsonFileWriter.readMailBoxes("pendingMails");
        if (mailsOfUser != null && mailsOfUser.containsKey(owner.getValue())) {
            return mailsOfUser.get(owner.getValue());
        } else {
            return null;
        }
    }

    /**
     * ELimina le mail in attesa del'utente <b>owner</b> nel momento in cui lui le apre.
     *
     * @param owner: proprietario dela casella di posta.
     */
    @Override
    public synchronized void deletePending(UserID owner, Mail mail) {
        LogWriter.getInstance().addToLog("user [ " + owner.getValue() + " ] read mail [ " + mail.getId() + " ] ");
        JsonFileWriter.deleteMailsPending(owner, mail);
    }

    /**
     * Ritorna la lista di mail dll'utente.
     *
     * @param owner: proprietario della casella di posta.
     * @return una lista di ocorrenze di <b>Mail</b>.
     */
    @Override
    public synchronized List<Mail> getMailsOfUser(UserID owner) {
        Map<String, List<Mail>> mailsOfUser = JsonFileWriter.readMailBoxes("mailBoxes");
        if (mailsOfUser != null && mailsOfUser.containsKey(owner.getValue())) {
            return mailsOfUser.get(owner.getValue());
        } else {
            return null;
        }
    }

    /**
     * Elimina la mail selezionata verificando prima se presente nella casella di posta dell'utente
     * in questione.
     * La Mail verrà allo stesso tempo eliminata dal file di testo.
     *
     * @param mail: mail da eliminare.
     * @return un valore booleano positivo se l'eliminazione è andata bene, negativo viceversa.
     * @throws RemoteException: eccezione che occorre durante l'esecuzione di un metodo remoto.
     */
    @Override
    public synchronized boolean deleteMail(UserID owner, Mail mail) throws RemoteException {
        LogWriter.getInstance().addToLog("user [ " + owner.getValue() + " ] delete mail [ " + mail.getId() + " ] ");
        if (mailBoxes.containsKey(owner)) {
            mailBoxes.get(owner).deleteMail(mail);
        }
        return JsonFileWriter.removeMail(owner.getValue(), mail);
    }

    /**
     * Ritorna la lista di mail eliminate dall'utente andando in primis a verificare la presenza
     * di mail eliminate dell'utente <b>owner</b>.
     *
     * @param owner: propritario dela casella di posta.
     * @return lista di occorrenze di <b>Mail</b>.
     */
    @Override
    public synchronized List<Mail> getDeletedMail(UserID owner) {
        Map<String, List<Mail>> mailsOfUser = JsonFileWriter.readMailBoxes("deletedMails");
        if (mailsOfUser != null && mailsOfUser.containsKey(owner.getValue())) {
            return mailsOfUser.get(owner.getValue());
        } else {
            return null;
        }
    }

    /**
     * Ritorna la lista di bozze dall'utente andando in primis a verificare la presenza
     * di bozze dell'utente <b>owner</b>.
     *
     * @param owner: propritario dela casella di posta.
     * @return lista di occorrenze di <b>Mail</b>.
     */
    @Override
    public synchronized List<Mail> getDrafts(UserID owner) {
        Map<String, List<Mail>> mailsOfUser = JsonFileWriter.readMailBoxes("drafts");
        if (mailsOfUser != null && mailsOfUser.containsKey(owner.getValue())) {
            return mailsOfUser.get(owner.getValue());
        } else {
            return null;
        }
    }

    /**
     * Ritorna la lista di mail inviate dall'utente andando a prelevare tutte le mail in ogni casella di posta,
     * poi andando a verificare per ogni mail quelle inviate dall'utente <b>owner</b>.
     *
     * @param owner: <b>Sender</b> della mail.
     * @return lista di occorrenze di <b>Mail</b>.
     */
    @Override
    public synchronized List<Mail> getSentMails(UserID owner) {
        Map<String, List<Mail>> allMails = JsonFileWriter.readMailBoxes("mailBoxes");
        Map<String, List<Mail>> pendingMails = JsonFileWriter.readMailBoxes("pendingMails");
        List<Mail> sentMails = new ArrayList<>();
        if (allMails != null) {
            allMails.forEach((String key, List<Mail> value) -> {
                for (Mail sentMail : value) {
                    if (sentMail.getSender().equals(owner)) {
                        sentMails.add(sentMail);
                    }
                }
            });
        }
        if (pendingMails != null) {
            pendingMails.forEach((String key, List<Mail> value) -> {
                for (Mail sentMail : value) {
                    if (sentMail.getSender().equals(owner)) {
                        sentMails.add(sentMail);
                    }
                }
            });
        }
        return sentMails;
    }

    /**
     * Ritorna l'istanza di AtomicInteger per la generazione di interi autoincrementanti.
     *
     * @return un istanza di <code>{@link AtomicInteger}</code>
     */
    @Override
    public synchronized int getID_GENERATOR() {
        return ID_GENERATOR.getAndIncrement();
    }

    /**
     * Agiunge la mail <b>mail</b> alle bozze del proprietario della casella di posta <b>owner</b>.
     *
     * @param mail:  mail da aggiungere alle bozze.
     * @param owner: proprietario della casella di posta.
     */
    @Override
    public synchronized void addToDrafts(Mail mail, UserID owner) {
        LogWriter.getInstance().addToLog("user [ " + owner.getValue() + " ] add mail [ " + mail.getId() + " ] to his drafts");
        JsonFileWriter.storeDrafts(owner, mail, "drafts");
    }

    /**
     * Toglie la mail <b>mail</b> alle bozze del proprietario della casella di posta <b>owner</b>.
     *
     * @param mail:  mail da aggiungere alle bozze.
     * @param owner: proprietario della casella di posta.
     */
    @Override
    public synchronized void deleteToDrafts(Mail mail, UserID owner) {
        LogWriter.getInstance().addToLog("user [ " + owner.getValue() + " ] delete mail [ " + mail.getId() + " ] from drafts");
        JsonFileWriter.deleteDrafts(mail, owner);
    }

    /**
     * Elimina definitivamente la mail selezionata del proprietario selezionato andando a verificare dapprima la
     * presenza della mail.
     *
     * @param mail: mail da eliminare.
     * @param user: proprietario della casella di posta.
     */
    @Override
    public synchronized void deleteForever(Mail mail, UserID user) {
        LogWriter.getInstance().addToLog("user [ " + user.getValue() + " ] delete forever mail [ " + mail.getId() + " ] ");
        JsonFileWriter.deleteForever(user, mail);
    }

    /**
     * Recupera una mail precedentemente eliminata dal proprietario della casella di posta andando a verificare dapprima
     * la presenza della mail.
     *
     * @param mail:  mail da recuperare.
     * @param owner: proprietario della casella di posta.
     */
    @Override
    public synchronized void restore(Mail mail, UserID owner) {
        LogWriter.getInstance().addToLog("user [ " + owner.getValue() + " ] restore previously deleted mail [ " + mail.getId() + " ] ");
        JsonFileWriter.restore(mail, owner);
    }

    /**
     * Aggiunge la mail alla lista di mail in attesa. Le mail in attesa sono tutte quelle i cui riceventi non
     * sono online e che quindi non sono registrati sul server.
     *
     * @param mail: mail da aggiungere alla lista delle <b>pendingMails</b>
     */
    public void addToPending(Mail mail) {
        List<Mail> pending = pendingMails.computeIfAbsent(mail.getReceiver(), k -> new ArrayList<>());
        pending.add(mail);
    }

    /**
     * Crea ed esporta l'istanza del registro nell'host locale che accetta richieste da specifiche
     * porte.
     * Attiva il server all'indirizzo, sulla porta e con il nome prefissati in fase di creazione.
     * Dal momento in cui è attivo viene fatto partire anche il thread principale del server che
     * rimarrà in ascolto di cambiamenti all'interno del <b>bufferPendings</b> del server.
     *
     * @throws RemoteException:       eccezione che occorre durante l'esecuzione di un metodo remoto.
     * @throws MalformedURLException: eccezione che occorre nel caso in cui l'URL non venga ben formato.
     */
    public void start() throws RemoteException, MalformedURLException {
        Naming.rebind("rmi://" + this.ip + ":" + this.port + "/" + this.name, this);
        JsonFileWriter.initWriter();
        isActiveServer = true;
        ID_GENERATOR = new AtomicInteger(JsonFileWriter.getSize());
        LogWriter.getInstance().addToLog(" === SERVER IS RUNNING === ");

        Thread t = new Thread(worker);
        t.start();
    }

    /**
     * Disattiva il server dall'indirizzo, porta e nome specificati in fase di creazione.
     * Allo stesso momento mette a false la variabile booleana che ferma il thread principale del server.
     *
     * @throws RemoteException:       eccezione che occorre durante l'esecuzione di un metodo remoto.
     * @throws NotBoundException:     eccezione che occorre nel caso in cui l'indirizzo, la porta o il
     *                                nome specificati per la disattivazione del server siano errati e quindi non esista un server
     *                                da disattivare.
     * @throws MalformedURLException: eccezione che occorre nel caso in cui l'URL non venga ben formato.
     */
    public void stop() throws RemoteException, NotBoundException, MalformedURLException {
        Naming.unbind("rmi://" + this.ip + ":" + this.port + "/" + this.name);
        isActiveServer = false;
        LogWriter.getInstance().addToLog(" === SERVER IS STOPPED === ");
    }

    /**
     * Restituisce un booleano che definisce lo stato del server:
     * - true: il server è attivo;
     * - false: il server non è attivo.
     *
     * @return un valore booleano che rappresenta lo stato del server.
     */
    public boolean isActiveServer() {
        return isActiveServer;
    }

    /**
     * Restituisce l'elemento in cima alla coda sincronizzata <b>bufferPendings</b> del server.
     * L'operazione essendo sincronizzata prevede una possibile eccezione di tipo
     * <code>{@link InterruptedException}</code>.
     *
     * @return un istanza di <b>Mail</b>.
     */
    public Mail getHeadQueuePending() {
        try {
            return bufferPendings.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verifica la presenza del ricevente della mail all'interno del registro del server.
     *
     * @param receiver: istanza di <b>UserID</b> che rappresenta la mail del ricevente.
     * @return un valore booleano <code>true</code> se <b>receiver</b> è presente, <code>false</code> viceversa.
     */
    public boolean containsReceiver(UserID receiver) {
        System.out.println("SERVER receiver = " + receiver.getValue());
        return mailBoxes.containsKey(receiver);
    }

    /**
     * Aggiunge alla casella di posta del destinatario la mail passatagli.
     *
     * @param mail: mail da aggungere alla casella di posta.
     */
    public void addToMailbox(Mail mail, UserID receiver) {
        try {
            mailBoxes.get(receiver).write(mail);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
