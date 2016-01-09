package models;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 *
 * @author svenkubiak
 *
 */
public class Fortune  implements Serializable, Comparable<Fortune> {
    private static final long serialVersionUID = 3493429313579555024L;

    @Id
    protected ObjectId objectId;

    @Indexed(unique=true)
    private long id;

    private String message;

    public Fortune(){
    }

    public Fortune(long id, String message) {
        this.id = id;
        this.message = message;
     }

    public String getMessage() {
        return message;
    }

    public long getId() {
        return id;
    }

    @Override
    public int compareTo(Fortune other) {
        return message.compareTo(other.message);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Fortune other = (Fortune) obj;
        if (id != other.id) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (objectId == null) {
            if (other.objectId != null) {
                return false;
            }
        } else if (!objectId.equals(other.objectId)) {
            return false;
        }

        return true;
    }
}