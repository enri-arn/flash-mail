package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Mail;
import model.Message;
import model.UserID;
import model.exception.MalformedMailException;
import org.omg.CORBA.INITIALIZE;
import view.item.SnackBar;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 * <p>
 * Controller che gestisce la scrittura di una nuova mail, la risposta o l'inoltro a una di quest'ultima.
 * Il controller fa riferimento alla grafica fxml <b>write_new_mail.fxml</b>.
 * Viene implementata l'interfaccia {@link Initializable} per inizializzare l'istanza del controller.
 */
public class WriteMailController extends MainControllerClient implements Initializable {

    /**
     * Istanza del controller
     */
    private static WriteMailController instance;

    /**
     * Componenti del layout
     */
    @FXML
    public TextField subjectInput;
    @FXML
    public TextField carbonCopyInput;
    @FXML
    public TextField mailReceiverInput;
    @FXML
    public TextArea messageInput;
    @FXML
    public Label lblFrom;
    @FXML
    public ImageView img_to;
    @FXML
    public ImageView img_Cc;

    /**
     * Istanza di mail che rappresenta la mail precedente in caso di reply o reply all.
     */
    private Mail previous;

    /**
     * Metodo del pattern <b>Singleton</b> per permettere l'uso di una singola istanza del controller.
     *
     * @return: una istanza del controller {@link WriteMailController}.
     */
    public static WriteMailController getWriteMailControllerInstance() {
        if (instance == null) {
            instance = new WriteMailController();
        }
        return instance;
    }

