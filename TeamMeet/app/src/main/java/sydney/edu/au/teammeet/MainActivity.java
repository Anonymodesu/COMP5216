package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends BaseActivity {
    //Variables for global navigation
    private String TAG = "";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public Toolbar toolbar;
    private TextView pageName;
    private FirebaseAuth mAuth;
    private String userName, userEmail, userId;
    //Variables for main activity
    private Button TestChangePasswordBtn;


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

        //referencing Users collection on Firestore
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        final CollectionReference users = mFirestore.collection("Users");
        //find all users with the same UID as the currently logged in user
        final Query query = users.whereEqualTo("uid", userId);


        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //if no user exists in the collection, add their details to the database and continue
                    if (task.getResult() == null) {
                        User user = new User(userName, userEmail, null, null, null, null);
                        users.document(userId).set(user);
                    }
                }else{
                    Log.d(TAG, "error fetching query");
                }
            }
        });


        //set up global nav drawer
        setSupportActionBar(toolbar);
        DrawerUtil.getDrawer(this, toolbar, userName, userEmail);
        //[END_of setup page header and navigation]
    }


    private void SendUserToChangePasswordActivity(){
        Intent mainIntent = new Intent(MainActivity.this, ChangePasswordActivity.class);
        //can be deleted depend on main page
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    //redirect user to see their groups


}
