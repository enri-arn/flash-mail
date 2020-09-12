package model;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 *
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailServerClientTest {

    public static void main(String[] args) {
        MailServer server = null;
        try {
            server = new MailServer();
            server.start();
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
