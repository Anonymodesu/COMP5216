package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    //Variables for global navigation
    private String TAG = "";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public MaterialToolbar toolbar;
    private TextView pageName;
    private FirebaseAuth mAuth;
    private String userName, userEmail, userId, userPhoto, userPhone;
    private User newUser;
    DocumentReference currentUser;

//    variables for notification
public static final String NOTIFICATION_REPLY = "NotificationReply";
    public static final String CHANNNEL_ID = "addMemberInvitationChannel";
    public static final String CHANNEL_NAME = "addMemberInvitationChannel";
    public static final String CHANNEL_DESC = "This is a channel for add new members";

    public static final String KEY_INTENT_ACCEPT = "keyintentaccept";
    public static final String KEY_INTENT_DECLINE = "keyintentdecline";

    public static final int REQUEST_CODE_ACCEPT = 100;
    public static final int REQUEST_CODE_DECLINE = 101;
    public static final int NOTIFICATION_ID = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpGlobalNav(MainActivity.this, "Home");

        //get current user information
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;

        userName = user.getDisplayName();
        userEmail = user.getEmail();
        userId = user.getUid();
        userPhoto = null;
        userPhone = null;

        //display notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESC);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
            displayNotification();
        }

//retrieve user information for google and facebook authentification
        for(UserInfo userInfo: user.getProviderData()) {
            if (userInfo.getProviderId().equals("facebook.com")) {
                userPhoto = "https://graph.facebook.com/" + userInfo.getUid() + "/picture?type=large";
                userPhone = userInfo.getPhoneNumber();
            }
            if (userInfo.getProviderId().equals("google.com")) {
                userPhoto = userInfo.getPhotoUrl().toString();
                userPhone = userInfo.getPhoneNumber();
            }
        }

        //referencing Users collection on Firestore
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        currentUser = mFirestore.collection("Users").document(userId);
        currentUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
         @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
         {
             if(task.isSuccessful()){
                 DocumentSnapshot document = task.getResult();
                if(!document.exists())
                {
                    newUser = new User(userName, userEmail.toLowerCase(), userPhone, userPhoto, null, null, null);
                    currentUser.set(newUser);
                }else{
                    newUser = document.toObject(User.class);
                }
                //set up User value for other pages
                 ((UserClient)(getApplicationContext())).setUser(newUser);
                //update user information into nav menu
                 setUpGlobalNav(MainActivity.this, "Home");
            }
         }
        });
    }
    public void displayNotification() {

        //Pending intent for a notification button named More
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                MainActivity.this,
                REQUEST_CODE_ACCEPT,
                new Intent(MainActivity.this, AddNewMemberActivity.NotificationReceiver.class)
                        .putExtra(KEY_INTENT_ACCEPT, REQUEST_CODE_ACCEPT),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //Pending intent for a notification button help
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                MainActivity.this,
                REQUEST_CODE_DECLINE,
                new Intent(MainActivity.this, AddNewMemberActivity.NotificationReceiver.class)
                        .putExtra(KEY_INTENT_DECLINE, REQUEST_CODE_DECLINE),
                PendingIntent.FLAG_UPDATE_CURRENT
        );




        //For the remote input we need this action object
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_delete,
                        "Reply Now...", acceptPendingIntent)
                        .build();

        //Creating the notifiction builder object
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Invitation")
                .setContentText("You got an group invitaion, pls reply")
                .setAutoCancel(true)
                .setContentIntent(acceptPendingIntent)
                .addAction(action)
                .addAction(android.R.drawable.ic_menu_compass, "More", acceptPendingIntent)
                .addAction(android.R.drawable.ic_menu_directions, "Help", declinePendingIntent);


        //finally displaying the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
