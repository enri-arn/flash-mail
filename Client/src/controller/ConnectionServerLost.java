package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.omg.CORBA.INITIALIZE;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 * <p>
 * Controller che gestisce la scrittura di una nuova mail, la risposta o l'inoltro ad una di quest'ultima.
 * Il controller fa riferimento alla grafica fxml <b>write_new_mail.fxml</b>.
 * Viene implementata l'interfaccia {@link Initializable} per assegarne gli ascoltatori.
 */
public class ConnectionServerLost extends MainControllerClient implements Initializable {

    /**
     * Componente del layout.
     */
    @FXML
    public ImageView closeBtnServerOffline;

    /**
     * Metodo dell'interfaccia <code>{@link Initializable}</code> che garantisce che
     * tutto ci&ograve; che viene inserito nel metodo <code>{@link INITIALIZE}</code>
     * venga eseguito prima di tutto il resto.
     * In tal caso viene assegnato un ascoltatore a closeBtnServerOffline.
     *
     * @param location:  parametro necessario al metodo ma non utilizzato.
     * @param resources: parametro necessario al metodo ma non utilizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeBtnServerOffline.setOnMouseClicked(event -> {
            Stage stage = (Stage) closeBtnServerOffline.getScene().getWindow();
            stage.close();
            System.exit(0);
        });
    }
}
