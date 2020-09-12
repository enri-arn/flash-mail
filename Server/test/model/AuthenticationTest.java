package model;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class AuthenticationTest {

    @Test
    public void authenticateTest(){
        String email = "test12@test12.test1";
        String password = "password1";
        User user = new User(new UserID(email), password, "testname1", "testsurname1");
        MailServer server = null;
        try {
            server = new MailServer();
            server.start();
            boolean test = server.authenticate(user);
            System.out.print(test);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
