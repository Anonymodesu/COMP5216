package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

public class MainActivity extends BaseActivity {
    //Variables for global navigation
    private String TAG = "";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public Toolbar toolbar;
    private TextView pageName;
    private FirebaseAuth mAuth;
    private String userName, userEmail, userId, userPhoto, userPhone;
    DocumentReference currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //[START_of setup page header and navigation
        //set up page name
        pageName = findViewById(R.id.page_name);
        pageName.setText("Home");

        //get current user information
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        userName = user.getDisplayName();
        userEmail = user.getEmail();
        userId = user.getUid();
        userPhoto = null;
        userPhone = null;

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
                    User newUser = new User(userName, userEmail, userPhone, userPhoto, null, null);
                    currentUser.set(newUser);
                }
            }
         }
        });
        //find all users with the same UID as the currently logged in user
//        final Query query = users.whereEqualTo("uid", userId);


//        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    //if no user exists in the collection, add their details to the database and continue
//                    if (task.getResult().isEmpty()) {
//                        User newUser = new User(userName, userEmail, null, null, null, null);
//                        users.document(userId).set(newUser);
//                    } else {
//                        Toast.makeText(MainActivity.this, "Fucking work you piece of shit", Toast.LENGTH_SHORT).show();
//                    }
//                }else{
//                    Log.d(TAG, "error fetching query");
//                }
//            }
//        });

        //set up global nav drawer
        setSupportActionBar(toolbar);
        DrawerUtil.getDrawer(this, toolbar, userName, userEmail);
        //[END_of setup page header and navigation]
    }

}
