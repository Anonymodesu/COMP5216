package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.ArrayList;

public class  PersonalTimetableActivity extends BaseActivity {
    public enum Mode {
        FREE, LOW, MEDIUM, HIGH, STANDARD
    }

    private static final String SHARED_PREF_ID = "personal_timetable_activity";
    private static final String SHARED_PREF_TIMETABLE = "personal_timetable_data";
    private static final String SHARED_PREF_USER = "personal_timetable_user";

    //Variables for global navigation
    private String userId;

    //Variables for Time table page
    private LockableRecyclerView timetableRecyclerView;
    private PersonalTimetableAdapter timetableGridAdapter;
    private LockableScrollView timetableHorizontalScroll;
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
        setUpGlobalNav(PersonalTimetableActivity.this, "Timetable");

        //[END_of setup page header and navigation]
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser userAuth = mAuth.getCurrentUser();
        userId = userAuth.getUid();


        standardZoom = true;
        currentMode = Mode.STANDARD;

        loadSavedData();
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

    //allows switch to mass assignment of weightings and activities
    private void setupMassFill() {
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

        //update filling activity when focus leaves the EditText
        final EditText activityFill = findViewById(R.id.fillTextView);
        activityFill.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                timetableGridAdapter.setFillActivity(activityFill.getText().toString());
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

    @Override
    protected void onPause() {
        super.onPause();
        saveByPreference();
    }

    /* save timetable's data to SharedPreferences in json format*/
    private void saveByPreference(){

        //update timetable locally
        SharedPreferences mPref = getSharedPreferences(SHARED_PREF_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(timetable);

        editor.putString(SHARED_PREF_TIMETABLE, json);
        editor.putString(SHARED_PREF_USER, userId);

        editor.apply();

        //update timetable to database
        DocumentReference currentUser = mFirestore.collection("Users").document(userId);
        currentUser.update("timetable", json);
    }

    /** get json data from SharedPreferences or Firebase and then restore the timetable */
    private void loadSavedData() {

        final Gson gson = new Gson();

        SharedPreferences mPref = getSharedPreferences(SHARED_PREF_ID, Context.MODE_PRIVATE);
        String jsonTimetable = mPref.getString(SHARED_PREF_TIMETABLE, null);
        String storedUser = mPref.getString(SHARED_PREF_USER, "");

        //check if the locally stored user timetable is same as current user
        if(userId.equals(storedUser)) {
            this.timetable = gson.fromJson(jsonTimetable, Timetable.class);


            setupTimetable();
            setupMassFill();

        } else {

            //check if firebase-stored timetable exists
            DocumentReference userReference = mFirestore.collection("Users").document(userId);
            userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()) {
                        String timetableJson = task.getResult().getString("timetable");
                        timetable = gson.fromJson(timetableJson, Timetable.class);

                    } else {
                        Log.e("timetable failed", task.getException().getMessage());

                        timetable = new Timetable();
                    }

                    setupTimetable();
                    setupMassFill();
                }
            });

        }

    }


    

}
