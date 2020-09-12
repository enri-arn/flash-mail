package model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class Mail implements Serializable {

    private int id;
    private UserID sender;
    private UserID receiver;
    private List<UserID> carbonCopy;
    private String subject;
    private Message message;
    private Mail previous;

    public Mail(int id, UserID sender, UserID receiver, List<UserID> carbonCopy, String subject, Message message, Mail previous) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.carbonCopy = carbonCopy;
        this.subject = subject;
        this.message = message;
        this.previous = previous;
    }

    public Mail(int id, UserID sender, UserID receiver, String subject, Message message, Mail previous) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.message = message;
        this.previous = previous;
    }

    public int getId() {
        return id;
    }

    public UserID getSender() {
        return sender;
    }

    public void setSender(UserID sender) {
        this.sender = sender;
    }

    public UserID getReceiver() {
        return receiver;
    }

    public boolean hasReceiver() {
        return receiver != null;
    }

    public List<UserID> getCarbonCopy() {
        return carbonCopy;
    }

    public boolean hasCarbonCopy() {
        return carbonCopy != null && !carbonCopy.isEmpty();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReceiver(UserID receiver) {
        this.receiver = receiver;
    }

    public void setCarbonCopy(List<UserID> carbonCopy) {
        this.carbonCopy = carbonCopy;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Mail getPrevious() {
        return previous;
    }

    public void setPrevious(Mail previous) {
        this.previous = previous;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "id=" + id +
                ", sender=" + sender +
                ", receiver=" + receiver +
                ", carbonCopy=" + carbonCopy +
                ", subject='" + subject + '\'' +
                ", message=" + message +
                ", previous=" + previous +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Mail){
            return ((Mail) obj).getId() == this.id;
        }
        return false;
    }
}
