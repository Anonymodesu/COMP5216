package sydney.edu.au.teammeet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import android.widget.RadioGroup;
import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.google.gson.Gson;

import org.litepal.LitePal;

import static android.widget.Toast.LENGTH_SHORT;

public class PersonalTimetableAdapter extends TimetableAdapter {

    private Context context;

    public PersonalTimetableAdapter(final Context context, final Timetable timetable, int cellSize) {
        super(context, timetable, cellSize);
        this.context = context;
        setupClickListeners(context, timetable);
    }


    // binds the data to View of each cell
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int adapterPos) {
        int viewType = getItemViewType(adapterPos);
        if(viewType == TIMESLOT_VIEW_TYPE) { //generate timeslot values

            int timetablePos = adapterPosToTimetablePos(adapterPos);
            TextView textView = ((TimeslotViewHolder) holder).myTextView;

            String text = mTimetable.getActivity(timetablePos);
            if(text == null) {
                text = "";
            }
            textView.setText(text);

            //cell changes colour depending on weighting
            int weighting = mTimetable.getWeighting(timetablePos);
            int colour = Color.WHITE;
            switch(weighting) {
                case 0:
                    colour = Color.parseColor("#FFFFFF"); //white
                    break;

                case 1:
                    colour = Color.parseColor("#B0E0E6"); //blue
                    break;

                case 2:
                    colour = Color.parseColor("#90EE90"); //green
                    break;

                case 3:
                    colour = Color.parseColor("#FF7F50"); //orange
                    break;
            }
            textView.setBackgroundColor(colour);

        } else { //let super class handle descriptor cells
            super.onBindViewHolder(holder, adapterPos);
        }



    }

    private void setupClickListeners(final Context context, final Timetable timetable) {
        //simple touches alternates colour
        setClickListener(new ItemClickListener() {
            @Override

            public void onItemClick(View view, int adapterPos) {
                int timetablePos = adapterPosToTimetablePos(adapterPos);

                if(mTimetable.getWeighting(timetablePos) > 0) {
                    mTimetable.setWeighting(timetablePos, 0);

                } else {

                    mTimetable.setWeighting(timetablePos, 2);
                }

                notifyItemChanged(adapterPos);
            }
        });

        //long touches allow for more customised edits
        setLongClickListener(new TimetableAdapter.ItemLongClickListener() {
            @Override

            public boolean onItemLongClick(View view, final int adapterPos) {
                final int timetablePos = adapterPosToTimetablePos(adapterPos);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final View editFields = LayoutInflater.from(context).inflate(R.layout.edit_timeslot, null);

                // Set up the activity text input
                final EditText inputActivity = editFields.findViewById(R.id.activity_input_field);
                // Specify the type of input expected; this, for example, sets the input as plaintext
                inputActivity.setInputType(InputType.TYPE_CLASS_TEXT);
                // Restore existing activity notes
                String activity = mTimetable.getActivity(timetablePos);
                if(activity != null) {
                    inputActivity.setText(mTimetable.getActivity(timetablePos));
                }

                //Set up radio button weighting input
                final RadioGroup weightingSelection  = editFields.findViewById(R.id.timeslot_weighting_selection);
                weightingSelection.check(weightingToId(mTimetable.getWeighting(timetablePos)));

                builder.setView(editFields)
                        .setTitle("Edit Timeslot")
                        .setMessage("Input timeslot weight and associated activity.")

                        .setPositiveButton("Confirm", new
                                DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //save the weighting and activity back to timetable
                                        String currentActivity = inputActivity.getText().toString();
                                        int currentWeighting = idToWeighting(weightingSelection.getCheckedRadioButtonId());
                                        mTimetable.setWeighting(timetablePos, currentWeighting);

                                        if(currentWeighting > 0) {
                                            mTimetable.setActivity(timetablePos, currentActivity);

                                            //cant associate activities to weighting==0 timeslots
                                        } else if (!currentActivity.equals("")) {
                                            Toast.makeText(context, "Can't associate an activity to free timeslots", Toast.LENGTH_LONG).show();
                                            mTimetable.setActivity(timetablePos, "");
                                        }
                                        notifyItemChanged(adapterPos);
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


    public void clearTimetable() {
        mTimetable = new Timetable();
        notifyDataSetChanged();
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
