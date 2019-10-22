package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    private FirebaseUser userAuth;

    private DocumentReference groupRef;
    private Group groupSnapshot;
    private String groupID;
    private boolean coordinates;

    private ListView meetingsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_timetable);
        mFunctions = FirebaseFunctions.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userAuth = mAuth.getCurrentUser();

        meetingsView = findViewById(R.id.meeting_times_listview);

        groupID = getIntent().getStringExtra("groupID");
        groupRef = mFirestore.collection("Groups").document(groupID);

        timetableRefresh(null);

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

    //set up the list view with data and click listener
    private void loadMeetingListView(ArrayList<GroupMeetingAdapter.Meeting> meetings, final int duration) {
        final GroupMeetingAdapter adapter = new GroupMeetingAdapter(this, meetings, duration);
        meetingsView.setAdapter(adapter);

        Long meetingTime = groupSnapshot.getSelectedMeetingTime();
        if(meetingTime != null) { //some meeting times have not been selected
            adapter.selectMeetingTime((int) (long) meetingTime);
        }

        //coordinators can set meeting times
        if(coordinates) {
            meetingsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int position, long rowD) {
                    final GroupMeetingAdapter.Meeting meeting = (GroupMeetingAdapter.Meeting) adapterView.getItemAtPosition(position);
                    final String meetingTime = GroupMeetingAdapter.timeToString(meeting.time, duration);

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupTimetableActivity.this, R.style.MyDialog))
                    .setTitle("Set Meeting Time")
                    .setMessage("Do you want to set the meeting time for group " + groupSnapshot.getGroupName() + " to " + meetingTime + "?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    groupSnapshot.setSelectedMeetingTime((long) position);
                                    adapter.selectMeetingTime(position);

                                    groupRef.set(groupSnapshot).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            String message = "";
                                            if(task.isSuccessful()) {
                                                message = "meeting selection successful";
                                            } else {
                                                message = "database failed to update";
                                            }

                                            Toast.makeText(GroupTimetableActivity.this, message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Do nothing :/
                                }
                            });
                    builder.create().show();
                }
            });
        }
    }

    public void returnToMenu(View view) {
        Intent intent = new Intent(GroupTimetableActivity.this, GroupProfileActivity.class);
        intent.putExtra("coordinates", coordinates); //the GroupProfileActivity needs these values to load properly
        intent.putExtra("groupname", groupSnapshot.getGroupName());
        intent.putExtra("groupid", groupID);
        startActivity(intent);
    }

    //also called by the timetable refresh button
    public void timetableRefresh(View view) {
        groupRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot result = task.getResult();
                    groupSnapshot = result.toObject(Group.class);

                    ArrayList<String> coordinators = groupSnapshot.getCoordinators();
                    coordinates = coordinators.contains(userAuth.getUid());

                    Map<String, ArrayList<Long>> meetingTimeslots = groupSnapshot.getBestTimes();
                    Long duration = groupSnapshot.getMeetingDuration();

                    if(coordinates){ //coordinators can coordinate :0
                        Button queryButton = findViewById(R.id.query_timetable);
                        queryButton.setVisibility(View.VISIBLE);
                    }

                    if(duration == null) { //no coordinator has requested meeting times yet

                        String message = "";
                        if(coordinates) {
                            message = "Please generate meeting times for the group";
                        } else {
                            message = "Please ask a coordinator to generate meeting times";
                        }
                        Toast.makeText(GroupTimetableActivity.this, message, Toast.LENGTH_LONG).show();

                    } else {
                        loadMeetingListView(convertMeetingTimesLong(meetingTimeslots.get("times"), meetingTimeslots.get("weights")), (int)(long)duration);
                    }

                } else {
                    Log.e("group retrieval", groupID + ": " + task.getException().getMessage());
                }
            }
        });
    }

    //also called by the timetable query button
    //displays a dialog allowing user to query meeting parameters
    public void timetableQuery(View view) {


        final View editFields = LayoutInflater.from(GroupTimetableActivity.this).inflate(R.layout.query_parameters, null);
        final EditText editNumMeetings = editFields.findViewById(R.id.num_meeting_times);
        final EditText editMeetingDuration = editFields.findViewById(R.id.meeting_duration);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupTimetableActivity.this, R.style.MyDialog))
                .setView(editFields)
                .setTitle("Set query parameters")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        int temp = 0; //test for valid number of meetings
                        try {
                            temp = Integer.parseInt(editNumMeetings.getText().toString());

                            if(temp < 1) {
                                temp = 1;
                            }
                        } catch(NumberFormatException e) {
                            temp = 1;
                        }
                        final int numTimes = temp;

                         //test for valid duration
                        try {
                            temp = Integer.parseInt(editMeetingDuration.getText().toString());

                            if(temp < 1) {
                                temp = 1;
                            }
                        } catch(NumberFormatException e) {
                            temp = 1;
                        }
                        final int duration = temp;

                        //perform query on database
                        getGroupTimes(duration, numTimes).addOnCompleteListener(new OnCompleteListener<Map<String, Object>>() {
                            @Override
                            public void onComplete(@NonNull Task<Map<String, Object> > task) {
                                String message = "";
                                if(task.isSuccessful()) {
                                    message = "Query successful";
                                    timetableRefresh(null);

                                } else {
                                    message = task.getException().getMessage();
                                }

                                Toast.makeText(GroupTimetableActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing :/
                    }
                });
        builder.create().show();

    }


    private Task<Map<String, Object> > getGroupTimes(int duration, int numTimes) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("duration", duration);
        data.put("numTimes", numTimes);
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
