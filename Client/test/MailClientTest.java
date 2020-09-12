import model.*;
import model.exception.MailBoxException;
import model.exception.MalformedMailException;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class MailClientTest {

    private static final int NUM_THREAD = 100;
    private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Egestas purus viverra accumsan in nisl nisi scelerisque. Congue eu " +
            "consequat ac felis donec. Erat pellentesque adipiscing commodo elit. Est velit egestas dui id ornare arcu odio. " +
            "Risus pretium quam vulputate dignissim. Sed cras ornare arcu dui vivamus arcu felis bibendum. Tortor at risus " +
            "viverra adipiscing at in tellus integer. Sed turpis tincidunt id aliquet. Scelerisque varius morbi enim nunc " +
            "faucibus a. Magna ac placerat vestibulum lectus. Imperdiet sed euismod nisi porta lorem mollis. Turpis egestas" +
            " pretium aenean pharetra magna ac placerat. Penatibus et magnis dis parturient montes nascetur ridiculus mus." +
            " Tellus integer feugiat scelerisque varius. Cum sociis natoque penatibus et magnis dis. Tristique risus nec " +
            "feugiat in fermentum.\n" +
            "\n" +
            "Magnis dis parturient montes nascetur ridiculus mus mauris vitae ultricies. Lobortis scelerisque fermentum " +
            "dui faucibus in ornare. Turpis massa sed elementum tempus egestas sed sed risus. Vitae auctor eu augue ut " +
            "lectus arcu bibendum at. Maecenas pharetra convallis posuere morbi. Mollis aliquam ut porttitor leo a diam " +
            "sollicitudin tempor. Tincidunt dui ut ornare lectus sit amet est. Vel facilisis volutpat est velit egestas. " +
            "Ullamcorper velit sed ullamcorper morbi tincidunt. Tincidunt lobortis feugiat vivamus at augue eget arcu. " +
            "Egestas fringilla phasellus faucibus scelerisque eleifend. Ullamcorper eget nulla facilisi etiam dignissim " +
            "diam. Mattis vulputate enim nulla aliquet porttitor. Blandit volutpat maecenas volutpat blandit aliquam etiam" +
            " erat velit. Fringilla ut morbi tincidunt augue interdum. Eros in cursus turpis massa tincidunt dui ut ornare. " +
            "Gravida in fermentum et sollicitudin ac orci. Nisi est sit amet facilisis magna. Tortor vitae purus faucibus " +
            "ornare suspendisse sed. At risus viverra adipiscing at in tellus integer feugiat scelerisque.";

    @Test
    void testRegisterClient() {
        MailClient client = null;
        String email = "test1@flashmail.com";
        String password = "Password123!";
        User user = new User(new UserID(email), password, "testname1", "testsurname1");
        UserID userID = new UserID(email);
        try {
            client = new MailClient();
            client.initClient();
            boolean test = client.authenticate(user);
            System.out.println("== TEST REGISTER-AUTHENTICATE ONE CLIENT : " + test + " ===");
            client.stopClient(userID);
        } catch (RemoteException | NotBoundException | MalformedURLException | MailBoxException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSendMail() {
        MailClient client = null;
        String user = "a@b.com";
        UserID userID = new UserID(user);
        try {
            client = new MailClient();
            client.initClient();
            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
            int id = client.getID();
            Mail mail = new Mail(id, new UserID(user), new UserID(user), "object", new Message(LOREM_IPSUM, new Date()), null);
            client.send(mail);
            System.out.println("== TEST SEND ONE MAIL CLIENT a@b.com to a@b.com ===");
            client.stopClient(userID);
        } catch (RemoteException | NotBoundException | MalformedURLException | MailBoxException | MalformedMailException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSendMailAtoAwitchCC() throws MailBoxException {
        MailClient client = null;
        String user = "a@b.com";
        UserID userID = new UserID(user);
        List<UserID> cc = new ArrayList<>();
        cc.add(new UserID("testcc1"));
        cc.add(new UserID("testcc2"));
        cc.add(new UserID("testcc3"));
        cc.add(new UserID("testcc4"));
        cc.add(new UserID("testcc5"));
        cc.add(new UserID("testcc6"));
        try {
            client = new MailClient();
            client.initClient();
            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
            int id = client.getID();
            Mail mail = new Mail(id, new UserID(user), new UserID(user), "object", new Message(LOREM_IPSUM, new Date()), null);
            client.send(mail);
            System.out.println("== TEST SEND ONE MAIL CLIENT a@b.com to a@b.com && testcc1, testcc2, testcc3, testcc4, testcc5, testcc6 ===");
            client.stopClient(userID);
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            System.err.println("Cuncurrency test failed");
        } catch (MalformedMailException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testConcurrency() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
            for (int i = 0; i < NUM_THREAD; i++) {
                executor.submit(new NamedRunnable("client" + i) {
                    @Override
                    public void run() {
                        try {
                            UserID userID = new UserID(this.getName());
                            MailClient client = new MailClient();
                            client.initClient();
                            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
                            client.stopClient(userID);
                        } catch (RemoteException ex) {
                            System.err.println("ExecutorService failed");
                        } catch (NotBoundException | MalformedURLException e) {
                            System.err.println("Cuncurrency test failed");
                        } catch (MailBoxException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.awaitTermination(20, TimeUnit.SECONDS);
            System.out.println("== TEST REGISTER_AUTHENTICATE " + NUM_THREAD + "  CLIENTS  ===");
        } catch (InterruptedException ex) {
            System.err.println("testSendMailAtoAwitchCC failed");
        }
    }

    @Test
    void testCuncurrencyMailAtoA() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
            for (int i = 0; i < NUM_THREAD; i++) {
                executor.submit(new NamedRunnable("client" + i) {
                    @Override
                    public void run() {
                        try {
                            UserID userID = new UserID(this.getName());
                            MailClient client = new MailClient();
                            client.initClient();
                            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
                            int id = client.getID();
                            Mail mail = new Mail(id, new UserID(this.getName()), new UserID(this.getName()), this.getName(), new Message(LOREM_IPSUM, new Date()), null);
                            client.send(mail);
                            client.stopClient(userID);
                        } catch (RemoteException | NotBoundException | MalformedURLException ex) {
                            System.err.println("testCuncurrencyMailAtoA failed");
                        } catch (MailBoxException | MalformedMailException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.awaitTermination(20, TimeUnit.SECONDS);
            System.out.println("== TEST SEND MAIL client(i) -> client(i) WITH " + NUM_THREAD + "  CLIENTS  ===");
        } catch (InterruptedException ex) {
            System.err.println("Cucnurrency test failed");
        }
    }

    @Test
    void testCuncurrencyMailAtoAwithCC() {
        List<UserID> cc = new ArrayList<>();
        cc.add(new UserID("testcc1"));
        cc.add(new UserID("testcc2"));
        cc.add(new UserID("testcc3"));
        cc.add(new UserID("testcc4"));
        cc.add(new UserID("testcc5"));
        cc.add(new UserID("testcc6"));
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
            for (int i = 0; i < NUM_THREAD; i++) {
                executor.submit(new NamedRunnable("client" + i) {
                    @Override
                    public void run() {
                        try {
                            UserID userID = new UserID(this.getName());
                            MailClient client = new MailClient();
                            client.initClient();
                            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
                            int id = client.getID();
                            Mail mail = new Mail(id, new UserID(this.getName()), new UserID(this.getName()), cc, this.getName(), new Message(LOREM_IPSUM, new Date()), null);
                            client.send(mail);
                            client.stopClient(userID);
                        } catch (RemoteException ex) {
                            System.err.println("ExecutorService failed");
                        } catch (NotBoundException | MalformedURLException | MailBoxException | MalformedMailException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.awaitTermination(20, TimeUnit.SECONDS);
            System.out.println("== TEST SEND MAIL client(i) -> client(i) && testcc1, testcc2, testcc3, testcc4, testcc5, testcc6 WITH " + NUM_THREAD + "  CLIENTS  ===");
        } catch (InterruptedException ex) {
            System.err.println("Cucnurrency test failed");
        }
    }

    @Test
    void testCuncurrencyMailAtoB() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
            for (int i = 0; i < NUM_THREAD; i++) {
                executor.submit(new NamedRunnable("client" + i) {
                    @Override
                    public void run() {
                        try {
                            UserID userID = new UserID(this.getName());
                            MailClient client = new MailClient();
                            client.initClient();
                            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
                            int id = client.getID();
                            Mail mail = new Mail(id, new UserID(this.getName()), new UserID("a@b.com"), this.getName(), new Message(LOREM_IPSUM, new Date()), null);
                            client.send(mail);
                            client.stopClient(userID);
                        } catch (RemoteException ex) {
                            System.err.println("ExecutorService failed");
                        } catch (NotBoundException | MalformedURLException | MailBoxException | MalformedMailException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.awaitTermination(20, TimeUnit.SECONDS);
            System.out.println("== TEST SEND MAIL client(i) -> a@b.com WITH " + NUM_THREAD + "  CLIENTS  ===");
        } catch (InterruptedException ex) {
            System.err.println("Cucnurrency test failed");
        }
    }

    @Test
    void testCuncurrencyMailAtoRandom() {
        Random rand = new Random();
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
            for (int i = 0; i < NUM_THREAD; i++) {
                executor.submit(new NamedRunnable("client" + i) {
                    @Override
                    public void run() {
                        try {
                            UserID userID = new UserID(this.getName());
                            MailClient client = new MailClient();
                            client.initClient();
                            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
                            int id = client.getID();
                            Mail mail = new Mail(id, new UserID(this.getName()), new UserID("client" + rand.nextInt(50)), this.getName(), new Message(LOREM_IPSUM, new Date()), null);
                            // è possibile che una mail di un client da 0 a 30 finisca in pending nel caso in cui lui sia già offline.
                            // se client0 invia mail e poi va offline, tutte le mail inviate dopo  saranno pendingMails.
                            client.send(mail);
                            client.stopClient(userID);
                        } catch (RemoteException | NotBoundException | MalformedURLException ex) {
                            System.err.println("ExecutorService failed");
                        } catch (MailBoxException | MalformedMailException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.awaitTermination(20, TimeUnit.SECONDS);
            System.out.println("== TEST SEND MAIL client(i) -> random client(0 - 50) WITH " + NUM_THREAD + "  CLIENTS  ===");
        } catch (InterruptedException ex) {
            System.err.println("Cucnurrency test failed");
        }
    }

    @Test
    void testCuncurrencyDeletePending() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
            for (int i = 0; i < NUM_THREAD; i++) {
                executor.submit(new NamedRunnable("client" + i) {
                    @Override
                    public void run() {
                        try {
                            UserID userID = new UserID(this.getName());
                            MailClient client = new MailClient();
                            client.initClient();
                            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
                            List<Mail> pending = client.checkPendingMails();
                            pending.forEach(System.out::println);
                            System.out.println(this.getName() + " pending size " + pending.size() + "\n");
                            for (Mail entry: pending) {
                                client.deletePending(entry);
                            }
                            client.stopClient(userID);
                        } catch (RemoteException | NotBoundException | MalformedURLException ex) {
                            System.err.println("ExecutorService failed");
                        } catch (MailBoxException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.awaitTermination(20, TimeUnit.SECONDS);
            System.out.println("== TEST SEND MAIL client(i) -> random client(0 - 50) WITH " + NUM_THREAD + "  CLIENTS  ===");
        } catch (InterruptedException ex) {
            System.err.println("Cucnurrency test failed");
        }
    }

    @Test
    void checkPendingMailTest() {
        MailClient client;
        UserID userID = new UserID("client5");
        try {
            client = new MailClient();
            client.initClient();
            boolean a = client.authenticate(new User(userID, "Scoiattolo123!", "testname2", "testsurname2"));
            System.out.println(a);
            List<Mail> mails = client.checkPendingMails();
            mails.forEach(System.out::println);
            client.deletePending(mails.get(0));
            List<Mail> mails1 = client.checkPendingMails();
            mails1.forEach(System.out::println);
            System.out.println("== TEST CHECK PENDING MAILS CLIENT 'a@b.com' mails.size() = " + mails.size() + "  ===");
            client.stopClient(userID);
        } catch (RemoteException | MalformedURLException | NotBoundException | MailBoxException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getMails() {
        MailClient client = null;
        String user = "a@b.com";
        UserID userID = new UserID(user);
        try {
            client = new MailClient();
            client.initClient();
            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
            List<Mail> mails = client.checkMails();
            System.out.println(mails);
            mails.forEach(System.out::println);
            System.out.println("== TEST GET MAILS CLIENT 'a@b.com' mails.size() = " + mails.size() + "  ===");
            client.stopClient(userID);
        } catch (RemoteException | NotBoundException | MalformedURLException | MailBoxException e) {
            e.printStackTrace();
        }
    }

    @Test
    void removeMailTest() {
        MailClient client = null;
        String user = "a@b.com";
        UserID userID = new UserID(user);
        try {
            client = new MailClient();
            client.initClient();
            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
            int id = client.getID();
            Mail mail = new Mail(id, new UserID(user), new UserID("a@b.com"), "object", new Message(LOREM_IPSUM, new Date()), null);
            client.send(mail);
            boolean test = client.deleteMail(mail);
            System.out.println("== TEST REMOVE MAIL CLIENT 'a@b.com' mails.size() = " + test + "  ===");
            client.stopClient(userID);
        } catch (RemoteException | MalformedURLException | NotBoundException | MailBoxException | MalformedMailException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCuncurrencyRemoveMail() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
            for (int i = 0; i < NUM_THREAD; i++) {
                executor.submit(new NamedRunnable("client" + i) {
                    @Override
                    public void run() {
                        try {
                            UserID userID = new UserID(this.getName());
                            MailClient client = new MailClient();
                            client.initClient();
                            client.authenticate(new User(userID, "Scoiattolo123!", "test", "test"));
                            int id = client.getID();
                            Mail mail = new Mail(id, new UserID(this.getName()), new UserID(this.getName()), this.getName(), new Message(LOREM_IPSUM, new Date()), null);
                            client.send(mail);
                            boolean test = client.deleteMail(mail);
                            System.out.println(test);
                            client.stopClient(userID);
                        } catch (RemoteException | NotBoundException | MalformedURLException ex) {
                            System.err.println("testCuncurrencyMailAtoA failed");
                        } catch (MailBoxException | MalformedMailException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.awaitTermination(20, TimeUnit.SECONDS);
            System.out.println("== TEST SEND MAIL client(i) -> client(i) WITH " + NUM_THREAD + "  CLIENTS  ===");
        } catch (InterruptedException ex) {
            System.err.println("Cucnurrency test failed");
        }
    }

    private static abstract class NamedRunnable implements Runnable {

        private String name;

        public NamedRunnable(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }
}
