package view.item;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import model.Mail;
import sun.security.krb5.internal.PAData;

import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailBoxItem {

    /**
     * Elementi da inserire nella grafica.
     */
    private Mail mail;
    private String[] colors = {"#4DB6AC", "#26A69A", "#009688", "#00897B", "#00796B", "#00695C", "#004D40", "#00838F", "#006064", "#0097A7", "#00ACC1", "#66BB6A", "#827717", "#00C853", "#8BC34A", "#4CAF50"};

    public MailBoxItem(Mail mail) {
        this.mail = mail;
    }

    /**
     * Metodo che ritorna un panello contenente le informazioni della mail.
     *
     * @param isRead: booleano che rappresenta mail non lette se &egrave; true, mail lette altrimenti.
     * @return: una istanza di {@link AnchorPane}.
     */
    public AnchorPane display(boolean isRead) {
        AnchorPane pane = new AnchorPane();
        pane.setPrefHeight(100);
        pane.setPrefWidth(2000);
        Label emailLbl = new Label(this.mail.getSender().getValue());
        emailLbl.setStyle("-fx-font-family: Verdana;" +
                "-fx-font-size: 16;");
        AnchorPane.setTopAnchor(emailLbl, 7.0);
        AnchorPane.setLeftAnchor(emailLbl, 60.0);
        Label subjectLbl = new Label(this.mail.getSubject());
        subjectLbl.setTextFill(Paint.valueOf("#757575"));
        subjectLbl.setStyle("-fx-font-family: Verdana;" +
                "-fx-font-size: 14;");
        AnchorPane.setTopAnchor(subjectLbl, 30.0);
        AnchorPane.setLeftAnchor(subjectLbl, 60.0);
        String capitalLetter = String.valueOf(this.mail.getSender().getValue().charAt(0)).toUpperCase();
        Label letter = new Label(capitalLetter);
        letter.setPrefWidth(44);
        letter.setPrefHeight(44);
        letter.setPadding(new Insets(4, 4, 4, 4));
        letter.setTextFill(Paint.valueOf("#FFFFFF"));
        letter.setAlignment(Pos.CENTER);
        int index = new Random().nextInt(colors.length);
        String color = colors[index];
        letter.setStyle("-fx-font-color: #FFFFFF;" +
                "-fx-font-size: 20;" +
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 8em;" +
                "-fx-background-position: center center;");
        AnchorPane.setTopAnchor(letter, 7.0);
        AnchorPane.setLeftAnchor(letter, 7.0);
        String dateString = new SimpleDateFormat("yyyy-MM-dd  HH:mm").format(this.mail.getMessage().getDate());
        Label lblDate = new Label(dateString);
        lblDate.setStyle("-fx-font-family: Verdana;" +
                "-fx-font-size: 11;");
        lblDate.setTextFill(Paint.valueOf("#b7b7b7"));
        AnchorPane.setTopAnchor(lblDate, 7.0);
        AnchorPane.setLeftAnchor(lblDate, 380.0);
        Label messageLbl = new Label(this.mail.getMessage().getMessage());
        messageLbl.setWrapText(true);
        messageLbl.setTextFill(Paint.valueOf("#a7a7a7"));
        messageLbl.setStyle("-fx-font-size: 10;" +
                "-fx-font-family: Verdana;");
        AnchorPane.setTopAnchor(messageLbl, 60.0);
        AnchorPane.setLeftAnchor(messageLbl, 60.0);
        AnchorPane.setRightAnchor(messageLbl, 15.0);
        messageLbl.setEllipsisString("...");
        messageLbl.setTextOverrun(OverrunStyle.ELLIPSIS);
        messageLbl.setMaxSize(850, 40);
        if (!isRead) {
            emailLbl.setStyle("-fx-font-family: Verdana;" +
                    "-fx-font-size: 16;" +
                    "-fx-font-weight: bold");
            subjectLbl.setTextFill(Paint.valueOf("#000000"));
            subjectLbl.setStyle("-fx-font-family: Verdana;" +
                    "-fx-font-size: 14;" +
                    "");
            messageLbl.setTextFill(Paint.valueOf("#020202"));
            messageLbl.setStyle("-fx-font-family: Verdana;" +
                    "-fx-font-weight: bold");
            lblDate.setTextFill(Paint.valueOf("#020202"));
            lblDate.setStyle("-fx-font-family: Verdana;" +
                    "-fx-font-weight: bold");
        }
        pane.getChildren().addAll(emailLbl, subjectLbl, letter, messageLbl, lblDate);
        pane.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #d7d7d7;" +
                "-fx-border-width: 0 0 1 0;");
        return pane;
    }
}
