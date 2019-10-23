package comp5216.sydney.edu.au.assignment3;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;



/**
 Class to handle the calculator activity of the application
 */
public class CalculatorActivity extends AppCompatActivity {

    // Define variables
    EditText distanceText;
    EditText timeText;
    TextView speedText;
    TextView paceText;


    public final int EDIT_ITEM_REQUEST_CODE = 647;
    public final int ADD_ITEM_REQUEST_CODE = 646;

    /**
     Handles the actions that occur on creating the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use "activity_calculator.xml" as the layout
        setContentView(R.layout.activity_calculator);

        /**
         * Getting all the inputs from the user in the calculator.xml file
         */
        distanceText = (EditText) findViewById(R.id.distanceText);
        timeText = (EditText) findViewById(R.id.timeText);
        speedText = (TextView) findViewById(R.id.speedText);
        paceText = (TextView) findViewById(R.id.paceText);


    }

    /**
     Handles all the calculations in the calculator view
     */
    public void onCalculateClick(View view) {

        String newItem = "";
        Log.i("CalculatorActivity", "calcualte speed and pace ");
        String distanceStr = distanceText.getText().toString();
        String timeStr = timeText.getText().toString();
        double distance = 0;
        double time  =0;
        double speed = 0;
        double pace = 0;
        /**
         * Checking that inputs do not throw an exception
         */
        try{
            if(!distanceStr.isEmpty()){
                distance = Double.parseDouble(distanceStr);
            }
            if(!timeStr.isEmpty()){
                time = Double.parseDouble(timeStr);
            }

            if(time != 0){
                speed = distance / time;
            }
            if(distance != 0){
                pace = time / distance;
            }

        }
        catch (Exception e) {
            Toast.makeText(this, "invalid input", Toast.LENGTH_SHORT).show();
        }

        speedText.setText(String.format("%.2f", speed)+" m/s");
        paceText.setText(String.format("%.2f", pace)+" s/m");

    }

    /**
     *Changes the view to the map view
     */
    public void onToMapClick(View view) {

        Intent intent = new Intent(CalculatorActivity.this, MapsActivity.class);
        startActivityForResult(intent,0);

    }


}
