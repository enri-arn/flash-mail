package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.MailClient;
import model.User;
import model.UserID;
import model.exception.MailBoxException;
import org.omg.CORBA.INITIALIZE;
import view.item.SnackBar;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 * <p>
 * Controller che gestisce la scena del login.
 * Il controller fa riferimento alla grafica fxml <b>client_login_view.fxml</b>.
 * Viene implementata l'interfaccia {@link Initializable} per inizializzare i campi email e password
 * per testare al meglio l'applicazione.
 */
public class LoginController extends MainControllerClient implements Initializable {

    /**
     * Componenti del layout.
     */
    @FXML
    public TextField emailInput;
    @FXML
    public PasswordField passwordInput;
    @FXML
    public Button loginBtn;
    @FXML
    public AnchorPane mainPane;
    @FXML
    private ImageView imgEmailLogin;
    @FXML
    private ImageView imgPasswordLogin;

    /**
     * Metodo dell'interfaccia <code>{@link Initializable}</code> che garantisce che
     * tutto ci&ograve; che viene inserito nel metodo <code>{@link INITIALIZE}</code>
     * venga eseguito prima di tutto il resto.
     * In tal caso vengono predisposti di default i campi email e password.
     * Vengono associati anche gli ascoltatori ai bottoni di login e di chiusura finestra.
     *
     * @param location:  parametro necessario al metodo ma non utilizzato.
     * @param resources: parametro necessario al metodo ma non utilizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imgEmailLogin.setImage(new Image("view/img/denied.png"));
        imgPasswordLogin.setImage(new Image("view/img/denied.png"));
        emailInput.setText("a@b.com"); //da eliminare
        passwordInput.setText("Scoiattolo123!");
        closeBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
            System.exit(0);
        });
        loginBtn.setOnMouseClicked(event -> authenticateUser());
    }

    /**
     * Metodo che prevede la richiesta di autenticazione al server una volta verificati i campi in ingresso.
     * La richiesta di validazione non avverr&agrave; infatti nel caso in cui o l'email o la passowrd non rispettino
     * la validazione.
     * Nel caso in cui invece la validazione venga effettuata, verrà richiesto al {@link MainControllerClient} di
     * cambiare la scena a quella principale.
     */
    private void authenticateUser() {
        String email = emailInput.getText();
        String password = passwordInput.getText();
        emailInput.setText("a@b.com");
        if (isValidEmail(email) && isValidPassword(password)) {
            user = new User(new UserID(email), password, "test", "test");
            System.out.println("email e password sono validi! ");
            MailClient client = MainControllerClient.getInstance();
            try {
                if (client != null && client.authenticate(user)) {
                    System.out.println("client ha autenticato user ... ");
                    MainControllerClient mainControllerClient = MainControllerClient.getInstanceController();
                    mainControllerClient.switchScene("main");
                }
            } catch (RemoteException | MailBoxException e) {
                getInstanceController().switchScene("connectionLost");
            }
        } else {
            showSnakBarLogin();
            System.out.println("errore di login");
        }
    }

    private void showSnakBarLogin() {
        AnchorPane snackbar = SnackBar.display("Some field are incorrect\nTry again!!", 200, 60);
        AnchorPane.setBottomAnchor(snackbar, 2.0);
        snackbar.autosize();
        AnchorPane.setRightAnchor(snackbar, 2.0);
        mainPane.getChildren().add(snackbar);
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
     * Validazione della password con espressione regolare. La password deve contenere:
     * - min 8 caratteri;
     * - almeno una lettera maiuscola;
     * - almeno una lettera minuscola;
     * - almeno un numero;
     * - almeno un carattere speciale;
     *
     * @param password: password da validare.
     * @return vero se la validazione ha avuto sucesso, falso altrimenti.
     */
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@!%*?&])[A-Za-z\\d$@!%*?&]{8,}$");
    }

    /**
     * Gestisce la modifica dell'input dell'email. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    @FXML
    private void emailKeyPressed() {
        if (isValidEmail(emailInput.getText())) {
            imgEmailLogin.setImage(new Image("view/img/accepted.png"));
        } else {
            imgEmailLogin.setImage(new Image("view/img/denied.png"));
        }
    }

    /**
     * Gestisce la modifica dell'input dell'email. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    @FXML
    private void emailKeyReleased() {
        if (isValidEmail(emailInput.getText())) {
            imgEmailLogin.setImage(new Image("view/img/accepted.png"));
        } else {
            imgEmailLogin.setImage(new Image("view/img/denied.png"));
        }
    }

    /**
     * Gestisce la modifica dell'input della password. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    public void pswKeyPressed() {
        if (isValidPassword(passwordInput.getText())) {
            imgPasswordLogin.setImage(new Image("view/img/accepted.png"));
        } else {
            imgPasswordLogin.setImage(new Image("view/img/denied.png"));
        }
    }

    /**
     * Gestisce la modifica dell'input della password. Se l'input &egrave; corretto allora mostra l'immagine di conferma,
     * altrimenti mostra l'immagine di errore vicino all'input.
     */
    public void pswKeyReleased() {
        if (isValidPassword(passwordInput.getText())) {
            imgPasswordLogin.setImage(new Image("view/img/accepted.png"));
        } else {
            imgPasswordLogin.setImage(new Image("view/img/denied.png"));
        }
    }
}

