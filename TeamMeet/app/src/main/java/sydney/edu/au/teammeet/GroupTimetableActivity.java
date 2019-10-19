package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GroupTimetableActivity extends BaseActivity {

    private FirebaseFunctions mFunctions;
    private FirebaseFirestore mFirestore;

    private String groupID;
    private String groupName;
    private boolean coordinates;

    private ListView meetingsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_timetable);
        mFunctions = FirebaseFunctions.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser userAuth = mAuth.getCurrentUser();

        meetingsView = findViewById(R.id.meeting_times_listview);

        groupID = getIntent().getStringExtra("groupID");
        mFirestore.collection("Groups").document(groupID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot result = task.getResult();
                    Group group = result.toObject(Group.class);
                    groupName = group.getGroupName();

                    ArrayList<String> coordinators = group.getCoordinators();
                    coordinates = coordinators.contains(userAuth.getUid());

                    //for some reason the group object does not contain the times arraylist
                    Map<String, ArrayList<Long>> meetingTimeslots = group.getBestTimes();//(Map<String, ArrayList<Long>>) result.get("bestTimes");

                    if(meetingTimeslots == null) {
                        timetableQuery(null);
                    } else {
                        loadMeetingListView(convertMeetingTimesLong(meetingTimeslots.get("times"), meetingTimeslots.get("weights")));
                    }

                } else {
                    Log.e("group retrieval", task.getException().getMessage());
                }
            }
        });



        setUpGlobalNav(GroupTimetableActivity.this,"Group Meeting Times");
    }

    //two convertMeetingTimes is needed since the return value from the getGroupTimescloud function is Integer
    //while the return value from the Java query is Long ????

    private ArrayList<GroupMeetingAdapter.Meeting> convertMeetingTimesLong(ArrayList<Long> times, ArrayList<Long> weightings) {
        ArrayList<GroupMeetingAdapter.Meeting> meetings = new ArrayList<>();
        for(int i = 0; i < times.size(); i++) {
            int a = (int) (long) times.get(i);
            int b = (int) (long) weightings.get(i);
            GroupMeetingAdapter.Meeting meeting = new GroupMeetingAdapter.Meeting(a, b);
            meetings.add(meeting);
        }
        return meetings;
    }

    private ArrayList<GroupMeetingAdapter.Meeting> convertMeetingTimesInt(ArrayList<Integer> times, ArrayList<Integer> weightings) {
        ArrayList<GroupMeetingAdapter.Meeting> meetings = new ArrayList<>();
        for(int i = 0; i < times.size(); i++) {
            GroupMeetingAdapter.Meeting meeting = new GroupMeetingAdapter.Meeting(times.get(i), weightings.get(i));
            meetings.add(meeting);
        }
        return meetings;
    }

    private void loadMeetingListView(ArrayList<GroupMeetingAdapter.Meeting> meetings) {
        GroupMeetingAdapter adapter = new GroupMeetingAdapter(this, meetings);
        meetingsView.setAdapter(adapter);
    }

    public void returnToMenu(View view) {
        Intent intent = new Intent(GroupTimetableActivity.this, GroupProfileActivity.class);
        intent.putExtra("coordinates", coordinates);
        intent.putExtra("groupname", groupName);
        intent.putExtra("groupid", groupID);
        startActivity(intent);
    }

    public void timetableQuery(View view) {
        getGroupTimes().addOnCompleteListener(new OnCompleteListener<Map<String, Object>>() {
            @Override
            public void onComplete(@NonNull Task<Map<String, Object> > task) {
                if(task.isSuccessful()) {
                    Map<String, Object> data = task.getResult();
                    ArrayList<Integer> times = (ArrayList<Integer>) data.get("times");
                    ArrayList<Integer> weights = (ArrayList<Integer>) data.get("weights");

                    loadMeetingListView(convertMeetingTimesInt(times, weights));

                    //Log.i("get group times", "" +task.getResult().size());
                } else {
                    Log.e("get group times", task.getException().getMessage());
                }
            }
        });
    }


    private Task<Map<String, Object> > getGroupTimes() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("duration", 3);
        data.put("numTimes", 6);
        data.put("groupID", groupID);

        return mFunctions
                .getHttpsCallable("getGroupTimes")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }
}
