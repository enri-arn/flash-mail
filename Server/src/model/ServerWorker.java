package model;

import persistence.JsonFileWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class ServerWorker implements Runnable {

    private MailServer server;
    private final Semaphore available = new Semaphore(1);

    ServerWorker(MailServer server) {
        this.server = server;
    }

    /**
     * Thread principale del server che si occupa di effettuare l'invio efettivo della mail.
     * <b>ServerWorker</b> si attiva allo start del server e resta da subito in attesa dello riempimento
     * della coda sincronizzata <code>SynchronousQueue(Mail) buffer</code> presente in <b>MailServer</b>.
     * Tale coda di base è autobloccante, per cui si avrà un effetto simile al produttore-consumatore in
     * quanto il thread resterà in attesa di un <b>header</b> al'interno della coda.
     * (istruzione <code>server.getHeadQueue()</code>)
     */
    @Override
    public void run() {
        while (server.isActiveServer()) {
            Mail mail = server.getHeadQueuePending();
            List<UserID> receivers = new ArrayList<>();
            UserID receiver = mail.getReceiver();
            receivers.add(receiver);
            if (mail.getCarbonCopy() != null && !mail.getCarbonCopy().isEmpty()) {
                receivers.addAll(mail.getCarbonCopy());
            }
            receivers.forEach(rec -> {
                try {
                    available.acquire();
                    if (server.containsReceiver(rec)){
                        server.addToMailbox(mail, rec);
                    }
                    server.addToPending(mail);
                    JsonFileWriter.storeMailsPending(rec, mail, "pendingMails");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    available.release();
                }
            });
        }
    }
}
