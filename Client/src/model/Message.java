package model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class Message implements Serializable {

    private String message;
    private Date date;

    public Message(String message, Date date) {
        this.message = message;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Messaggio : " + message +
                ", data :" + date;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Message && ((Message) obj).getMessage().equals(this.message)
                && ((Message) obj).getDate().equals(this.date);
    }
}
