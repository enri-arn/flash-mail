package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Mail;
import model.exception.MailBoxException;
import org.omg.CORBA.INITIALIZE;
import view.item.MailBoxItem;
import view.item.ShowMailInfoBox;
import view.item.SnackBar;
import view.item.WriteMailView;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;


/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 * <p>
 * Controller principale del pannello centrale <b>mainAnchorPane</b> che permette di controllare tutto ci&ograve; che
 * riguarda la visualizzazione di:
 * - tutta la posta;
 * - mail non lette;
 * - mail gi&agrave; lette;
 * - mail spedite;
 * - bozze.
 * Il controller implementa l'interfaccia {@link Initializable}, che permette di mostrare all'inizio la sezione di tutta la
 * posta e l'interfaccia {@link Observer} che permette, tramite il metodo update, di notificare l'arrivo di una nuova
 * mail.
 */
public class MainViewController extends MainControllerClient implements Initializable, Observer {

    /**
     * Istanza del controller.
     */
    private static MainViewController instance;

    /**
     * Variabili per gestire lo spostamento della finestra.
     */
    private static double xOffset = 0;
    private static double yOffset = 0;

    /**
     * Valori booleani per effetti grafici di hover del mouse.
     */
    private boolean isOpenMenu = true;
    private boolean isHoverClose = false;
    private boolean paneOpen = false;

    /**
     * Componenti del layout
     */
    @FXML
    public AnchorPane centerAnchorPane;
    @FXML
    public ImageView closeBtnMainView;
    @FXML
    public ImageView menuOpenCloseBtn;
    @FXML
    public Label titleLbl;
    @FXML
    public Label titleMenuSelect;
    @FXML
    public Label newMailLbl;
    @FXML
    public ImageView newMailImg;
    @FXML
    public Label allMailLbl;
    @FXML
    public ImageView allMailImg;
    @FXML
    public GridPane navigationDrawer;
    @FXML
    public AnchorPane leftAnchorPane;
    @FXML
    public ImageView leftArrow;
    @FXML
    public Label deletedMailsLbl;
    @FXML
    public ImageView deletedMailsImg;
    @FXML
    public Label draftsLbl;
    @FXML
    public ImageView draftsImg;
    @FXML
    public Label sentMailLbl;
    @FXML
    public ImageView sentMailImg;
    @FXML
    public Label readMailLbl;
    @FXML
    public ImageView readMailImg;
    @FXML
    public Label lblAccount;

    /**
     * Altre variabili di supporto.
     */
    private VBox mailsVbox;
    private Button writeMailBtn;
    private boolean inWriteMail = false;
    private boolean inShowMail = false;

    /**
     * Metodo del pattern <b>Singleton</b> per permettere l'uso di una singola istanza del controller.
     *
     * @return: una istanza del controller {@link MainViewController}.
     */
    public static MainViewController getControllerInstance() {
        if (instance == null) {
            instance = new MainViewController();
        }
        return instance;
    }

