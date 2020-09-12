package model;

import java.io.Serializable;

/**
 *
 * @author Arnaudo Enrico, Burdisso Enrico, Giraudo Paolo
 */
public class UserID implements Serializable{

    private String value;

    public UserID(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof UserID && ((UserID) obj).getValue().equals(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
