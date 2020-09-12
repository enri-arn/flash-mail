package persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import model.Mail;
import model.User;
import model.UserID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class JsonFileWriter {

    /**
     * Path di riferimento per i file.
     */
    private static String FILE_PATH = System.getProperty("user.dir") + "\\Server\\src\\persistence\\file\\";

    /**
     * Istanze statiche del file.
     */
    private static ConcurrentMap<UserID, List<Mail>> mailBoxesTmp = new ConcurrentHashMap<>();
    private static ConcurrentMap<UserID, List<Mail>> mailPendsTmp = new ConcurrentHashMap<>();
    private static ConcurrentMap<UserID, List<Mail>> mailDeletTmp = new ConcurrentHashMap<>();
    private static ConcurrentMap<UserID, List<Mail>> mailDraftTmp = new ConcurrentHashMap<>();

    /**
     * Scrive su file gli utenti, passatigli nella lista, in formato json tramite
     * il parser <b>Gson</b>.
     *
     * @param users: lista di istanze di <b>User</b> da salvare.
     */
    public static void storeUsers(List<User> users) {
        Gson gson = new Gson();
        String json = gson.toJson(users);
        System.out.println(json);
        try (FileWriter writer = new FileWriter(new File(FILE_PATH + "\\users.json"))) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Legge da file di testo gli utenti salvandoli in una lista.
     * La parsificazione del file in una lista di utenti avviene tramite il parser <b>Gson</b>
     * andando a definire in precendenza tramite il <code>TypeToken</code> il tipo di oggetti
     * che ci aspettiamo di leggere.
     * Nel caso in cui il file non esista occorrerà una <code>{@link FileNotFoundException}</code>
     *
     * @return una lista di istanze di <b>User</b> se presenti, <b>null</b> altrimenti.
     */
    @Nullable
    public static List<User> readUsers() {
        final Type USERS_TYPE = new TypeToken<List<User>>() {
        }.getType();
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(new File(FILE_PATH + "\\users.json")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (reader != null) {
            return gson.fromJson(reader, USERS_TYPE);
        } else {
            return null;
        }
    }

    /**
     * Scrive su file ti testo in formato json le caselle di posta di ogni utente nel seguente formato:
     * -"email 1"
     * {
     * - " mail 1 ",
     * - " mail 2 ",
     * ...
     * }
     * -"email 2"
     * {
     * ...
     * }
     * Questo formato viene ottenuto parsificando la <code>Map (String, List (Mail)) </code> tramite
     * il parser <b>Gson</b>.
     * Tale struttura viene applicata sia alla lista di tutte le mail che alle mail in attesa.
     *
     * @param userID: proprietario della casella di posta
     * @param mail:   mail da aggungere al file.
     * @param file:   nome del file in cui salvare.
     */
    public static void storeMailsPending(UserID userID, Mail mail, String file) {
        Gson gson = new Gson();
        if (!mailPendsTmp.containsKey(userID)) {
            mailPendsTmp.put(userID, new ArrayList<>());
        }
        mailPendsTmp.get(userID).add(mail);
        try (FileWriter writer = new FileWriter(new File(FILE_PATH + file + ".json"))) {
            gson.toJson(mailPendsTmp, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Scrive su file ti testo in formato json le caselle di posta di ogni utente nel seguente formato:
     * -"email 1"
     * {
     * - " mail 1 ",
     * - " mail 2 ",
     * ...
     * }
     * -"email 2"
     * {
     * ...
     * }
     * Questo formato viene ottenuto parsificando la <code>Map (String, List (Mail)) </code> tramite
     * il parser <b>Gson</b>.
     * Tale struttura viene applicata sia alla lista di tutte le mail che alle mail in attesa.
     *
     * @param userID : proprietario della casella di posta
     * @param mail   :   mail da aggungere al file.
     */
    private static void storeDeleteMails(UserID userID, Mail mail) {
        Gson gson = new Gson();
        if (!mailDeletTmp.containsKey(userID)) {
            mailDeletTmp.put(userID, new ArrayList<>());
        }
        mailDeletTmp.get(userID).add(mail);
        try (FileWriter writer = new FileWriter(new File(FILE_PATH + "deletedMails" + ".json"))) {
            gson.toJson(mailDeletTmp, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Scrive su file ti testo in formato json le bozze di ogni utente nel seguente formato:
     * -"email 1"
     * {
     * - " mail 1 ",
     * - " mail 2 ",
     * ...
     * }
     * -"email 2"
     * {
     * ...
     * }
     * Questo formato viene ottenuto parsificando la <code>Map (String, List (Mail)) </code> tramite
     * il parser <b>Gson</b>.
     * Tale struttura viene applicata sia alla lista di tutte le mail che alle mail in attesa.
     *
     * @param userID : proprietario della casella di posta
     * @param mail   :   mail da aggungere al file.
     * @param file:  file sul quale scrivere.
     */
    public static void storeDrafts(UserID userID, Mail mail, String file) {
        Gson gson = new Gson();
        if (!mailDraftTmp.containsKey(userID)) {
            mailDraftTmp.put(userID, new ArrayList<>());
        }
        mailDraftTmp.get(userID).add(mail);
        try (FileWriter writer = new FileWriter(new File(FILE_PATH + file + ".json"))) {
            gson.toJson(mailDraftTmp, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Legge da file di testo le mail per ogni utente salvandole in una <code>Map (String, List (Mail)) </code>.
     * Anche se gli utenti in fase di scruttura vengono passati come <code>{@link UserID}</code>, il
     * parser <b>Gson</b> per risparmiare spazio e vedendo che l'oggetto racchiude solo una stringa al suo
     * interno, li trasforma come tipi più semplici <code>{@link String}</code>.
     * Questo è il motivo per cui in ingresso viene passato uno <code>{@link UserID}</code> mentre in
     * uscita ci aspettiamo una <code>{@link String}</code>.
     *
     * @param file: nome del file da leggere.
     * @return una istanza di <code>Map (String, List (Mail)) </code><b>mails</b> contenente una lista di mail
     * per ogni utente.
     */
    @Nullable
    public static Map<String, List<Mail>> readMailBoxes(String file) {
        final Type MAILBOXES_TYPE = new TypeToken<Map<String, List<Mail>>>() {
        }.getType();
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(new File(FILE_PATH + file + ".json")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Map<String, List<Mail>> ris;
        if (reader != null) {
            ris = gson.fromJson(reader, MAILBOXES_TYPE);
        } else {
            ris = new HashMap<>();
        }
        return ris;
    }

    /**
     * Elimia una mail dalle <b>mailBoxesTmp</b> e la scrive in un altro file dedicato
     * con visibilità solo al server.
     *
     * @param user: utente della casella di posta.
     * @param mail: mail da eliminare.
     * @return un valore booleano positivo in caso di eliminazione riuscita.
     */
    @Contract(pure = true)
    public static boolean removeMail(String user, Mail mail) {
        if (mailBoxesTmp.containsKey(new UserID(user))) {
            mailBoxesTmp.get(new UserID(user)).remove(mail);
        }
        if (mailPendsTmp.containsKey(new UserID(user))) {
            mailPendsTmp.get(new UserID(user)).remove(mail);
        }
        if (mailDraftTmp.containsKey(new UserID(user))) {
            mailDraftTmp.get(new UserID(user)).remove(mail);
        }
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(new File(FILE_PATH + "mailBoxes" + ".json"))) {
            gson.toJson(mailBoxesTmp, writer);
        } catch (IOException e) {
            System.err.println("JsonFileWriter IOException");
        }
        JsonFileWriter.storeDeleteMails(new UserID(user), mail);
        return true;
    }

    /**
     * Per mantenere la pertinenza di tutte le mail ongi accensione del server viene letto
     * il file contenente tutte le mail e salvato all'interno della <code>Map (String, List (Mail)) </code>.
     * Al file non viene effettuata una append del testo perché il json parsificato non sarebbe corretto.
     */
    public static void initWriter() {
        Map<String, List<Mail>> mails = readMailBoxes("mailBoxes");
        if (mails != null && !mails.isEmpty()) {
            for (Map.Entry<String, List<Mail>> entry : mails.entrySet()) {
                mailBoxesTmp.put(new UserID(entry.getKey()), entry.getValue());
            }
        }
        Map<String, List<Mail>> pending = readMailBoxes("pendingMails");
        if (pending != null && !pending.isEmpty()) {
            for (Map.Entry<String, List<Mail>> entry : pending.entrySet()) {
                mailPendsTmp.put(new UserID(entry.getKey()), entry.getValue());
            }
        }
        Map<String, List<Mail>> drafts = readMailBoxes("drafts");
        if (drafts != null && !drafts.isEmpty()) {
            for (Map.Entry<String, List<Mail>> entry : drafts.entrySet()) {
                mailDraftTmp.put(new UserID(entry.getKey()), entry.getValue());
            }
        }
        Map<String, List<Mail>> delete = readMailBoxes("deletedMails");
        if (delete != null && !delete.isEmpty()) {
            for (Map.Entry<String, List<Mail>> entry : delete.entrySet()) {
                mailDeletTmp.put(new UserID(entry.getKey()), entry.getValue());
            }
        }
    }

    /**
     * Elimina dalle mail in attesa la lista di mail dell'utente andandole a salvare nel file
     * delle amil principali.
     *
     * @param owner: proprietario della casella di posta.
     * @param mail:  mail da eliminare dalle pending.
     */
    public static void deleteMailsPending(UserID owner, Mail mail) {
        Gson gson = new Gson();
        System.out.println("5 - FileWriter - " + owner.getValue());
        if (mailPendsTmp.containsKey(owner) && mailPendsTmp.get(owner).contains(mail)) {
            mailPendsTmp.get(owner).remove(mailPendsTmp.get(owner).indexOf(mail));
        }
        try (FileWriter writer = new FileWriter(new File(FILE_PATH + "pendingMails" + ".json"))) {
            gson.toJson(mailPendsTmp, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Mail> oldMailboxes = new ArrayList<>();
        if (!mailBoxesTmp.isEmpty() && mailBoxesTmp.containsKey(owner)) {
            oldMailboxes.addAll(mailBoxesTmp.get(owner));
            mailBoxesTmp.get(owner).clear();
        }
        oldMailboxes.add(mail);
        mailBoxesTmp.put(owner, oldMailboxes);
        try (FileWriter writer = new FileWriter(new File(FILE_PATH + "mailBoxes" + ".json"))) {
            gson.toJson(mailBoxesTmp, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restituisce la grandezza del file con la lista pi&ugrave; lunga che verr&agrave; poi usata come id della mail.
     *
     * @return un intero rappresentante la lunghezza del file più lungo.
     */
    public static int getSize() {
        Map<String, List<Mail>> mails1 = readMailBoxes("pendingMails");
        Map<String, List<Mail>> mails2 = readMailBoxes("pendingMails");
        int currentMaxSize = 0;
        if (mails1 != null && !mails1.isEmpty()) {
            currentMaxSize = mails1.size();
            for (Map.Entry<String, List<Mail>> entry : mails1.entrySet()) {
                if (entry.getValue().size() > currentMaxSize) {
                    currentMaxSize = entry.getValue().size();
                }
            }
        }
        if (mails2 != null && !mails2.isEmpty()) {
            for (Map.Entry<String, List<Mail>> entry : mails2.entrySet()) {
                if (entry.getValue().size() > currentMaxSize) {
                    currentMaxSize = entry.getValue().size();
                }
            }
            return currentMaxSize;
        }
        return 0;
    }

    /**
     * Verifica la presenza della mail dell'utente <b>user</b> e poi elimina dal file di testo (sar&agrave; per forza
     * di cose mailBoxes.json) la mail selezionata e riscriverà la lista
     * precedente meno la mail.
     *
     * @param user: proprietario della casella di posta.
     * @param mail: mail da eliminare.
     */
    public static void deleteForever(UserID user, Mail mail) {
        if (mailDeletTmp.containsKey(user)) {
            mailDeletTmp.get(user).remove(mailDeletTmp.get(user).indexOf(mail));
            Gson gson = new Gson();
            try (FileWriter writer = new FileWriter(new File(FILE_PATH + "deletedMails" + ".json"))) {
                gson.toJson(mailDeletTmp, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Verifica la presenza della mail dell'utente <b>user</b> e poi la elimina dal file deletedMails.json per andarla
     * a scrivere sul file mailBoxes.json.
     *
     * @param owner: proprietario della casella di posta.
     * @param mail:  mail da eliminare.
     */
    public static void restore(Mail mail, UserID owner) {
        if (mailDeletTmp.containsKey(owner)) {
            Gson gson = new Gson();
            if (!mailBoxesTmp.containsKey(owner)) {
                mailBoxesTmp.put(owner, new ArrayList<>());
            }
            mailBoxesTmp.get(owner).add(mail);
            try (FileWriter writer = new FileWriter(new File(FILE_PATH + "mailBoxes" + ".json"))) {
                gson.toJson(mailBoxesTmp, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            deleteForever(owner, mail);
        }
    }

    /**
     * Elimina definitivamente la bozza <b>mail</b> dell'utente <b>owner</b>.
     *
     * @param mail:  mail da eliminare.
     * @param owner: proprietario della casella di posta.
     */
    public static void deleteDrafts(Mail mail, UserID owner) {
        if (mailDraftTmp.containsKey(owner)) {
            mailDraftTmp.get(owner).remove(mailDraftTmp.get(owner).indexOf(mail));
            Gson gson = new Gson();
            try (FileWriter writer = new FileWriter(new File(FILE_PATH + "drafts" + ".json"))) {
                gson.toJson(mailDraftTmp, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
