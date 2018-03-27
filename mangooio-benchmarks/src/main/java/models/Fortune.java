package models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;

import de.svenkubiak.mangooio.morphia.MorphiaModel;

/**
 * 
 * @author svenkubiak
 *
 */
@Entity(value = "fortunes", noClassnameStored = true)
public class Fortune extends MorphiaModel implements Comparable<Fortune> {
    private static final long serialVersionUID = 3493429313579555024L;
    private static final int PRIME = 31;

    @Indexed
    private long fortuneId;

    private String message;

    public Fortune(){
        //Empty constructor for injection
    }

    public Fortune(long fortuneId, String message) {
        this.fortuneId = fortuneId;
        this.message = message;
     }

    public String getMessage() {
        return message;
    }

    public long getFortuneId() {
        return fortuneId;
    }

    @Override
    public int compareTo(Fortune other) {
        return message.compareTo(other.message);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = PRIME * result + (int) (fortuneId ^ (fortuneId >>> 32));
        result = PRIME * result + ((message == null) ? 0 : message.hashCode());
        return PRIME * result + ((objectId == null) ? 0 : objectId.hashCode());
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
        if (fortuneId != other.fortuneId) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        } else {
            //ignore anything else
        }
        if (objectId == null) {
            if (other.objectId != null) {
                return false;
            }
        } else if (!objectId.equals(other.objectId)) {
            return false;
        } else {
            //ignore anything else
        }

        return true;
    }
}