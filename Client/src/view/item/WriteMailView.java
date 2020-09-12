package view.item;

import controller.WriteMailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import model.Mail;

import java.io.IOException;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class WriteMailView {

    /**
     * Elementi da inserire nella grafica.
     */
    private Mail mail;
    private int type;

    public WriteMailView(Mail mail, int type) {
        this.mail = mail;
        this.type = type;
    }

    /**
     * Layout grafico per scrivere una nuova mail o eseguire un inoltro o risposta.
     *
     * @return: una istanza di {@link AnchorPane}.
     */
    public AnchorPane display() {
        AnchorPane pane = null;
        try {
            pane = FXMLLoader.load(getClass().getResource("../fxml/write_new_mail.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pane != null) {
            pane.setMaxWidth(10000);
            pane.setMaxHeight(10000);
            pane.setStyle("-fx-background-color: #FFFFFF;");
        }
        WriteMailController.getWriteMailControllerInstance().setComponents(mail, type);
        return pane;
    }
}
