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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GroupProfilerAdapter extends RecyclerView.Adapter<GroupProfilerAdapter.MyViewHolder> {

    //private ArrayList<String> dataList;
    //private boolean coordinates;
    private Context context;
    private ArrayList<String> memberList;
    private final List<Map.Entry<String, String>> mData;
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();

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

    @Override
    public GroupProfilerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        GroupProfilerAdapter.MyViewHolder vh = new GroupProfilerAdapter.MyViewHolder(v);
        return vh;
    }

    public GroupProfilerAdapter(HashMap<String, String> map, Context context) {
        mData = new ArrayList<>(map.entrySet());
        this.context = context;
        //this.coordinates = coordinates;
    }

    /*public String getMemberList(int position){
        return memberList.get(position);
    }*/

    public Map.Entry<String, String> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getKey().hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull GroupProfilerAdapter.MyViewHolder holder, int position) {
        //final String member = getMemberList(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void deleteGroupMember(int position){
        final Map.Entry<String, String> item = getItem(position);
        Log.i("会打印吗", "会吗");

        //update the new 'member' of 'Users'
        mFirestore.collection("Users")
                .document(item.getKey())
                .update("isMemberOf", deleteIsMemberOf());//得到被删除的userId
        //TODO:在userId中删除相应的组
    }

    public Map.Entry<String, String> deleteIsMemberOf(){

        //retrieve 'groupID' from sharepreference
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        String groupId = mPref.getString("groupId", "" );
        Log.i("是否取到groupId", groupId);

        //delete the group from the 'Users' table
        Iterator<Map.Entry<String, String>> iterator = mData.iterator();
        while (iterator.hasNext()){
            if(iterator.next().toString() == groupId){//TODO:得到groupId
                iterator.remove();
            }
        }

        return (Map.Entry<String, String>) iterator;
    }


}
