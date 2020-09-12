package view;

import controller.MainControllerServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import view.utils.ResizeHelp;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("java.security.policy", "file:server.policy");
        Parent root = FXMLLoader.load(getClass().getResource("server_main_view.fxml"));
        primaryStage.setTitle("FlashMail | Server");
        primaryStage.getIcons().add(new Image("view/img/logo.png"));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            MainControllerServer.getInstance().stopServer();
            System.exit(0);
        });
        primaryStage.setScene(new Scene(root, 600, 400));
        ResizeHelp.addResizeListener(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
