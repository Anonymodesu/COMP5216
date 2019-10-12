package sydney.edu.au.teammeet;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserViewAdapter extends RecyclerView.Adapter<UserViewAdapter.MyViewHolder> {
    //private String[] mDataset;

    private Context context;
    private ArrayList<String> memberIdList;
    private ArrayList<String> memberNameList;
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();

    private static final String TAG="UserViewAdapter";


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mtextView, leave_link, delete_link;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.group_item);
            mtextView = v.findViewById(R.id.group_name);
            leave_link = v.findViewById(R.id.link_leave);
            delete_link = v.findViewById(R.id.link_delete);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserViewAdapter(ArrayList<String> memberNameList, ArrayList<String> memberIdList, Context context) {
        //mDataset = dataset;
        this.memberNameList = memberNameList;
        this.memberIdList = memberIdList;
        this.context = context;
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
        holder.mtextView.setText(memberNameList.get(position));
        //holder.mtextView.setText(showMemberName(memberIdList).get(position));
        holder.leave_link.setVisibility(View.GONE);
        holder.delete_link.setVisibility(View.GONE);
        holder.mtextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return memberNameList.size();
    }

    public String getMemberList(int position){
        return memberIdList.get(position);
    }

    //retrieve groupId from sharepreference
    public String getGroupId(){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        String groupId = mPref.getString("groupId", "" );
        Log.i("是否取到groupId", groupId);
        return groupId;
    }

    public void deleteGroupMember(final Context context, final int position){

        deleteGroupFromMember(position);
        //remove data item from list
        memberNameList.remove(position);
        //update recycle view
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());

    }

    public void deleteMemberFromGroup(final int position){

        final DocumentReference groupRef = mFirestore.collection("Groups").document(getGroupId());
        groupRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group= documentSnapshot.toObject(Group.class);
                        ArrayList<String> members = group.getMembers();

                        String memberId = getMemberList(position);
                        //remove member from members
                        members.remove(memberId);

                        group.setMembers(members);
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

    public void deleteGroupFromMember(final int position){

        String userId = getMemberList(position);

        final DocumentReference userRef = mFirestore.collection("Users").document(userId);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User userObject = documentSnapshot.toObject(User.class);
                        HashMap<String, String> memberofGroups = userObject.getIsMemberOf();
                        memberofGroups.remove(getGroupId());

                        userObject.setIsMemberOf(memberofGroups);
                        userRef.set(userObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "Member of group deleted from user collection");
                            }
                        });
                        deleteMemberFromGroup(position);
                    }
                });
    }

}