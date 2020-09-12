package controller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import model.MailServer;
import persistence.LogWriter;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo.
 */
public class MainControllerServer implements Initializable, Observer {

    /**
     * Parametri del controller.
     */
    private MailServer server;
    private StringBuilder logString = new StringBuilder();
    private StringBuilder currentLogString = new StringBuilder();

    /**
     * Componenti del layout.
     */
    @FXML
    private TextArea logServer;
    @FXML
    private TextArea currentLogServer;

    /**
     * Istanza unica del MainControllerServer secondo il pattern <b>Singleton</b>.
     */
    private static MainControllerServer instance;

    public static MainControllerServer getInstance() {
        return instance;
    }

    /**
     * Richiama il metodo dello skeleton per far partire il servizio.
     */
    public void startServer() {
        server = MailServer.getInstance();
        try {
            if (server != null && !server.isActiveServer()) {
                server.start();
            }
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Richiama il metodo dello skeleton per interrompere il servizio.
     */
    public void stopServer() {
        if (server != null && server.isActiveServer()) {
            try {
                server.stop();
            } catch (RemoteException | NotBoundException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Preparazione del Controller che include la definizione dell'istanza come osservatore del LogWriter.
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        LogWriter logWriter = LogWriter.getInstance();
        logWriter.addObserver(this);
        List<String> log = LogWriter.getLog();
        logServer.textProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> logServer.setScrollTop(Double.MAX_VALUE));
        if (log != null && !log.isEmpty()) {
            for (String entry : log) {
                logString.append(entry).append("\n");
            }
            logServer.setText(String.valueOf(logString));
            logServer.appendText("");
        }
    }

    /**
     * Non appena il log del server viene aggiornato le modifiche vengono mostrate a video.
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof String) {
            currentLogString.append(arg).append("\n");
            String string = String.valueOf(currentLogString);
            currentLogServer.setText(string);
            currentLogServer.appendText("");
        }
    }

    /**
     * Pulisce le stampe nel log della sessione corrente.
     */
    public void clearLog() {
        currentLogServer.setText("");
        currentLogServer.appendText("");
        currentLogString = new StringBuilder();
    }

}
