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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupProfileMemberAdapter extends RecyclerView.Adapter<GroupProfileMemberAdapter.MyViewHolder> {
    //private String[] mDataset;

    private Context context;
    private ArrayList<String> memberIdList;
    private ArrayList<String> memberNameList;

    private OnItemClicked onClick;

    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();

    private static final String TAG="MemberViewAdapter";


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView groupMemberIcon, groupMemberName;

        public MyViewHolder(View v) {
            super(v);
            groupMemberIcon = v.findViewById(R.id.groupmember_icon);
            groupMemberName = v.findViewById(R.id.groupmember_name);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public GroupProfileMemberAdapter(ArrayList<String> memberNameList, ArrayList<String> memberIdList, Context context) {
        //mDataset = dataset;
        this.memberNameList = memberNameList;
        this.memberIdList = memberIdList;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupProfileMemberAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.groupprofile_groupmember_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.groupMemberName.setText(memberNameList.get(position));
        holder.groupMemberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onItemClick(position);
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

    public String getMemberNameList(int position){
        return memberNameList.get(position);
    }

    //retrieve groupId from sharepreference
    public String getGroupId(){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        String groupId = mPref.getString("groupId", "" );
        return groupId;
    }


    public void deleteGroupMember(final Context context, final int position){

        deleteGroupFromMember(position);

        //save member name to sharepreference
        saveDeletedMemberName(memberNameList.get(position));

    }

    public void deleteMemberFromList(final Context context, final int position) {

        //remove data item from list
        memberNameList.remove(position);
        memberIdList.remove(position);
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

                        //save deleted memberId
                        saveDeletedMemberId(memberId);

                        //remove member from members
                        members.remove(memberId);

                        group.setMembers(members);
                        groupRef.set(group)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        deleteMemberFromList(context, position);
                                        Log.d(TAG, "User deleted from group");
                                    }
                                });

                        /*group.setMembers(members);
                        groupRef.set(group)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        deleteMemberFromList(context, position);
                                        Log.d(TAG, "User deleted from group");
                                    }
                                });*/
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
                        userObject.removeFromMembers(getGroupId());

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

    //save the memberId which is deleted to SharePreference
    public void saveDeletedMemberName(String deletedMemberName){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("deletedMemberName", deletedMemberName);
        editor.commit();
    }

    //retrieve deleted memberName from sharepreference
    public String getDeletedMemberName(){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        String deletedMemberName = mPref.getString("deletedMemberName", "" );
        return deletedMemberName;
    }

    //save the memberName which is deleted to SharePreference
    public void saveDeletedMemberId(String deletedMemberId){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("deletedMemberId", deletedMemberId);
        editor.commit();
    }

    //retrieve deleted memberId from sharepreference
    public String getDeletedMemberId(){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        String deletedMemberId = mPref.getString("deletedMemberId", "" );
        return deletedMemberId;
    }

    //insert the deleted memberId
    public void insertDeletedMember(int position){

        memberNameList.add(position, getDeletedMemberName());
        memberIdList.add(position, getDeletedMemberId());
        notifyItemInserted(position);
        notifyItemRangeChanged(0, getItemCount());
        /*if (position != memberNameList.size() - 1) {
            notifyItemRangeChanged(position, memberNameList.size() - position);
        }*/

        //insertTheGroupToDeletedMember(getGroupId());

        //insertDeletedMemberToTheGroup(getGroupId(), position);
    }

    //insert the deleted memberId to previous group
    public void insertDeletedMemberToTheGroup(final String groupId, final int position){
        final DocumentReference groupRef = mFirestore.collection("Groups").document(groupId);
        groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Group group = documentSnapshot.toObject(Group.class);
                ArrayList<String> memberList = group.getMembers();

                memberList.add(getDeletedMemberId());
                group.setMembers(memberList);
                groupRef.set(group)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                insertDeletedMember(position);
                            }
                        });
                insertTheGroupToDeletedMember(groupId);

                /*if(!memberList.contains(getDeletedMemberId())){
                    memberList.add(getDeletedMemberId());
                    group.setMembers(memberList);
                    groupRef.set(group)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    notifyItemInserted(position);
                                    notifyItemRangeChanged(position, getItemCount());
                                }
                            });
                    insertTheGroupToDeletedMember(groupId);
                }*/
            }
        });

       /* memberNameList.add(position, getDeletedMemberName());
        notifyItemInserted(position);
        notifyItemRangeChanged(position, getItemCount());*/
    }

    public void insertTheGroupToDeletedMember(final String groupId){
        final DocumentReference usersRef = mFirestore.collection("Users").document(getDeletedMemberId());
        usersRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                HashMap<String, String> isMemberOfList = user.getIsMemberOf();

                //TODO: pur the group name later
                isMemberOfList.put(groupId, "");

                user.setIsMemberOf(isMemberOfList);
                usersRef.set(user);
            }
        });
    }

    public void removeDeletedMember(int position){
        //save member name to sharepreference
        saveDeletedMemberName(memberNameList.get(position));
        memberNameList.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void addDeletedMember(int position){
        memberNameList.add(position,getDeletedMemberName());
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void setOnClick(OnItemClicked onClick)
    {
        this.onClick = onClick;
    }

}