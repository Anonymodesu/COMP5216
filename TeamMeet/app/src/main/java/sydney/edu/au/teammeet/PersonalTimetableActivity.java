package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

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
    private FirebaseAuth mAuth;
    private String userName, userEmail;

    //Variables for Time table page
    private LockableRecyclerView timetableRecyclerView;
    private PersonalTimetableAdapter timetableGridAdapter;
    private LockableScrollView timetableHorizontalScroll;
    ArrayList<String> items;
    private Timetable timetable;
    private boolean standardZoom; //whether the timetable is zoomed at standard level
    private Mode currentMode;

    /*
    private RecyclerView.OnItemTouchListener recyclerViewTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            boolean fired = currentMode != Mode.STANDARD;

            Log.d("intercept", "" + fired);


            if(fired) {
                return true;
            }

            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            Log.d("touch", "" + e.getActionMasked());
            rv.findChildViewUnder(e.getX(), e.getY()).dispatchTouchEvent(e);
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };
    */
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

        timetable = new Timetable();
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
    }

    //swaps between two zoom levels
    public void onZoom(View view) {
        int newSize = standardZoom ? TimetableAdapter.LARGE_CELL_SIZE : TimetableAdapter.SMALL_CELL_SIZE;
        standardZoom = !standardZoom;

        timetableGridAdapter = new PersonalTimetableAdapter(this, timetable, newSize);
        timetableGridAdapter.switchMode(currentMode);
        timetableRecyclerView.setAdapter(timetableGridAdapter);

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
