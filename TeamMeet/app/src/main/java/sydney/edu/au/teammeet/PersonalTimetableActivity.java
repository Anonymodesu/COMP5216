package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

public class PersonalTimetableActivity extends BaseActivity {
    public enum Mode {
        FREE, LOW, MEDIUM, HIGH, STANDARD
    }

    //Variables for global navigation
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    public Toolbar toolbar;
    private TextView pageName;
    private String userName, userEmail;

    //Variables for Time table page
    private LockableRecyclerView timetableRecyclerView;
    private PersonalTimetableAdapter timetableGridAdapter;
    private LockableScrollView timetableHorizontalScroll;
    ArrayList<String> items;
    private Timetable timetable;
    private boolean standardZoom; //whether the timetable is zoomed at standard level
    private Mode currentMode;


    FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

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

        timetable = loadSavedData();
        if(timetable == null) {
            timetable = new Timetable();
        }
        standardZoom = true;
        currentMode = Mode.STANDARD;
        setupTimetable();
        setupModeSelector();
    }

    private void setupTimetable() {
        timetableHorizontalScroll = findViewById(R.id.timetable_scroll_view);
        timetableHorizontalScroll.setNestedScrollingEnabled(false);

        timetableRecyclerView = findViewById(R.id.timetablegridview);
        timetableRecyclerView.setNestedScrollingEnabled(false);
        // add 1 to the Grid's spanCount to account for hour descriptors
        /*
        FixedGridLayoutManager layoutManager = new FixedGridLayoutManager();
        layoutManager.setTotalColumnCount(Timetable.NUM_DAYS + 1);
        timetableRecyclerView.setLayoutManager(layoutManager);
        */
        timetableRecyclerView.setLayoutManager(new GridLayoutManager(this, Timetable.NUM_DAYS + 1));
        timetableGridAdapter = new PersonalTimetableAdapter(this, timetable, TimetableAdapter.SMALL_CELL_SIZE);
        timetableRecyclerView.setAdapter(timetableGridAdapter);
        //timetableRecyclerView.addOnItemTouchListener(recyclerViewTouchListener);
    }

    //switch between mass assignment of weightings or standard mode
    private void setupModeSelector() {
        RadioGroup modeSelector = findViewById(R.id.mode_selector);
        modeSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {

                switch(id) {
                    case R.id.free_priority_mode_radio:
                        currentMode = Mode.FREE;
                        break;
                    case R.id.low_priority_mode_radio:
                        currentMode = Mode.LOW;
                        break;
                    case R.id.medium_priority_mode_radio:
                        currentMode = Mode.MEDIUM;
                        break;
                    case R.id.high_priority_mode_radio:
                        currentMode = Mode.HIGH;
                        break;
                    case R.id.standard_mode_radio:
                        currentMode = Mode.STANDARD;
                        break;
                    default:
                        throw new IllegalArgumentException("unsupported radio button type in setupModeSelector()");
                }

                timetableGridAdapter.switchMode(currentMode);
                timetableRecyclerView.setScrollable(currentMode == Mode.STANDARD);
                timetableHorizontalScroll.setScrollable(currentMode == Mode.STANDARD);
            }
        });
    }

    public void onClear(View view) {
        timetableGridAdapter.clearTimetable();
        clearByPreference ();
    }

    //swaps between two zoom levels
    public void onZoom(View view) {
        int newSize = standardZoom ? TimetableAdapter.LARGE_CELL_SIZE : TimetableAdapter.SMALL_CELL_SIZE;
        standardZoom = !standardZoom;

        timetableGridAdapter = new PersonalTimetableAdapter(this, timetable, newSize);
        timetableGridAdapter.switchMode(currentMode);
        timetableRecyclerView.setAdapter(timetableGridAdapter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveByPreference();
    }

    /* save timetable's data to SharedPreferences in json format*/
    private void saveByPreference(){


        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(timetable);

        editor.putString("personal_timetable", json);
        editor.commit();
        //Toast.makeText(this, "saved!", LENGTH_SHORT).show();

        //upload data to firebase
        mFirestore = FirebaseFirestore.getInstance();
        DocumentReference newPTId = mFirestore.collection("PersonalTimetables").document();
        newPTId.set(mPref);
    }

    /** get json data from SharedPreferences and then restore the timetable */
    private Timetable loadSavedData() {
        //removeAll();

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        String json = mPref.getString("personal_timetable", "");

        Gson gson = new Gson();
        Timetable savedTimetable = gson.fromJson(json, Timetable.class);

        return savedTimetable;
        //if (savedData == null && savedData.equals("")) return;
        //load(savedData);
    }


    /** clear all data */
    private void clearByPreference (){

        timetable = new Timetable();
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(timetable);

        editor.putString("personal_timetable", json);
        editor.commit();
        //Toast.makeText(this, "saved!", LENGTH_SHORT).show();
    }
    

}
