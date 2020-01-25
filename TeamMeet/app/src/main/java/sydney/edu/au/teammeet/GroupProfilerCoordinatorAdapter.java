package sydney.edu.au.teammeet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupProfilerCoordinatorAdapter extends RecyclerView.Adapter<GroupProfilerCoordinatorAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<String> coordinatorList;
    private ArrayList<String> coordPhotoList;
    //private final List<Map.Entry<String, String>> mData;

    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView coordinatorName;
        CircleImageView coordinatorIcon;

        public MyViewHolder(View v) {
            super(v);
            coordinatorIcon = v.findViewById(R.id.coord_profile_image);
            coordinatorName = v.findViewById(R.id.coorinator_name);
        }
    }

    @Override
    public GroupProfilerCoordinatorAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.groupprofile_coordinators_item, parent, false);
        GroupProfilerCoordinatorAdapter.MyViewHolder vh = new GroupProfilerCoordinatorAdapter.MyViewHolder(v);
        return vh;
    }

    public GroupProfilerCoordinatorAdapter(ArrayList<String> coordinatorList,  ArrayList<String> coordPhotoList, Context context) {
        this.coordinatorList = coordinatorList;
        this.context = context;
        this.coordPhotoList = coordPhotoList;
    }

    /*public String getMemberList(int position){
        return memberList.get(position);
    }*/

    public String getCoordinatorList(int position) {
        return coordinatorList.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupProfilerCoordinatorAdapter.MyViewHolder holder, int position) {
        holder.coordinatorName.setText(coordinatorList.get(position));
        if(coordPhotoList.get(position) != null){
            Picasso.get()
                    .load(coordPhotoList.get(position))
                    .fit().centerCrop()
                    .placeholder(R.drawable.profile)
                    .into(holder.coordinatorIcon);
        }
    }

    @Override
    public int getItemCount() {
        return coordinatorList.size();
    }

    public void addMemberToAdapter(final Context context, String coordinator) {
        coordinatorList.add(coordinator);
        notifyDataSetChanged();
    }

}
