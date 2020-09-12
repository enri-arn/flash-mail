package persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LogWriterTest {

    @Test
    public void addToLog() {
        LogWriter.getInstance().addToLog("test add to log 1");
        LogWriter.getInstance().addToLog("test add to log 2");
        LogWriter.getInstance().addToLog("test add to log 3");
    }

    @Test
    public void getToLog(){
        List<String> log = LogWriter.getLog();
        for (String logs: log) {
            System.out.println(logs);
        }
    }
}