package model;

import org.junit.jupiter.api.Test;
import persistence.JsonFileWriter;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 *
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailServerTest{

    @Test
    public void testStartServer() {
        MailServer server = null;
        try {
            server = new MailServer();
            assert server != null;
            server.start();
            server.stop();
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInitWriter(){
        MailServer server = null;
        try {
            server = new MailServer();
            server.start();
            server.stop();
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
    }

}
