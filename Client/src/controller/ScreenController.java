package controller;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.HashMap;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class ScreenController {

    /**
     * <b>screenMap</b>: Map contenente tutte le scene del progetto.
     * <b>main:</b>: Scena principale.
     */
    private HashMap<String, Pane> screenMap = new HashMap<>();
    private static Scene main;

    /**
     * Costruttore.
     */
    ScreenController() {
    }

    /**
     * Metodo derivato dal pattern <b>Singleton</b> per ottenere il riferimento all'istanza di {@link ScreenController}.
     *
     * @return: una istanza di {@link ScreenController}.
     */
    protected static Scene getInstance() {
        return main;
    }

    /**
     * Aggiunge un pannello alla Map screenMap.
     *
     * @param name: chiave di riferimento nella Map.
     * @param pane: pannello da aggiungere.
     */
    protected void addScreen(String name, Pane pane) {
        screenMap.put(name, pane);
    }

    /**
     * Definisce la scena principale.
     */
    protected void initialize() {
        main = new Scene(screenMap.get("login"));
    }

    /**
     * Attiva la scena con nome associato definendola come root principale.
     *
     * @param name: nome della scena associata.
     */
    protected void activate(String name) {
        System.out.println("attivo scena " + name + " " + screenMap.get("connectionLost"));
        main.setRoot(screenMap.get(name));
    }
}
