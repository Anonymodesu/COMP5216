package sydney.edu.au.teammeet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.widget.RadioGroup;
import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;



import org.litepal.LitePal;

import java.util.Set;

import static android.widget.Toast.LENGTH_SHORT;


public class PersonalTimetableAdapter extends TimetableAdapter {
    private ItemTouchListener mItemTouchListener;
    private RecyclerView parentRecyclerView;
    private String fillActivity;
    private Set<Integer> groupMeetingTimes;

    public PersonalTimetableAdapter(final Context context, final Timetable timetable, Set<Integer> groupMeetingTimes, int cellSize) {
        super(context, timetable, cellSize);
        setupClickListeners(context);
        setOnTouchListener(null);
        fillActivity = "";
        this.groupMeetingTimes = groupMeetingTimes;
    }


    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.timetable_cell, parent, false);

        //set cell size
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = cellSize;
        params.width = cellSize;
        view.setLayoutParams(params);

        switch (viewType) {
            case TIMESLOT_VIEW_TYPE:
                return new PersonalTimetableAdapter.TimeslotViewHolder(view);

            case DAY_VIEW_TYPE:
            case HOUR_VIEW_TYPE:
                return new DescriptorViewHolder(view);

            default:
                throw new RuntimeException("Wrong viewType in PersonalTimetableAdapter.onCreateViewHolder()");
        }
    }

    // binds the data to View of each cell
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int adapterPos) {
        int viewType = getItemViewType(adapterPos);
        if(viewType == TIMESLOT_VIEW_TYPE) { //generate timeslot values

            int timetablePos = adapterPosToTimetablePos(adapterPos);
            TextView textView = ((TimeslotViewHolder) holder).myTextView;

            if(groupMeetingTimes.contains(timetablePos)) {
                textView.setText("Group Meeting");
                textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.group_meeting_colour));

            } else {
                String text = mTimetable.getActivity(timetablePos);
                if(text == null) {
                    text = "";
                }
                textView.setText(text);

                //cell changes colour depending on weighting
                int weighting = mTimetable.getWeighting(timetablePos);
                int colour = weightingToColour(weighting);
                textView.setBackgroundColor(colour);
            }

        } else { //let super class handle descriptor cells
            super.onBindViewHolder(holder, adapterPos);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentRecyclerView = recyclerView;
    }

    //non standard mode fill up cells on touch
    public void switchMode(PersonalTimetableActivity.Mode mode) {
        if(mode == PersonalTimetableActivity.Mode.STANDARD) {
            setupClickListeners(mContext);
            setOnTouchListener(null);
        } else {

            setupTouchListener(mContext, mode);
            setClickListener(null);
            setLongClickListener(null);
        }
    }

    //touching timeslots in nonstandard mode sets them to the specified colour automagically
    private void setupTouchListener(final Context context, final PersonalTimetableActivity.Mode mode) {

        setOnTouchListener(new ItemTouchListener() {
            @Override
            public boolean onItemTouch(View view, int adapterPos) {

                int timetablePos = adapterPosToTimetablePos(adapterPos);
                int weighting = 0;
                int oldWeighting = mTimetable.getWeighting(timetablePos);
                String oldActivity = mTimetable.getActivity(timetablePos);

                if(groupMeetingTimes.contains(timetablePos)) { //can't modify group meeting times
                    return true;
                }

                switch(mode) {
                    case FREE:
                        weighting = 0;
                        break;
                    case LOW:
                        weighting = 1;
                        break;

                    case MEDIUM:
                        weighting = 2;
                        break;

                    case HIGH:
                        weighting = 3;
                        break;

                    case STANDARD:
                        throw new IllegalArgumentException("can't use standard mode for touch listeners");
                }


                if(weighting != oldWeighting || !fillActivity.equals(oldActivity)) {
                    mTimetable.setWeighting(timetablePos, weighting);
                    if(weighting == 0) {
                        mTimetable.setActivity(timetablePos, "");
                    } else {
                        mTimetable.setActivity(timetablePos, fillActivity);
                    }
                    notifyItemChanged(adapterPos);
                }


                return true;
            }
        });
    }

    private void setupClickListeners(final Context context) {
        //simple touches alternates colour
        setClickListener(new ItemClickListener() {
            @Override

            public void onItemClick(View view, int adapterPos) {
                int timetablePos = adapterPosToTimetablePos(adapterPos);

                if(groupMeetingTimes.contains(timetablePos)) { //can't modify group meeting times
                    return;
                }

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

                if(groupMeetingTimes.contains(timetablePos)) { //can't modify group meeting times
                    return true;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final View editFields = LayoutInflater.from(context).inflate(R.layout.edit_timeslot, null);

                // Set up the activity text input
                final EditText inputActivity = editFields.findViewById(R.id.activity_input_field);
                // Specify the type of input expected; this, for example, sets the input as plaintext
                inputActivity.setInputType(InputType.TYPE_CLASS_TEXT);
                // Restore existing activity notes
                inputActivity.setText(mTimetable.getActivity(timetablePos));

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

    public void setFillActivity(String activity) {
        if(activity == null) {
            fillActivity = "";
        } else {
            fillActivity = activity;
        }
    }

    public void clearTimetable() {
        mTimetable = new Timetable();
        notifyDataSetChanged();
    }

    private int weightingToColour(int weighting) {
        int colour;
        switch(weighting) {
            case 0:
                colour = ContextCompat.getColor(mContext, R.color.free_priority_colour);
                break;

            case 1:
                colour = ContextCompat.getColor(mContext, R.color.low_priority_colour);
                break;

            case 2:
                colour = ContextCompat.getColor(mContext, R.color.medium_priority_colour);
                break;

            case 3:
                colour = ContextCompat.getColor(mContext, R.color.high_priority_colour);
                break;

            default:
                throw new IllegalArgumentException("cant find colour for weighting " + weighting);
        }
        return colour;
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

    public Timetable getTimetable() {
        return mTimetable;
    }

    public Set<Integer> getGroupMeetingTimes() {
        return groupMeetingTimes;
    }

    public class TimeslotViewHolder extends TimetableAdapter.TimeslotViewHolder implements View.OnTouchListener {
        TimeslotViewHolder(View itemView) {
            super(itemView);
            itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent me) {

            if(mItemTouchListener == null) {
                return false;
            }

            int[] parentCoords = new int[2];
            parentRecyclerView.getLocationOnScreen(parentCoords);
            float relativeX = me.getRawX() - parentCoords[0] + parentRecyclerView.getLeft();
            float relativeY = me.getRawY() - parentCoords[1] + parentRecyclerView.getTop();

            View touchedView = parentRecyclerView.findChildViewUnder(relativeX, relativeY);

            if(touchedView == null) {
                return false;
            }

            else if(view == touchedView) {
                return mItemTouchListener.onItemTouch(view, getAdapterPosition());

            } else {
                RecyclerView.ViewHolder touchedHolder = parentRecyclerView.findContainingViewHolder(touchedView);

                if(touchedHolder == null) {
                    return false;
                } else {

                    Log.d("qwert", parentCoords[0] + " " + parentCoords[1]);
                    //Log.d("qwert", ""  + (touchedHolder.getAdapterPosition() - getAdapterPosition()));
                    return mItemTouchListener.onItemTouch(touchedView, touchedHolder.getAdapterPosition());
                }

            }

        }

    }

    public void setOnTouchListener(ItemTouchListener itemTouchListener) {
        mItemTouchListener = itemTouchListener;
    }



    public interface ItemTouchListener {
        boolean onItemTouch(View view, int position);
    }

}
