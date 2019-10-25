package sydney.edu.au.teammeet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private static final String TAG="MyAdapter";
    private final List<Map.Entry<String, String>> mData;
    private ArrayList<String> membersList;
    private boolean coordinates;

    private GroupSelfDialog selfDialog;

    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public CardView mCardView;
        public TextView groupName,leave_link, delete_link;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.group_item);
            groupName = v.findViewById(R.id.group_name);
            leave_link = v.findViewById(R.id.link_leave);
            delete_link = v.findViewById(R.id.link_delete);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    //set up adapter with array list
    public MyAdapter(HashMap<String, String> map, boolean coordinates) {
        mData = new ArrayList<>(map.entrySet());
        this.coordinates = coordinates;
    }

    public Map.Entry<String, String> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getKey().hashCode();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Map.Entry<String, String> item = getItem(position);

        holder.groupName.setText(item.getValue());
        holder.groupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, GroupProfileActivity.class);
                intent.putExtra("coordinates", coordinates);
                intent.putExtra("groupname", item.getValue());
                intent.putExtra("groupid", item.getKey());
                context.startActivity(intent);
            }
        });
        //User cannot leave group if is coordinator
        if(coordinates){
            holder.leave_link.setVisibility(View.GONE);
        }else {
            //handle leave action
            holder.leave_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    leaveGroup(context, position, item.getKey());
                }
            });
        }
        //delete a group
        if(coordinates){
            holder.delete_link.setVisibility(View.VISIBLE);
            //handle leave action
            holder.delete_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    deleteGroup(context, position, item.getKey());
        }
            });
        }else {
            holder.delete_link.setVisibility(View.GONE);
        }
    }

    public void leaveGroup(final Context context, final int position, final String groupId){
        //confirm to leave dialog
        selfDialog = new GroupSelfDialog(context);
        selfDialog.setTitle("Leave Group");
        selfDialog.setMessage("Are you sure to leave this group?");
        selfDialog.setYesOnclickListener("Leave", new GroupSelfDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                deleteUserFromGroupInFirestore(groupId);
                deleteGroupFromUserInFirestore(groupId);
                //remove data item from list
                mData.remove(position);
                //update recycle view
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
                selfDialog.dismiss();
            }
        });
        selfDialog.setNoOnclickListener("Cancel", new GroupSelfDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                selfDialog.dismiss();
            }
        });
        selfDialog.show();
    }

    public void deleteUserFromGroupInFirestore(String groupId){
        final DocumentReference groupRef = mFirestore.collection("Groups").document(groupId);
        groupRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group= documentSnapshot.toObject(Group.class);
                        ArrayList<String> members = group.getMembers();
                        //get current user id
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        String userId = user.getUid();
                        //remove user from members
                        group.removeMember(userId);
                        groupRef.set(group)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "User deleted from group");
                                    }
                                });
                    }
                });
    }

    public void deleteGroupFromUserInFirestore(final String groupId){
        //get current user object
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        final DocumentReference userRef = mFirestore.collection("Users").document(userId);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User userObject = documentSnapshot.toObject(User.class);
                        userObject.removeFromMembers(groupId);
                        userRef.set(userObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "Member of group deleted from user collection");
                            }
                        });
                    }
                });
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void deleteGroup(final Context context, final int position, final String groupId){
        //confirm to leave dialog
        /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_delete_title);
        builder.setMessage(R.string.dialog_delete_msg);
        builder.setPositiveButton(R.string.dialog_delete_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                final DocumentReference groupRef = mFirestore.collection("Groups").document(groupId);
                groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group= documentSnapshot.toObject(Group.class);
                        ArrayList<String> membersList = group.getMembers();

                        if(membersList.size() == 0){
                            deleteGroupFromCoordinators(groupId, position);
                        }else {
                            deleteGroupFromMembers(groupId, position);
                        }
                        //remove data item from list
                        mData.remove(position);
                        //update recycle view
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) { // User cancelled the dialog Nothing happens
            }
        });
        builder.create().show();*/

        selfDialog = new GroupSelfDialog(context);
        selfDialog.setTitle("Delete Group");
        selfDialog.setMessage("Are you sure to delete the group?");
        selfDialog.setYesOnclickListener("Yes", new GroupSelfDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                final DocumentReference groupRef = mFirestore.collection("Groups").document(groupId);
                groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group= documentSnapshot.toObject(Group.class);
                        ArrayList<String> membersList = group.getMembers();

                        if(membersList.size() == 0){
                            deleteGroupFromCoordinators(groupId, position);
                        }else {
                            deleteGroupFromMembers(groupId, position);
                        }
                        //remove data item from list
                        mData.remove(position);
                        //update recycle view
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                    }
                });
                selfDialog.dismiss();
            }
        });
        selfDialog.setNoOnclickListener("No", new GroupSelfDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                selfDialog.dismiss();
            }
        });
        selfDialog.show();
    }

    public void deleteGroupFromMembers(final String groupId, final int position){

        final DocumentReference groupRef = mFirestore.collection("Groups").document(groupId);
        groupRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group= documentSnapshot.toObject(Group.class);
                        ArrayList<String> membersList = group.getMembers();

                        //get every group member info by their ID
                        for (String memberId : membersList) {
                            Log.i("The memberId is: ", memberId);

                            final DocumentReference memberRef = mFirestore.collection("Users").document(memberId);
                            memberRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = documentSnapshot.toObject(User.class);
                                    user.removeFromMembers(groupId);

                                    memberRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            deleteGroupFromCoordinators(groupId, position);
                                            Log.d(TAG, "group deleted from members!");
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }

    public void deleteGroupFromCoordinators(final String groupId, final int position){

        final DocumentReference groupRef = mFirestore.collection("Groups").document(groupId);
        groupRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group= documentSnapshot.toObject(Group.class);
                        ArrayList<String> coordinatorsList = group.getCoordinators() != null ? group.getCoordinators() : new ArrayList<String>();

                        //get every group coordinator info by their ID
                        for (String coordinatorId : coordinatorsList) {
                            Log.i("The memberId is:", coordinatorId);

                            final DocumentReference memberRef = mFirestore.collection("Users").document(coordinatorId);
                            memberRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = documentSnapshot.toObject(User.class);
                                    user.removeFromCoordinates(groupId);

                                    memberRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            deleteTheGroupFromFirebase(groupId);
                                            Log.d(TAG, "group deleted from members!");
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }

    public void deleteTheGroupFromFirebase(final String groupId){

        mFirestore.collection("Groups").document(groupId)
                .delete();
    }





}