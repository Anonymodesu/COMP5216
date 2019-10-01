package sydney.edu.au.teammeet;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;


//Base Timetable class for personal timetables and group timetable adapters
public abstract class TimetableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int TIMESLOT_VIEW_TYPE = 0;
    protected static final int DAY_VIEW_TYPE = 1;
    protected static final int HOUR_VIEW_TYPE = 2;

    protected static final int SMALL_CELL_SIZE = 150;
    protected static final int LARGE_CELL_SIZE = 300;

    //add 1 to to account for row descriptors
    private static final int ITEMS_PER_ROW = Timetable.NUM_DAYS + 1;

    protected final int cellSize;

    protected Timetable mTimetable;
    protected LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    protected Context mContext;

    // data is passed into the constructor
    public TimetableAdapter(Context context, final Timetable timetable, int cellSize) {
        this.mInflater = LayoutInflater.from(context);
        this.mTimetable = timetable;
        this.mContext = context;
        this.cellSize = cellSize;
    }

    //return the type of cell
    @Override
    public int getItemViewType(int adapterPos) {
        if(adapterPos < ITEMS_PER_ROW) { //descriptors Mon, Tue, Wed, etc.
            return DAY_VIEW_TYPE;

        } else if(adapterPos % ITEMS_PER_ROW == 0) {  //descriptors 9:00, 9:30, 10:00 etc.
            return HOUR_VIEW_TYPE;

        } else  { //editable timeslots
            return TIMESLOT_VIEW_TYPE;
        }
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

        switch(viewType) {
            case TIMESLOT_VIEW_TYPE: return new TimeslotViewHolder(view);

            case DAY_VIEW_TYPE: case HOUR_VIEW_TYPE: return new DescriptorViewHolder(view);

            default:
                throw new RuntimeException("Wrong viewType in TimetableAdapter.onCreateViewHolder()");
        }
    }

    // binds the data to View of each descriptor cell
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int adapterPos) {
        int viewType = getItemViewType(adapterPos);
        TextView descriptorText = ((DescriptorViewHolder) holder).descriptorText;

        if(viewType == DAY_VIEW_TYPE) {
            if(adapterPos == 0) { //first cell is empty
                descriptorText.setText("");

            } else {
                String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                descriptorText.setText(days[adapterPos - 1]);
            }

        } else if(viewType == HOUR_VIEW_TYPE) {
            //we subtract ITEMS_PER_ROW from adapterPos to account for the first row being the days
            int currentHour  = Timetable.START_HOUR + (adapterPos - ITEMS_PER_ROW) / (2 * ITEMS_PER_ROW);
            String displayText = "" + currentHour;

            if((adapterPos - ITEMS_PER_ROW) % (2 * ITEMS_PER_ROW) == 0){
                displayText += ":00";
            } else {
                displayText += ":30";
            }

            descriptorText.setText(displayText);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        //add 1 to account for row/column descriptors
        return (Timetable.NUM_DAYS + 1) * (Timetable.NUM_HALF_HOURS + 1);
    }

    // stores and recycles day/hour descriptors as they are scrolled off screen
    public class DescriptorViewHolder extends RecyclerView.ViewHolder {
        protected TextView descriptorText;

        //getAdapterPosition() and getItemViewType() doesn't work inside constructor
        //so we have to generate the descriptor values in onBindViewHolder()
        public DescriptorViewHolder(View itemView) {
            super(itemView);
            descriptorText = itemView.findViewById(R.id.timetable_cell_text);
        }
    }

    // stores and recycles timeslots as they are scrolled off screen
    public class TimeslotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected TextView myTextView;

        TimeslotViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.timetable_cell_text);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) {
                return mLongClickListener.onItemLongClick(view, getAdapterPosition());
            }
            return true;
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mTimetable.getActivity(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // parent activity will implement this method to respond to long click events
    public interface ItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }


    //returns the index position of the timetable given the position of the selected cell in the adapter
    protected static int adapterPosToTimetablePos(int adapterPos) {

        return adapterPos
                - ITEMS_PER_ROW // - first row of day descriptors
                - (adapterPos / ITEMS_PER_ROW); // - one hour descriptor per row
    }

}
