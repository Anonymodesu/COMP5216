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
public abstract class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {
    protected Timetable mTimetable;
    protected TimetableBean mTimetableBean;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    protected Context mContext;

    // data is passed into the constructor
    public TimetableAdapter(Context context, final Timetable timetable) {
        this.mInflater = LayoutInflater.from(context);
        this.mTimetable = timetable;
        this.mContext = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.timetable_cell, parent, false);
        return new ViewHolder(view);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mTimetable.getLength();
    }

    public void clearTimetable() {
        mTimetable = new Timetable();
        notifyDataSetChanged();
        //delete data from the local database
        LitePal.deleteAll(TimetableBean.class);
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected TextView myTextView;

        ViewHolder(View itemView) {
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

}
