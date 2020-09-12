package controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.MailClient;
import model.User;
import model.exception.MailBoxException;
import view.item.SnackBar;
import view.utils.ResizeHelp;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 * <p>
 * Controller principale che gestisce le varie scene
 */
public class MainControllerClient extends Application {

    /**
     * Path di riferimento per l'<code>{@link FXMLLoader}</code>.
     */
    private static final String MAIN_CLIENT_VIEW_PATH = "../view/fxml/client_main_view.fxml";
    private static final String CLIENT_LOGIN_VIEW_PATH = "../view/fxml/client_login_view.fxml";
    private static final String CONNECTION_LOST_VIEW_PATH = "../view/fxml/connection_server_lost.fxml";

    /**
     * Istanze statiche dei controller e dell'utente.
     */
    private static MailClient client;
    private static MainControllerClient instance;
    static User user;

    /**
     * Finestra principale e selettore schermate.
     */
    private Stage window;
    private ScreenController screenController;

    /**
     * Componenti del layout
     */
    @FXML
    public AnchorPane mainAnchorPane;
    @FXML
    public ImageView closeBtn;

    /**
     * Metodo di avvio dell'interfaccia grafica che attiva la schermata opportuna a seconda che sia attivo o meno
     * il server.
     * Come primo punto vengono salvate tutte le schermate nel selettore schermata <b>{@link ScreenController}</b>;
     * da qui in poi a seconda che il server sia attivo o meno saranno attivate le schermate di login o di connection lost.
     *
     * @param primaryStage: finestra principale nella quale inserire le scene.
     */
    @Override
    public void start(Stage primaryStage) {
        System.setProperty("java.security.policy", "file:client.policy");
        instance = this;
        screenController = new ScreenController();
        try {
            screenController.addScreen("login", FXMLLoader.load(getClass().getResource(CLIENT_LOGIN_VIEW_PATH)));
            screenController.addScreen("connectionLost", FXMLLoader.load(getClass().getResource(CONNECTION_LOST_VIEW_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        screenController.initialize();
        client = getInstance();
        try {
            if (client != null) {
                client.initClient();
                screenController.activate("login");
            }
        } catch (Exception e) {
            System.err.println("=== connection with client failed ===");
            screenController.activate("connectionLost");
        }
        window = primaryStage;
        window.getIcons().add(new Image("view/img/pikachu-pokemon.png"));
        window.setOnCloseRequest(event -> {
            try {
                client.stopClient(user.getEmail());
            } catch (RemoteException | MailBoxException e) {
                switchScene("connectionLost");
            }
        });
        window.setScene(ScreenController.getInstance());
        window.initStyle(StageStyle.UNDECORATED);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Metodo derivato dal pattern Singleton che prevede l'uso di una singola istanza di un oggetto, in tal
     * caso {@link MailClient}.
     *
     * @return: un'istanza di {@link MailClient}.
     */
    public static MailClient getInstance() {
        if (client == null) {
            client = new MailClient();
        }
        return client;
    }

    /**
     * Metodo derivato dal pattern Singleton che prevede l'uso di una singola istanza di un oggetto, in tal
     * caso {@link MainControllerClient}
     *
     * @return: una istanza di {@link MainControllerClient}.
     */
    public static MainControllerClient getInstanceController() {
        return instance;
    }

    /**
     * Metodo che permette di cambire la scena all'interno dello stage <b>window</b>.
     *
     * @param name: nome chiave della scena da attivare.
     */
    public void switchScene(String name) {
        try {
            ResizeHelp.addResizeListener(window);
            screenController.addScreen("main", FXMLLoader.load(getClass().getResource(MAIN_CLIENT_VIEW_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        screenController.activate(name);
        window.setScene(ScreenController.getInstance());
        if (name.equals("main")) {
            window.setHeight(600);
            window.setWidth(1000);
            window.setX(150);
            window.setY(80);
        } else {
            window.setHeight(400);
            window.setWidth(600);
            window.setX(150);
            window.setY(150);
        }
    }

    void switchToConnectionLost(){
        screenController = new ScreenController();
        try {
            screenController.addScreen("connectionLost", FXMLLoader.load(getClass().getResource(CONNECTION_LOST_VIEW_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        screenController.activate("connectionLost");
        ScreenController.getInstance().getWindow().setWidth(600);
        ScreenController.getInstance().getWindow().setHeight(400);
    }

    /**
     * Mostra una snakbar sul <b>mainAnchorPane</b> ancorandola in basso a destra.
     *
     * @param title: stringa al'interno della notifica.
     */
    void showSnakBar(String title) {
        AnchorPane snackbar = SnackBar.display(title, 280, 80);
        AnchorPane.setBottomAnchor(snackbar, 10.0);
        AnchorPane.setRightAnchor(snackbar, 10.0);
        mainAnchorPane.getChildren().add(snackbar);
    }

    /**
     * Restituisce la finestra principale.
     *
     * @return: una istanza di stage.
     */
    public Stage getWindow() {
        return window;
    }
}
