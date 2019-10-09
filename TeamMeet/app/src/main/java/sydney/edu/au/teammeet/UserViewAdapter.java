package sydney.edu.au.teammeet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserViewAdapter extends RecyclerView.Adapter<UserViewAdapter.MyViewHolder> {
    private final List<Map.Entry<String, String>> mDataset;
    private boolean coordinates;
    private String groupID;
    private String groupName;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


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
    public UserViewAdapter(HashMap<String, String> dataset, boolean coordinates, String groupID) {
        mDataset = new ArrayList<>(dataset.entrySet());

        this.coordinates = coordinates;
        this.groupID = groupID;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    public Map.Entry<String, String> getItem(int position) {
        return mDataset.get(position);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final MyViewHolder mHolder = holder;

        holder.mtextView.setText(getItem(position).getValue());
        holder.leave_link.setVisibility(View.GONE);
        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (coordinates) {
                    final Context context = view.getContext();

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Make " + getItem(position).getValue() + " a coordinator?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //on clicking yes do something here

                            final DocumentReference groupDoc = mFirestore.collection("Groups").document(groupID);
                            groupDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Group group = documentSnapshot.toObject(Group.class);
                                    group.removeMember(getItem(position).getKey());
                                    group.addCoordinator(getItem(position).getKey());
                                    groupName = group.getGroupName();


                                    groupDoc.set(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            final DocumentReference userDoc = mFirestore.collection("Users").document(getItem(position).getKey());
                                            userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    User user = documentSnapshot.toObject(User.class);

                                                    user.removeFromMembers(groupID);
                                                    user.addToCoordinates(groupID, groupName);

                                                    userDoc.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    builder.create().show();
                    return true;
                }

                return true;
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}