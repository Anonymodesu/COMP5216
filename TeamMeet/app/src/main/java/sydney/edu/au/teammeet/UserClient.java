package sydney.edu.au.teammeet;

import android.app.Application;

//singleton to get user object from database for all activities
public class UserClient extends org.litepal.LitePalApplication {
    private User user = null;
    public User getUser(){return user;}
    public void setUser(User user){this.user = user;}
}