    /**
     * Metodo dell'interfaccia <code>{@link Initializable}</code> che garantisce che
     * tutto ci&ograve; che viene inserito nel metodo <code>{@link INITIALIZE}</code>
     * verr&agrave; eseguito prima di tutto il resto.
     * In tal caso viene inizializzata l'istanza del controller.
     *
     * @param location:  parametro necessario al metodo ma non utilizzato.
     * @param resources: parametro necessario al metodo ma non utilizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
    }

    /**
     * Metodo per andare a inizializzare i vari componenti grafici.
     * La mail rappresenta, nel caso in cui non sia null, la mail precendente, ad esempio nel caso di una reply.
     * Il tipo rappresenta invece l'azione che abbiamo scelto di effettuare ed &egrave; diviso come segue:
     * - 1: caso <b>reply</b>:
     * In questo caso verranno impostati sia l'oggetto che il destinatario della mail.
     * - 2: caso <b>reply all</b>:
     * In questo caso verranno impostati l'oggetto, il destinatario, i cc e la mail previous.
     * - 3: caso <b>forward</b>:
     * In questo caso verr&agrave; solamente impostato l'oggetto della mail.
     * - 4: caso <b>drafts</b>:
     * In questo caso vengono settati tutti i componenti presenti.
     * In ogni caso sar&agrave; possibile modificare tutti i parametri in quanto inseriti in {@link TextField}.
     *
     * @param mail: mail dalla quale prendere i dati ove necessari.
     * @param type: tipologia di azione.
     */
    public void setComponents(Mail mail, int type) {
        this.lblFrom.setText(user.getEmail().getValue());
        switch (type) {
            case 1:
                this.subjectInput.setText("Re: " + mail.getSubject());
                this.mailReceiverInput.setText(mail.getSender().getValue());
                img_to.setVisible(false);
                mailReceiverInput.setEditable(false);
                previous = mail;
                break;
            case 2:
                this.subjectInput.setText("Re all: " + mail.getSubject());
                this.mailReceiverInput.setText(mail.getSender().getValue());
                img_to.setVisible(false);
                mailReceiverInput.setEditable(false);
                previous = mail;
                if (mail.getCarbonCopy() != null && !mail.getCarbonCopy().isEmpty()) {
                    this.carbonCopyInput.setText(setCarbonCopy(mail));
                }
                break;
            case 3:
                this.subjectInput.setText("Fw: " + mail.getSubject());
                break;
            case 4:
                this.subjectInput.setText(mail.getSubject());
                this.mailReceiverInput.setText(mail.getSender().getValue());
                img_to.setVisible(false);
                mailReceiverInput.setEditable(false);
                if (mail.getCarbonCopy() != null && !mail.getCarbonCopy().isEmpty()) {
                    final String[] Cc = {""};
                    mail.getCarbonCopy().forEach(c -> Cc[0] += c.getValue() + ", ");
                    this.carbonCopyInput.setText(Cc[0]);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Validazione tramite regex del campo email.
     *
     * @param entry: stringa da valutare.
     * @return: valore booleano positivo o negativo a seconda della validazione.
     */
    private boolean isValid(String entry) {
        return entry.matches("\\S+@\\S+\\.\\S+");
    }

    /**
     * Verifica la validit&agrave; e la presenza di tutti i campi necessari per la mail.
     * In caso la verifica sia corretta prende l'istanza dal model del {@link model.MailClient} per l'invio
     * effettivo della mail.
     */
    public void sendMail() {
        String receiver = mailReceiverInput.getText();
        String newString = carbonCopyInput.getText().replaceAll("\\s+", "");
        String[] Cc = newString.split("(,|,\\s)");
        String subject = subjectInput.getText();
        String message = messageInput.getText();
        if (receiver != null && !receiver.isEmpty() && isValid(receiver) && message != null && !message.isEmpty()) {
            System.out.println("Mail to : " + receiver + " subject = " + subject + " Cc " + Arrays.toString(Cc) + " message " + message);
            List<UserID> carbonCopy = null;
            if (Cc.length > 0) {
                carbonCopy = new ArrayList<>();
                for (String aCc : Cc) {
                    if (isValid(aCc)) {
                        carbonCopy.add(new UserID(aCc));
                    }
                }
            }
            int id = 0;
            try {
                id = getInstance().getID();
            } catch (RemoteException e) {
                getInstanceController().switchScene("connectionLost");
            }
            Mail mail = new Mail(id, user.getEmail(), new UserID(receiver), carbonCopy, subject, new Message(message, new Date()), previous);
            try {
                getInstance().send(mail);
            } catch (RemoteException e) {
                getInstanceController().switchScene("connectionLost");
            } catch (MalformedMailException e) {
                e.printStackTrace();
            }
            MainViewController.getControllerInstance().showAllMail();
        } else {
            Platform.runLater(() -> SnackBar.display("Message not correct", 80, 280));
        }
    }

    /**
     * Verifica la validit&agrave; e la presenza di tutti i campi necessari per la mail.
     * In caso la verifica sia corretta prende l'istanza dal model del {@link model.MailClient} per l'aggiunta
     * effettiva della mail alle bozze dell'utente.
     */
    public void saveMail() {
        String receiver = mailReceiverInput.getText();
        String newString = carbonCopyInput.getText().replaceAll("\\s+", "");
        String[] Cc = newString.split("(,|,\\s)");
        System.out.println(Arrays.toString(Cc));
        String subject = subjectInput.getText();
        String message = messageInput.getText();
        if (receiver != null && isValid(receiver) && message != null && !message.isEmpty()) {
            List<UserID> carbonCopy = null;
            if (Cc.length > 0) {
                carbonCopy = new ArrayList<>();
                for (String aCc : Cc) {
                    System.out.println(aCc);
                    if (isValid(aCc))
                        carbonCopy.add(new UserID(aCc));
                }
                System.out.println("cc size = " + carbonCopy.size());
            }
            int id = 0;
            try {
                id = getInstance().getID();
            } catch (RemoteException e) {
                getInstanceController().switchScene("connectionLost");
            }
            Mail mail = new Mail(id, user.getEmail(), new UserID(receiver), carbonCopy, subject, new Message(message, new Date()), previous);
            try {
                Platform.runLater(this::showSnakBarDraft);
                getInstance().addToDrafts(mail);
            } catch (RemoteException e) {
                getInstanceController().switchScene("connectionLost");
            }
        }
    }

    /**
     * Mostra una snakbar sul <b>centerAnchorPane</b> ancorandola in basso a destra.
     */
    private void showSnakBarDraft() {
        MainViewController.getControllerInstance().showSnakBarDrafts();
    }

    /**
     * Validazione della email con una espressione regolare.
     *
     * @param email: email da verificare.
     * @return vero se la validazione ha avuto sucesso, falso altrimenti.
     */
    private boolean isValidEmail(String email) {
        return email.matches("\\S+@\\S+\\.\\S+");
    }

    /**
     * Validazione tramite regex del carbon copy.
     */
    private boolean isValidCc(String cc) {
        String newString = cc.replaceAll("\\s+", "");
        String Cc[] = newString.split("(,|,\\s)");
        boolean isValid = true;
        for (String cC : Cc) {
            if (!isValidEmail(cC)) {
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Gestisce la modifica dell'input dell'email. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    public void toEmailPressed() {
        if (isValidEmail(mailReceiverInput.getText())) {
            img_to.setImage(new Image("view/img/accepted.png"));
        } else {
            img_to.setImage(new Image("view/img/denied.png"));
        }
    }

    /**
     * Gestisce la modifica dell'input del Cc. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    public void ccEmailPressed() {
        if (carbonCopyInput.getText().equals("")) {
            img_Cc.setVisible(false);
        } else {
            img_Cc.setVisible(true);
        }
        if (isValidCc(carbonCopyInput.getText())) {
            img_Cc.setImage(new Image("view/img/accepted.png"));
        } else {
            img_Cc.setImage(new Image("view/img/denied.png"));
        }
    }

    /**
     * Gestisce la modifica dell'input dell'email. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    public void toEmailReleased() {
        if (isValidEmail(mailReceiverInput.getText())) {
            img_to.setImage(new Image("view/img/accepted.png"));
        } else {
            img_to.setImage(new Image("view/img/denied.png"));
        }
    }

    /**
     * Gestisce la modifica dell'input del Cc. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    public void ccEmailReleased() {
        if (carbonCopyInput.getText().equals("")) {
            img_Cc.setVisible(false);
        } else {
            img_Cc.setVisible(true);
        }
        if (isValidCc(carbonCopyInput.getText())) {
            img_Cc.setImage(new Image("view/img/accepted.png"));
        } else {
            img_Cc.setImage(new Image("view/img/denied.png"));
        }
    }

    /**
     * Setta i carbon copy della mail.
     *
     * @param carbonCopy: mail da cui ricavarei dati.
     * @return una stringa contenente i carbon copy.
     */
    private String setCarbonCopy(Mail carbonCopy) {
        StringBuilder Cc = new StringBuilder();
        if (!user.getEmail().equals(carbonCopy.getReceiver())) {
            Cc.append(carbonCopy.getReceiver().getValue()).append(", ");
        }
        for (UserID cc : carbonCopy.getCarbonCopy()) {
            if (!user.getEmail().equals(cc)) {
                Cc.append(cc.getValue()).append(", ");
            }
        }
        return Cc.toString();
    }
}
