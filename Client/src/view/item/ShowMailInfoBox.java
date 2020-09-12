package view.item;

import controller.ShowMailInfoController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import model.Mail;

import java.io.IOException;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class ShowMailInfoBox {

    /**
     * Elementi da inserire nella grafica.
     */
    private Mail mail;
    private boolean isDelete;

    public ShowMailInfoBox(Mail mail, boolean isDelete) {
        this.mail = mail;
        this.isDelete = isDelete;
    }

    /**
     * Layout grafico per scrivere una nuova mail, per eseguire un inoltro o una risposta.
     *
     * @return: una istanza di {@link AnchorPane}.
     */
    public AnchorPane display() {
        AnchorPane pane = null;
        try {
            pane = FXMLLoader.load(getClass().getResource("../fxml/show_mail.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pane != null) {
            pane.setMaxWidth(10000);
            pane.setMaxHeight(10000);
            pane.setStyle("-fx-background-color: #FFFFFF;");
        }
        ShowMailInfoController.getShowMailControllerInstance().setComponents(mail);
        return pane;
    }
}
