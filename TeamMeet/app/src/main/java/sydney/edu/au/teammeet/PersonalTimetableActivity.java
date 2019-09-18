package sydney.edu.au.teammeet;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class PersonalTimetableActivity extends BaseActivity {
    //Variables for global navigation
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public Toolbar toolbar;
    private TextView pageName;
    private FirebaseAuth mAuth;
    private String userName, userEmail;
    //Variables for Time table page
    private RecyclerView timetableRecyclerView;
    private TimetableAdapter timetableGridAdapter;
    ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_timetable);
        //[START_of setup page header and navigation
        //set up page name
        pageName=findViewById(R.id.page_name);
        pageName.setText("Time Table");
        //get current user information
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        userName = user.getDisplayName();
        userEmail = user.getEmail();
        //set up global nav drawer
        setSupportActionBar(toolbar);
        DrawerUtil.getDrawer(this, toolbar, userName, userEmail);
        //[END_of setup page header and navigation]


        timetableRecyclerView = findViewById(R.id.timetablegridview);
        // add 1 to the Grid's spanCount to account for hour descriptors
        timetableRecyclerView.setLayoutManager(new GridLayoutManager(this, Timetable.NUM_DAYS + 1));
        timetableGridAdapter = new PersonalTimetableAdapter(this, new Timetable());
        timetableRecyclerView.setAdapter(timetableGridAdapter);
    }

    public void onClear(View view) {
        timetableGridAdapter.clearTimetable();
    }

    private void readItemsFromDatabase()
    {
        //Use asynchronous task to run query on the background and wait for result
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    //read items from database
                    List<TimetableBean> itemsFromDB = LitePal.findAll(TimetableBean.class);
                    items = new ArrayList<String>();
                    if (itemsFromDB != null & itemsFromDB.size() > 0) {
                        for (TimetableBean item : itemsFromDB) {
                                items.add(item.getActivities());
                                Log.i("SQLite read item", "ID: " + item.getTimetableID() + " Name: " + item.getActivities());
                        }
                    }
                    return null;
                }
            }.execute().get();
        }
        catch(Exception ex) {
            Log.e("readItemsFromDatabase", ex.getStackTrace().toString());
        }
    }

}
