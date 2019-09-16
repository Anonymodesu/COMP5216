package sydney.edu.au.teammeet;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final int GOTO_PERSONAL_TIMETABLE_CODE = 420;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //called by the login button
    //temporarily no user authentication yet
    public void onLogin(View v) {
        Intent intent = new Intent(MainActivity.this, PersonalTimetableActivity.class);

        if (intent != null) {
            // brings up the personal timetable
            startActivityForResult(intent, GOTO_PERSONAL_TIMETABLE_CODE);
        }
    }
}
