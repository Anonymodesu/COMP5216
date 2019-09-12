package sydney.edu.au.teammeet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;

public class PersonalTimetableActivity extends AppCompatActivity {

    private RecyclerView timetableRecyclerView;
    private TimetableAdapter timetableGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_timetable);

        timetableRecyclerView = findViewById(R.id.timetablegridview);
        timetableRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        timetableGridAdapter = new PersonalTimetableAdapter(this, new Timetable());
        timetableRecyclerView.setAdapter(timetableGridAdapter);
    }

    public void onClear(View view) {
        timetableGridAdapter.clearTimetable();
    }

}