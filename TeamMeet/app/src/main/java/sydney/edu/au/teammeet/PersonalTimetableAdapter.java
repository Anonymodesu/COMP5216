package sydney.edu.au.teammeet;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class PersonalTimetableAdapter extends TimetableAdapter {

    public PersonalTimetableAdapter(final Context context, final Timetable timetable) {
        super(context, timetable);

        //simple touches alternates colour
        setClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(mTimetable.getWeighting(position) > 0) {
                    mTimetable.setWeighting(position, 0);

                } else {

                    mTimetable.setWeighting(position, 2);
                }

                notifyItemChanged(position);
            }
        });

        //long touches allow for more customised edits
        setLongClickListener(new TimetableAdapter.ItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, final int position) {
                String[] weights = new String[] {"Free", "Low", "Medium", "High"};

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                final View editFields = LayoutInflater.from(context).inflate(R.layout.edit_timeslot, null);

                // Set up the activity text input
                final EditText inputActivity = editFields.findViewById(R.id.activity_input_field);
                // Specify the type of input expected; this, for example, sets the input as plaintext
                inputActivity.setInputType(InputType.TYPE_CLASS_TEXT);
                // Restore existing activity notes
                String activity = mTimetable.getActivity(position);
                if(activity != null) {
                    inputActivity.setText(mTimetable.getActivity(position));
                }

                //Set up radio button weighting input
                final RadioGroup weightingSelection  = editFields.findViewById(R.id.timeslot_weighting_selection);
                weightingSelection.check(weightingToId(mTimetable.getWeighting(position)));

                builder.setView(editFields)
                        .setTitle("Edit Timeslot")
                        .setMessage("Input timeslot weight and associated activity.")

                        .setPositiveButton("Confirm", new
                                DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //save the weighting and activity back to timetable
                                        String currentActivity = inputActivity.getText().toString();
                                        int currentWeighting = idToWeighting(weightingSelection.getCheckedRadioButtonId());
                                        mTimetable.setWeighting(position, currentWeighting);

                                        if(currentWeighting > 0) {
                                            mTimetable.setActivity(position, currentActivity);
                                            
                                            //cant associate activities to weighting==0 timeslots
                                        } else if (!currentActivity.equals("")) {
                                            Toast.makeText(context, "Can't associate an activity to free timeslots", Toast.LENGTH_LONG).show();
                                            mTimetable.setActivity(position, "");
                                        }

                                        notifyItemChanged(position);
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


    //returns XML radio button ID associated with the weighting
    private int weightingToId(int weighting) {
        switch(weighting) {
            case 0: return R.id.free_radio;
            case 1: return R.id.low_radio;
            case 2: return R.id.medium_radio;
            case 3: return R.id.high_radio;
            default:
                Log.d("INCORRECT WEIGHTING", "" + weighting);
                return -1;
        }
    }

    //returns the weighting associated with the radio button ID
    private int idToWeighting(int id) {
        switch(id) {
            case R.id.free_radio: return 0;
            case R.id.low_radio: return 1;
            case R.id.medium_radio: return 2;
            case R.id.high_radio: return 3;
            default:
                Log.d("INCORRECT ID", "" + id);
                return -1;
        }
    }
}
