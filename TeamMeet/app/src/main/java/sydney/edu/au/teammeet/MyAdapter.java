package sydney.edu.au.teammeet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private static final String TAG="MyAdapter";
    private final List<Map.Entry<String, String>> mData;
    private String[] mDataset;
    private String[] mKeys;
    private boolean coordinates;
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public CardView mCardView;
        public TextView groupName,leave_link;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.group_item);
            groupName = v.findViewById(R.id.group_name);
            leave_link = v.findViewById(R.id.link_leave);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
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
        holder.leave_link.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.dialog_delete_title);
                builder.setMessage(R.string.dialog_delete_msg);
                builder.setPositiveButton(R.string.dialog_delete_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (coordinates) {
                            //delete document
                            mFirestore.collection("Groups").document(item.getKey())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Group successfully deleted!");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            assert user != null;
                                            String userId = user.getUid();
                                            final DocumentReference userRef = mFirestore.collection("Users").document(userId);
                                            userRef.get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            final User currentUser = documentSnapshot.toObject(User.class);
                                                            assert currentUser != null;

                                                            final HashMap coordinates = currentUser.getCoordinates();
                                                            if (coordinates != null) {
                                                                coordinates.remove(item.getKey());
                                                                mData.remove(position);
                                                            }

                                                            currentUser.setCoordinates(coordinates);
                                                            userRef.set(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d(TAG, "group deleted from user collection!");
                                                                    notifyItemRemoved(position);
                                                                    notifyItemRangeChanged(position, getItemCount());
                                                                }
                                                            });
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error deleting group", e);
                                        }
                                    });
                        } else {
                            //a user deletes himself/herself from a group
                            final DocumentReference docRef = mFirestore.collection("Groups").document(mKeys[position]);
                            docRef.get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            final Group group = documentSnapshot.toObject(Group.class);
                                            assert group != null;
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            assert user != null;
                                            String userId = user.getUid();
                                            final DocumentReference userRef = mFirestore.collection("Users").document(userId);
                                            userRef.get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            User currentUser = documentSnapshot.toObject(User.class);
                                                            assert currentUser != null;
                                                            HashMap<String, String> member = currentUser.getIsMemberOf();
                                                            if (member != null) {
                                                                member.remove(item.getKey());
                                                                mData.remove(position);
                                                            }
                                                            currentUser.setCoordinates(member);
                                                            userRef.set(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d(TAG, "user successfully leave group from user collection!");
                                                                    notifyItemRemoved(position);
                                                                    notifyItemRangeChanged(position, getItemCount());
                                                                }
                                                            });
                                                        }
                                                    });

                                            ArrayList<String> oldList = group.getMembers();
                                            oldList.remove(userId);
                                            if(oldList.size()==0){
                                                mFirestore.collection("Groups").document(item.getKey())
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "Empty Group successfully deleted!");
                                                            }
                                                        });
                                            }
                                            group.setMembers(oldList);
                                            docRef.set(group).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d(TAG, "user successfully leave group from group collection!");
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) { // User cancelled the dialog
// Nothing happens
                    }
                });
                builder.create().show();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
//        return mDataset.length;
        return mData.size();
    }

    public void deleteGroupCard(final int position){

        final Map.Entry<String, String> item = getItem(position);

        if (coordinates) {
            //delete document
            mFirestore.collection("Groups").document(item.getKey())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Group successfully deleted!");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            String userId = user.getUid();
                            final DocumentReference userRef = mFirestore.collection("Users").document(userId);
                            userRef.get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            final User currentUser = documentSnapshot.toObject(User.class);
                                            assert currentUser != null;

                                            final HashMap coordinates = currentUser.getCoordinates();
                                            if (coordinates != null) {
                                                coordinates.remove(item.getKey());
                                                mData.remove(position);
                                            }
                                            currentUser.setCoordinates(coordinates);
                                            //更新数据
                                            userRef.set(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d(TAG, "group deleted from user collection!");
                                                    notifyItemRemoved(position);
                                                    notifyItemRangeChanged(position, getItemCount());
                                                }
                                            });
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting group", e);
                        }
                    });
        }

    }

}