    /**
     * Metodo dell'interfaccia <code>{@link Initializable}</code> che garantisce che
     * tutto ciò che viene inserito nel metodo <code>{@link INITIALIZE}</code>
     * verrà eseguito prima di tutto il resto.
     * In tal caso nel metdo sono presenti due assegnamenti di larghezza preferita per
     * quanto rigurarda il menù laterale.
     *
     * @param location:  parametro necessario al metodo ma non utilizzato.
     * @param resources: parametro necessario al metodo ma non utilizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        getInstance().addObserver(this);
        centerAnchorPane.setPrefWidth(600);
        leftArrow.setVisible(false);
        lblAccount.setText(user.getEmail().getValue());
        addToCenterAnchorPane();
        List<Mail> readMails = null;
        List<Mail> notReadMails = null;
        try {
            readMails = getInstance().checkMails() == null ? new ArrayList<>() : getInstance().checkMails();
            notReadMails = getInstance().checkPendingMails() == null ? new ArrayList<>() : getInstance().checkPendingMails();
        } catch (RemoteException e){
            switchToConnectionLost();
        }
        if ((readMails == null || readMails.isEmpty()) && (notReadMails == null || notReadMails.isEmpty())) {
            mailsVbox.getChildren().addAll(noMails("There are no mails"));
        } else {
            mailsVbox.getChildren().addAll(showAllMailBoxes(Objects.requireNonNull(notReadMails), false, false, false));
            mailsVbox.getChildren().addAll(showAllMailBoxes(Objects.requireNonNull(readMails), true, false, false));
        }
    }

    /**
     * Aggiunge al pannello centrale (<b>centerAnchorPane</b>) una Vbox per inserire le mail una sotto l'altra, uno
     * ScrollPane per poterle visualizzare e il bottone per scrivere un nuovo messaggio.
     */
    private void addToCenterAnchorPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(false);
        scrollPane.setHmin(0);
        scrollPane.setVmin(0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane paneIntoScroll = new AnchorPane();
        mailsVbox = new VBox();
        AnchorPane.setLeftAnchor(mailsVbox, 0.0);
        AnchorPane.setBottomAnchor(mailsVbox, 0.0);
        AnchorPane.setRightAnchor(mailsVbox, 0.0);
        AnchorPane.setTopAnchor(mailsVbox, 0.0);
        paneIntoScroll.getChildren().addAll(mailsVbox);
        scrollPane.setContent(paneIntoScroll);
        writeMailBtn = new Button();
        String style = "    -fx-background-image: url(\"/view/img/pencil.png\");\n" +
                "    -fx-background-size: 30 30;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center center;\n" +
                "    -fx-background-color: #ffc107;\n" +
                "    -fx-background-radius: 5em;";
        DropShadow dropShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.valueOf("#000000"), 8.25, 0.0, 1.0, 1.0);
        dropShadow.setHeight(15.0);
        dropShadow.setWidth(20.0);
        writeMailBtn.setStyle(style);
        writeMailBtn.setPrefWidth(48);
        writeMailBtn.setPrefHeight(48);
        writeMailBtn.setEffect(dropShadow);
        AnchorPane.setBottomAnchor(writeMailBtn, 15.0);
        AnchorPane.setRightAnchor(writeMailBtn, 20.0);
        writeMailBtn.setOnAction(e -> writeMail());
        centerAnchorPane.getChildren().addAll(scrollPane, writeMailBtn);
    }

    /**
     * Visualizza a video le varie mail mostrando gli elementi basilari.
     *
     * @param mails:    lista di mail da visualizzare.
     * @param isRead:   booleano positivo se la mail è letta.
     * @param isDelete: booleano positivo se la mail è cncellata.
     * @param isDrafts: booleano che rappresenta la presenza di una bozza se positivo.
     * @return una lista di istanze di {@link AnchorPane}.
     */
    private List<AnchorPane> showAllMailBoxes(List<Mail> mails, boolean isRead, boolean isDelete, boolean isDrafts) {
        mails.sort(Comparator.comparing(a -> a.getMessage().getDate()));
        Collections.reverse(mails);
        List<AnchorPane> panes = new ArrayList<>();
        for (Mail entry : mails) {
            AnchorPane pane = new MailBoxItem(entry).display(isRead);
            pane.setOnMouseClicked(e -> mousePaneClickListener(entry, isRead, isDelete, isDrafts));
            panes.add(pane);
        }
        return panes;
    }

    /**
     * Gestisce il click sul pannello della mail. Mostra le informazioni della mail in modo pi&ugrave; dettagliato
     * andando ad utilizzare la view <b>show_mail.fxml</b>.
     *
     * @param entry:    mail da visualizzare in modo dettagliato.
     * @param isRead:   booleano che rappresenta se è già letta o meno.
     * @param isDelete: booleano che rappresenta se fa parte delle mail eliminate o meno.
     */
    private void mousePaneClickListener(Mail entry, boolean isRead, boolean isDelete, boolean isDrafts) {
        centerAnchorPane.getChildren().clear();
        inShowMail = true;
        if (!isRead) {
            try {
                getInstance().deletePending(entry);
            } catch (RemoteException e) {
                switchToConnectionLost();
            }
        }
        List<AnchorPane> previousMail = new ArrayList<>();
        Mail prev = entry.getPrevious();
        while (prev != null) {
            previousMail.add(new MailBoxItem(prev).display(isRead));
            prev = prev.getPrevious();
        }
        previousMail.forEach(panel -> panel.setOnMouseClicked(e -> {
            if (!paneOpen) {
                panel.setPrefHeight(200);
                paneOpen = true;
            } else {
                panel.setPrefHeight(100);
                paneOpen = false;
            }
        }));
        addToView(previousMail, entry, isDelete, isDrafts);
    }

    /**
     * Metodo di supporto per aggiungere alla vista <b>show_mail.fxml</b> i bottoni flottanti per le varie azioni.
     *
     * @param previousMail: mail precedente.
     * @param entry:        mail attuale.
     * @param isDelete:     booleano che rappresenta se fa parte delle mail eliminate o meno.
     * @param isDrafts:     booleano che se positivo rappresenta la presenza di una bozza.
     */
    private void addToView(List<AnchorPane> previousMail, Mail entry, boolean isDelete, boolean isDrafts) {
        leftArrow.setVisible(true);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(false);
        scrollPane.setHmin(0);
        scrollPane.setVmin(0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        ShowMailInfoBox showMailInfoBox = new ShowMailInfoBox(entry, isDelete);
        AnchorPane showInfoBox = showMailInfoBox.display();
        AnchorPane.setRightAnchor(showInfoBox, 0.0);
        AnchorPane.setLeftAnchor(showInfoBox, 0.0);
        AnchorPane.setBottomAnchor(showInfoBox, 0.0);
        VBox vBox = new VBox();
        if (previousMail != null && !previousMail.isEmpty()) {
            vBox.getChildren().addAll(previousMail);
            AnchorPane.setTopAnchor(vBox, 0.0);
            AnchorPane.setRightAnchor(vBox, 0.0);
            AnchorPane.setLeftAnchor(vBox, 0.0);
        }
        vBox.getChildren().addAll(showInfoBox);
        scrollPane.setContent(vBox);
        centerAnchorPane.getChildren().addAll(scrollPane);
        if (!isDelete & !isDrafts) {
            addShowMailInfoButton();
        } else if (isDrafts) {
            addDraftsButton(entry);
        } else {
            addRestoreMailButton(entry);
        }
    }

    /**
     * Aggiunge alla grafica quando si visualizza una mail eliminata i bottoni per eliminare definitivamente o recuperare
     * il messaggio.
     *
     * @param mail: mail usata per i listener dei bottoni.
     */
    private void addRestoreMailButton(Mail mail) {
        String style = "    -fx-background-size: 30 30;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center center;\n" +
                "    -fx-background-color: #ffc107;\n" +
                "    -fx-background-radius: 5em;";
        DropShadow dropShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.valueOf("#000000"), 8.25, 0.0, 1.0, 1.0);
        dropShadow.setHeight(15.0);
        dropShadow.setWidth(20.0);
        Button deleteForever = new Button();
        Button restore = new Button();
        deleteForever.setEffect(dropShadow);
        restore.setEffect(dropShadow);
        deleteForever.setTooltip(new Tooltip("Delete forever"));
        restore.setTooltip(new Tooltip("Restore mail"));
        deleteForever.setPrefWidth(48);
        deleteForever.setPrefHeight(48);
        restore.setPrefWidth(36);
        restore.setPrefHeight(36);
        deleteForever.setStyle("-fx-background-image: url(\"view/img/delete-forever.png\");" + style);
        restore.setStyle(style + "-fx-background-image: url(\"view/img/delete-restore.png\");\n" +
                "    -fx-background-size: 20 20;" +
                "-fx-background-color: #009688;");
        AnchorPane.setBottomAnchor(deleteForever, 20.0);
        AnchorPane.setRightAnchor(deleteForever, 15.0);
        AnchorPane.setBottomAnchor(restore, 26.0);
        AnchorPane.setRightAnchor(restore, 80.0);
        deleteForever.setOnAction(e -> {
            try {
                getInstance().deleteForever(mail);
            } catch (RemoteException e1) {
                switchToConnectionLost();
            }
            showDeletedMail();
        });
        restore.setOnAction(e -> {
            try {
                getInstance().restore(mail);
            } catch (RemoteException e1) {
                switchToConnectionLost();
            }
            showAllMail();
        });
        centerAnchorPane.getChildren().addAll(deleteForever, restore);
    }

    /**
     * Aggiunge i bottoni alla grafica della visualizzazione classica della mail e assegna loro i listener.
     */
    private void addShowMailInfoButton() {
        String style = "    -fx-background-size: 30 30;" +
                "    -fx-background-repeat: no-repeat;" +
                "    -fx-background-position: center center;" +
                "    -fx-background-color: #ffc107;" +
                "    -fx-background-radius: 5em;";
        DropShadow dropShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.valueOf("#000000"), 8.25, 0.0, 1.0, 1.0);
        dropShadow.setHeight(15.0);
        dropShadow.setWidth(20.0);
        Button reply = new Button();
        Button replyAll = new Button();
        Button forward = new Button();
        Button delete = new Button();
        reply.setEffect(dropShadow);
        replyAll.setEffect(dropShadow);
        forward.setEffect(dropShadow);
        delete.setEffect(dropShadow);
        reply.setTooltip(new Tooltip("Reply"));
        replyAll.setTooltip(new Tooltip("Reply all"));
        forward.setTooltip(new Tooltip("Forward message"));
        delete.setTooltip(new Tooltip("Delete message"));
        reply.setPrefWidth(48);
        reply.setPrefHeight(48);
        replyAll.setPrefWidth(36);
        replyAll.setPrefHeight(36);
        forward.setPrefWidth(36);
        forward.setPrefHeight(36);
        delete.setPrefWidth(36);
        delete.setPrefHeight(36);
        reply.setStyle(style + "-fx-background-image: url(\"view/img/reply(1).png\");");
        replyAll.setStyle(style + "-fx-background-image: url(\"view/img/reply-all(1).png\");");
        forward.setStyle(style + "-fx-background-image: url(\"view/img/forward(1).png\");");
        delete.setStyle(style + "-fx-background-image: url(\"view/img/delete(1).png\");" +
                "-fx-background-color: #009688;");
        AnchorPane.setBottomAnchor(reply, 20.0);
        AnchorPane.setRightAnchor(reply, 15.0);
        AnchorPane.setBottomAnchor(replyAll, 26.0);
        AnchorPane.setRightAnchor(replyAll, 80.0);
        AnchorPane.setBottomAnchor(forward, 26.0);
        AnchorPane.setRightAnchor(forward, 130.0);
        AnchorPane.setBottomAnchor(delete, 26.0);
        AnchorPane.setRightAnchor(delete, 180.0);
        reply.setOnMouseClicked(e -> writeMail(ShowMailInfoController.getMail(), 1));
        replyAll.setOnMouseClicked(e -> writeMail(ShowMailInfoController.getMail(), 2));
        forward.setOnMouseClicked(e -> writeMail(ShowMailInfoController.getMail(), 3));
        delete.setOnMouseClicked(e -> {
            try {
                getInstance().deleteMail(ShowMailInfoController.getMail());
            } catch (RemoteException e1) {
                switchToConnectionLost();
            }
            showAllMail();
        });
        centerAnchorPane.getChildren().addAll(reply, replyAll, forward, delete);
    }

    /**
     * Nel caso in cui non ci siano mail viene stampato a video un messaggio.
     *
     * @param text: messaggio di testo da visualizzare.
     * @return una istanza di {@link AnchorPane} con una {@link Label} al suo interno.
     */
    private AnchorPane noMails(String text) {
        AnchorPane pane = new AnchorPane();
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16; -fx-font-family: Verdana;");
        AnchorPane.setTopAnchor(label, 10.0);
        AnchorPane.setRightAnchor(label, 10.0);
        AnchorPane.setBottomAnchor(label, 10.0);
        AnchorPane.setLeftAnchor(label, 10.0);
        pane.getChildren().add(label);
        return pane;
    }

    /**
     * Chiude lo stage attuale andando a prendere il riferimento dall'immagine <code>closeBtnMainView</code>.
     */
    public void closeWindow() {
        Stage stage = (Stage) closeBtnMainView.getScene().getWindow();
        try {
            MainControllerClient.getInstance().stopClient(user.getEmail());
        } catch (RemoteException | MailBoxException e) {
            switchToConnectionLost();
        }
        stage.close();
        System.exit(0);
    }

    /**
     * Apre e chiude il menu laterale (navigation drawer) per rendere pi&ugrave; o meno ampia la vista dell'utente
     * alla schermata attuale.
     */
    public void openCloseMenu() {
        if (isOpenMenu) {
            leftAnchorPane.setPrefWidth(30);
            navigationDrawer.setPrefWidth(30);
            isOpenMenu = false;
        } else {
            leftAnchorPane.setPrefWidth(150);
            navigationDrawer.setPrefWidth(100);
            isOpenMenu = true;
        }
    }

    /**
     * Effetto di hover del mouse per fare in modo di cambiare colore.
     */
    public void changeBlendMode() {
        if (!isHoverClose) {
            closeBtnMainView.setBlendMode(BlendMode.RED);
            isHoverClose = true;
        } else {
            closeBtnMainView.setBlendMode(BlendMode.SRC_OVER);
            isHoverClose = false;
        }
    }

    /**
     * Visualizza a video tutte le mail non ancora lette.
     */
    public void showNewMail() {
        inWriteMail = false;
        inShowMail = false;
        titleMenuSelect.setText("New mails");
        leftArrow.setVisible(false);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        List<Mail> readMails = null;
        try {
            readMails = getInstance().checkPendingMails() == null ? new ArrayList<>() : getInstance().checkPendingMails();
        } catch (RemoteException e) {
            switchToConnectionLost();
        }
        if (readMails == null || readMails.isEmpty()) {
            mailsVbox.getChildren().addAll(noMails("No new mails"));
        } else {
            mailsVbox.getChildren().addAll(showAllMailBoxes(readMails, false, false, false));
        }
    }

    /**
     * Visualizza a video tutte le mail.
     */
    public void showAllMail() {
        inWriteMail = false;
        inShowMail = false;
        titleMenuSelect.setText("All mails");
        leftArrow.setVisible(false);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        List<Mail> readMails = null;
        List<Mail> notReadMails = null;
        try {
            readMails = getInstance().checkMails() == null ? new ArrayList<>() : getInstance().checkMails();
            notReadMails = getInstance().checkPendingMails() == null ? new ArrayList<>() : getInstance().checkPendingMails();
        } catch (RemoteException e) {
            switchToConnectionLost();
        }
        if ((readMails == null || readMails.isEmpty()) && (notReadMails == null || notReadMails.isEmpty())) {
            mailsVbox.getChildren().addAll(noMails("There are no mails"));
        } else {
            mailsVbox.getChildren().addAll(showAllMailBoxes(Objects.requireNonNull(notReadMails), false, false, false));
            mailsVbox.getChildren().addAll(showAllMailBoxes(Objects.requireNonNull(readMails), true, false, false));
        }
    }

    /**
     * Apre la schermata per a scrittura di una nuova mail.
     */
    private void writeMail() {
        inWriteMail = true;
        inShowMail = false;
        titleMenuSelect.setText("Write mail");
        leftArrow.setVisible(true);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        writeMailBtn.setVisible(false);
        addWriteMailButton();
        WriteMailView writeMailView = new WriteMailView(null, 0);
        mailsVbox.getChildren().addAll(writeMailView.display());
    }

    /**
     * Mostra una snakbar sul <b>mainAnchorPane</b> ancorandola in basso a destra.
     */
    void showSnakBarDrafts() {
        AnchorPane snackbar = SnackBar.display("Mail saved correctly", 280, 80);
        AnchorPane.setBottomAnchor(snackbar, 10.0);
        AnchorPane.setRightAnchor(snackbar, 10.0);
        centerAnchorPane.getChildren().add(snackbar);
    }

    /**
     * Aggiunge alla grafica principale il bottone per scrivere una nuova mail e le assegna l'ascoltatore.
     */
    private void addWriteMailButton() {
        String style = "    -fx-background-size: 30 30;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center center;\n" +
                "    -fx-background-color: #ffc107;\n" +
                "    -fx-background-radius: 5em;";
        DropShadow dropShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.valueOf("#000000"), 8.25, 0.0, 1.0, 1.0);
        dropShadow.setHeight(15.0);
        dropShadow.setWidth(20.0);
        Button send = new Button();
        Button save = new Button();
        send.setEffect(dropShadow);
        save.setEffect(dropShadow);
        send.setTooltip(new Tooltip("Send mail"));
        save.setTooltip(new Tooltip("Save mail"));
        send.setPrefWidth(48);
        send.setPrefHeight(48);
        save.setPrefWidth(36);
        save.setPrefHeight(36);
        send.setStyle("-fx-background-image: url(\"view/img/sendW.png\");" + style);
        save.setStyle(style + "-fx-background-image: url(\"view/img/content-save(1).png\");\n" +
                "    -fx-background-size: 20 20;" +
                "-fx-background-color: #009688;");
        AnchorPane.setBottomAnchor(send, 20.0);
        AnchorPane.setRightAnchor(send, 15.0);
        AnchorPane.setBottomAnchor(save, 26.0);
        AnchorPane.setRightAnchor(save, 80.0);
        send.setOnAction(e -> WriteMailController.getWriteMailControllerInstance().sendMail());
        save.setOnAction(e -> WriteMailController.getWriteMailControllerInstance().saveMail());
        centerAnchorPane.getChildren().addAll(send, save);
    }

    /**
     * Aggiunge alla grafica delle bozze il bottone per scrivere la mail o salvarla come bozza.
     */
    private void addDraftsButton(Mail mail) {
        String style = "    -fx-background-size: 30 30;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center center;\n" +
                "    -fx-background-color: #ffc107;\n" +
                "    -fx-background-radius: 5em;";
        DropShadow dropShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.valueOf("#000000"), 8.25, 0.0, 1.0, 1.0);
        dropShadow.setHeight(15.0);
        dropShadow.setWidth(20.0);
        Button write = new Button();
        Button deleteDraft = new Button();
        write.setEffect(dropShadow);
        deleteDraft.setEffect(dropShadow);
        write.setTooltip(new Tooltip("Write mail"));
        deleteDraft.setTooltip(new Tooltip("Delete draft forever"));
        write.setPrefWidth(48);
        write.setPrefHeight(48);
        deleteDraft.setPrefWidth(36);
        deleteDraft.setPrefHeight(36);
        write.setStyle("-fx-background-image: url(\"view/img/pencil.png\");" + style);
        deleteDraft.setStyle(style + "-fx-background-image: url(\"view/img/delete-forever.png\");\n" +
                "    -fx-background-size: 20 20;" +
                "-fx-background-color: #009688;");
        AnchorPane.setBottomAnchor(write, 20.0);
        AnchorPane.setRightAnchor(write, 15.0);
        AnchorPane.setBottomAnchor(deleteDraft, 26.0);
        AnchorPane.setRightAnchor(deleteDraft, 80.0);
        write.setOnAction(e -> writeMail(mail, 4));
        deleteDraft.setOnAction(e -> {
            try {
                getInstance().deleteToDrafts(mail);
            } catch (RemoteException e1) {
                switchToConnectionLost();
            }
            showDrafts();
        });
        centerAnchorPane.getChildren().addAll(write, deleteDraft);
    }

    /**
     * Apre la schermata per la scrittura di una nuova mail.
     */
    private void writeMail(Mail mail, int type) {
        inWriteMail = true;
        inShowMail = false;
        titleMenuSelect.setText("Write mail");
        leftArrow.setVisible(true);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        writeMailBtn.setVisible(false);
        addWriteMailButton();
        WriteMailView writeMailView = new WriteMailView(mail, type);
        if (mail.getPrevious() != null) {
            List<Mail> previousMails = new ArrayList<>();
            Mail prev = mail.getPrevious();
            while (prev != null) {
                previousMails.add(prev);
                prev = prev.getPrevious();
            }
            mailsVbox.getChildren().addAll(Objects.requireNonNull(showAllMailBoxes(previousMails, true, false, false)));
        }
        mailsVbox.getChildren().addAll(writeMailView.display());
    }

    /**
     * Torna alla schermata principale.
     */
    public void goBack() {
        inWriteMail = false;
        inShowMail = false;
        leftArrow.setVisible(false);
        titleMenuSelect.setText("All Mails");
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        List<Mail> readMails = null;
        try {
            readMails = getInstance().checkMails() == null ? new ArrayList<>() : getInstance().checkMails();
        } catch (RemoteException e) {
            switchToConnectionLost();
        }
        List<Mail> notReadMails = null;
        try {
            notReadMails = getInstance().checkPendingMails() == null ? new ArrayList<>() : getInstance().checkPendingMails();
        } catch (RemoteException e) {
            switchToConnectionLost();
        }
        if ((readMails == null || readMails.isEmpty()) && (notReadMails == null || notReadMails.isEmpty())) {
            mailsVbox.getChildren().addAll(noMails("There are no mails"));
        } else {
            mailsVbox.getChildren().addAll(showAllMailBoxes(Objects.requireNonNull(notReadMails), false, false, false));
            mailsVbox.getChildren().addAll(showAllMailBoxes(Objects.requireNonNull(readMails), true, false, false));
        }
    }

    /**
     * Visualizza a video tutte le mail cancellate.
     */
    public void showDeletedMail() {
        inWriteMail = false;
        inShowMail = false;
        titleMenuSelect.setText("Trash");
        leftArrow.setVisible(false);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        List<Mail> deletedMails = getInstance().getDeletedMails() == null ? new ArrayList<>() : getInstance().getDeletedMails();
        if (deletedMails == null || deletedMails.isEmpty()) {
            mailsVbox.getChildren().addAll(noMails("No deleted mails here"));
        } else {
            mailsVbox.getChildren().addAll(Objects.requireNonNull(showAllMailBoxes(deletedMails, true, true, false)));
        }
    }

    /**
     * Visualizza a video tutte le bozze.
     */
    public void showDrafts() {
        inWriteMail = false;
        inShowMail = false;
        titleMenuSelect.setText("Drafts");
        leftArrow.setVisible(false);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        List<Mail> drafts = null;
        try {
            drafts = getInstance().getDrafts() == null ? new ArrayList<>() : getInstance().getDrafts();
        } catch (RemoteException e) {
            switchToConnectionLost();
        }
        if (drafts == null || drafts.isEmpty()) {
            mailsVbox.getChildren().addAll(noMails("Still no message saved"));
        } else {
            mailsVbox.getChildren().addAll(Objects.requireNonNull(showAllMailBoxes(drafts, true, false, true)));
        }
    }

    /**
     * Visualizza a video tutte le mail inviate.
     */
    public void showSentMails() {
        inWriteMail = false;
        inShowMail = false;
        titleMenuSelect.setText("Sents mails");
        leftArrow.setVisible(false);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        List<Mail> sentsMails = null;
        try {
            sentsMails = getInstance().getSentMails();
        } catch (RemoteException e) {
            switchToConnectionLost();
        }
        if (sentsMails == null || sentsMails.isEmpty()) {
            mailsVbox.getChildren().addAll(noMails("Still no sent messsage"));
        } else {
            mailsVbox.getChildren().addAll(Objects.requireNonNull(showAllMailBoxes(sentsMails, true, false, true)));
        }
    }

    /**
     * Visualizza a video tutte le mail lette.
     */
    public void showReadMails() {
        inWriteMail = false;
        inShowMail = false;
        titleMenuSelect.setText("Reads mails");
        leftArrow.setVisible(false);
        centerAnchorPane.getChildren().clear();
        addToCenterAnchorPane();
        List<Mail> readMails = null;
        try {
            readMails = getInstance().getReadMails() == null ? new ArrayList<>() : getInstance().getReadMails();
        } catch (RemoteException e) {
            switchToConnectionLost();
        }
        if (readMails == null || readMails.isEmpty()) {
            mailsVbox.getChildren().addAll(noMails("Still no mails here"));
        } else {
            mailsVbox.getChildren().addAll(Objects.requireNonNull(showAllMailBoxes(readMails, true, false, true)));
        }
    }

    /**
     * Metodo del pattern Observer-Observable utilizzato per notificare all'utente l'arrivo di una nuova mail.
     * La notifica prevede l'aggiunta al pannello centrale <b>mainAnchorPane</b> di un pannello con mandante e
     * oggetto della mail.
     * La chiusura della notifica non &egrave; stata prevista automaticamente ma &egrave; previsto un bottone <b>OK</b>
     * di conferma di avvenuta lettura.
     *
     * @param o:   Osservabile che ha notificato il cambiamento.(L'arrivo di una mail nella <code>write</code> di
     *             {@link model.MailBox}).
     * @param arg: Oggetto di tipo mail.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof Mail) {
            Platform.runLater(() -> {
                showSnakBar("New mail from " + ((Mail) arg).getSender().getValue() + "\n" + "Title: " + ((Mail) arg).getSubject());
                if (!inWriteMail || !inShowMail) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    showAllMail();
                }
            });
        }
    }

    /**
     * Ascoltatore del drag del mouse.
     *
     * @param mouseEvent: evento che genera l'azione.
     */
    public void handleMouseDragged(MouseEvent mouseEvent) {
        getInstanceController().getWindow().setX(mouseEvent.getScreenX() + xOffset);
        getInstanceController().getWindow().setY(mouseEvent.getScreenY() + yOffset);
    }

    /**
     * Ascoltore del click prolungato del mouse.
     *
     * @param mouseEvent: evento che genera l'azione.
     */
    public void handleMousePressed(MouseEvent mouseEvent) {
        xOffset = getInstanceController().getWindow().getX() - mouseEvent.getScreenX();
        yOffset = getInstanceController().getWindow().getY() - mouseEvent.getScreenY();
    }
}
