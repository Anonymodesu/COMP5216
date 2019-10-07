package sydney.edu.au.teammeet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserViewAdapter extends RecyclerView.Adapter<UserViewAdapter.MyViewHolder> {
    private String[] mDataset;
    private String[] mUserids;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mtextView, leave_link;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.group_item);
            mtextView = v.findViewById(R.id.group_name);
            leave_link = v.findViewById(R.id.link_leave);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserViewAdapter(String[] dataset, String[] userIds) {
        mDataset = dataset;
        mUserids = userIds;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mtextView.setText(mDataset[position]);
        holder.leave_link.setVisibility(View.GONE);
        holder.mtextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Context context = view.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Make " + mDataset[position] + " a coordinator?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //on clicking yes do something here
                    }
                });
                builder.create().show();
                return true;
            }
        });
        holder.mtextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}