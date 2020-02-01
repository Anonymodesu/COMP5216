package sydney.edu.au.teammeet;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChatMessage {
    private String userId;
    private User user;
    private String message;
    private String message_id;
    private @ServerTimestamp
    Date timestamp;
    private boolean isseen;

    public ChatMessage(String userId, User user, String message, String message_id, Date timestamp, boolean isseen) {
        this.userId = userId;
        this.user = user;
        this.message = message;
        this.message_id = message_id;
        this.timestamp = timestamp;
        this.isseen = isseen;
    }

    public ChatMessage() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
