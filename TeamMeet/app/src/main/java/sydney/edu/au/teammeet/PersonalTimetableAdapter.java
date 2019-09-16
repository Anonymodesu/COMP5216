package sydney.edu.au.teammeet;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class PersonalTimetableAdapter extends TimetableAdapter {

    ArrayList<String> items;
    private TimetableBean mTimetableBean;

    private static void setColorFilter(@NonNull Drawable drawable, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public PersonalTimetableAdapter(Context context, final Timetable timetable) {
        super(context, timetable);

        //simple touches alternates colour
        setClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mTimetable.getWeighting(position) > 0) {
                    mTimetable.setWeighting(position, 0);
                    //saveItemsToDatabase();
                } else {
                    mTimetable.setWeighting(position, 2);
                    //saveItemsToDatabase();
                }

                notifyItemChanged(position);
            }
        });

        //long touches allow for more customised edits
        setLongClickListener(new TimetableAdapter.ItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, final int position) {
                String[] weights = new String[]{"Free", "Low", "Medium", "High"};

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                // Set up the activity text input
                final EditText inputActivity = new EditText(mContext);

                if (inputActivity.getText().toString() == null) {
                }
                    inputActivity.setText(mTimetable.getActivity(position));
                    // Specify the type of input expected; this, for example, sets the input as plaintext
                    inputActivity.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(inputActivity)
                            .setTitle("Edit Timeslot")
                            .setMessage("Input timeslot weight and associated activity.")

                            //weight the timeslot
                            //this doesnt work yet...
                            .setItems(weights, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected) {
                                    //chosenWeighting = indexSelected;
                                }
                            })
                            .setPositiveButton("Confirm", new
                                    DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //save the weighting and activity back to timetable
                                            mTimetable.setActivity(position, inputActivity.getText().toString());
                                            notifyItemChanged(position);

                                            //save data to local database
                                            mTimetableBean = new TimetableBean();
                                            mTimetableBean.setTimetableID(position);
                                            mTimetableBean.setActivities(inputActivity.getText().toString());
                                            mTimetableBean.saveThrows();

                                            Log.i("Input:" + position, inputActivity.getText().toString());
                                        }
                                    })
                            .setNegativeButton("Cancel", new
                                    DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // User cancelled the dialog
                                            // Nothing happens
                                        }
                                    });

                    builder.create().show();
                    return true;
            }
        });
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int weighting = mTimetable.getWeighting(position);
        holder.myTextView.setText("" + weighting);

        //cell changes colour depending on weighting
        int colour = Color.WHITE;
        switch(weighting) {
            case 0:
                colour = Color.parseColor("#FFFFFF"); //white
                break;

            case 1:
                colour = Color.parseColor("#FFFF00"); //yellow
                break;

            case 2:
                colour = Color.parseColor("#FF9900"); //orange
                break;

            case 3:
                colour = Color.parseColor("#FF0000"); //red
                break;
        }
        holder.myTextView.setBackgroundColor(colour);
    }

}
