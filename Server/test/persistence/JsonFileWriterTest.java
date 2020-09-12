package persistence;

import java.util.*;

import model.Mail;
import model.Message;
import model.User;
import model.UserID;
import org.junit.Test;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class JsonFileWriterTest {

    @Test
    public void storeUsersTest() {
        UserID usertest1 = new UserID("test12@test12.test1");
        UserID usertest2 = new UserID("test22@test22.test2");
        List<User> users = new ArrayList<>();
        users.add(new User(usertest1, "password1", "testname1", "testsurname1"));
        users.add(new User(usertest2, "password2", "testname2", "testsurname2"));
        JsonFileWriter.storeUsers(users);
    }

    @Test
    public void readUsersTest() {
        List<User> users = JsonFileWriter.readUsers();
        Objects.requireNonNull(users).forEach(System.out::println);
    }
}
