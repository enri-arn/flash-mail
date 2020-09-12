package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import model.Mail;
import org.omg.CORBA.INITIALIZE;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller che gestisce il layout <b>show_mail.fxml</b>.
 * Il controller si occupa di andare a definire i componenti del layout.
 */
public class ShowMailInfoController extends MainViewController implements Initializable {

    /**
     * Istanza del controller
     */
    private static ShowMailInfoController instance;

    /**
     * Componenti del layout.
     */
    @FXML
    public Label mailReceiverInput;
    @FXML
    public Label carbonCopyInput;
    @FXML
    public Label subjectInput;
    @FXML
    public Label messageInput;
    @FXML
    public Label lblFrom;
    @FXML
    public Label dateInput;

    /**
     * Altre variabili di appoggio.
     */
    private static Mail mail;

    public static ShowMailInfoController getShowMailControllerInstance() {
        if (instance == null) {
            instance = new ShowMailInfoController();
        }
        return instance;
    }

    /**
     * @return l'istanza della mail.
     */
    public static Mail getMail() {
        return mail;
    }

    /**
     * Metodo dell'interfaccia <code>{@link Initializable}</code> che garantisce che
     * tutto ciò che viene inserito nel metodo <code>{@link INITIALIZE}</code>
     * verrà eseguito prima di tutto il resto.
     * In tal caso inizializza l'istanza della mail.
     *
     * @param location:  parametro necessario al metodo ma non utilizzato.
     * @param resources: parametro necessario al metodo ma non utilizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
    }

    /**
     * Setta i componenti del layout.
     *
     * @param mail: mail da inserire nel layout.
     */
    public void setComponents(Mail mail) {
        ShowMailInfoController.mail = mail;
        mailReceiverInput.setText(mail.getReceiver().getValue());
        lblFrom.setText(mail.getSender().getValue());
        messageInput.setText(mail.getMessage().getMessage());
        subjectInput.setText(mail.getSubject());
        dateInput.setText(mail.getMessage().getDate().toString());
        if (!mail.getCarbonCopy().isEmpty()) {
            final String[] CcString = {""};
            mail.getCarbonCopy().forEach(Cc -> CcString[0] += Cc.getValue() + ", ");
            carbonCopyInput.setText(CcString[0]);
        } else {
            carbonCopyInput.setText("no other person receive this mail ...");
        }
    }

}
