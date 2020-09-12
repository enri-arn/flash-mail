package persistence;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class LogWriter extends Observable{

    private static final String LOG_FILE_PATH = System.getProperty("user.dir")+"\\Server\\src\\persistence\\log\\flashmail.log";

    private static LogWriter instance;

    /**
     * Metodo derivato dal pattern Singleton che permette di ottenere l'istanza di <b>LogWriter</b>.
     *
     * @return un istanza di {@link LogWriter}.
     */
    public static LogWriter getInstance(){
        if (instance == null){
            instance =  new LogWriter();
        }
        return instance;
    }

    /**
     * Scrive su file di persistence.log tutte le operazioni importanti del server.
     *
     * @param log: stringa da aggiungere al file.
     */
    public void addToLog(String log){
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss ");
        df.setTimeZone(tz);
        String currentTime = df.format(now);
        FileWriter aWriter;
        try {
            aWriter = new FileWriter(LOG_FILE_PATH, true);
            aWriter.write(currentTime + " " + log + "\n");
            aWriter.flush();
            aWriter.close();
            setChanged();
            notifyObservers(currentTime + " " + log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ritorna la lista di log racchiudendoli in una lista.
     *
     * @return una lista di istanze di <b>String</b>.
     */
    public static List<String> getLog(){
        List<String> log = null;
        try {
            FileReader fileReader = new FileReader(new File(LOG_FILE_PATH));
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            log = new ArrayList<>();
            while ((line = reader.readLine()) != null){
                log.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log;
    }

}
