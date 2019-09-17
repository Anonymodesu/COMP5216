package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public Toolbar toolbar;
    private TextView pageName;
    private Button TestChangePasswordBtn;
    private FirebaseAuth mAuth;

    private String userName, userEmail;

//    @BindView(R.id.toolbar)
//    public Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set up pagename
        pageName=findViewById(R.id.page_name);
        pageName.setText("Home");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        userName = user.getDisplayName();
        userEmail = user.getEmail();
        //set up global nav drawer
        setSupportActionBar(toolbar);
        DrawerUtil.getDrawer(this, toolbar, userName, userEmail);


        TestChangePasswordBtn = (Button) findViewById(R.id.TestChangePassWordBtn);

        TestChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToChangePasswordActivity();
            }
        });

    }

    private void SendUserToChangePasswordActivity(){
        Intent mainIntent = new Intent(MainActivity.this, ChangePasswordActivity.class);
        //can be deleted depend on main page
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}