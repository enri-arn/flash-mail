package view.item;

import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class SnackBar {

    /**
     * Metodo per mostrare a video una notifica.
     *
     * @param message: Stringa da inserire nella notifica.
     * @return: una istanza di {@link AnchorPane}.
     */
    public static AnchorPane display(String message, int width, int height) {
        AnchorPane pane = new AnchorPane();
        Label msg = new Label(message);
        msg.setTextFill(Paint.valueOf("#FFFFFF"));
        msg.setStyle("-fx-font-family: Verdana;" +
                "-fx-font-size: 12;");
        Label ok = new Label("OK");
        ok.setOnMouseClicked(event -> pane.setVisible(false));
        ok.setTextFill(Paint.valueOf("#FFC107"));
        ok.setStyle("-fx-font-family: Verdana;" +
                "-fx-font-size: 16;");
        pane.setPrefHeight(height);
        pane.setPrefWidth(width);
        pane.setStyle("-fx-background-color: #212121");
        AnchorPane.setTopAnchor(msg, 15.0);
        AnchorPane.setLeftAnchor(msg, 30.0);
        AnchorPane.setBottomAnchor(ok, 5.0);
        AnchorPane.setRightAnchor(ok, 5.0);
        pane.getChildren().addAll(msg, ok);

        return pane;
    }

}

