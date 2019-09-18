package sydney.edu.au.teammeet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class PersonalTimetableActivity extends BaseActivity {

    private RecyclerView timetableRecyclerView;
    private TimetableAdapter timetableGridAdapter;
    ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_timetable);

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
