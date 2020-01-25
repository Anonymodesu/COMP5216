package sydney.edu.au.teammeet;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupProfileActivity extends BaseActivity implements OnItemClicked {

    private Toolbar toolbar;
    //private RecyclerView userRecyclerView
    private RecyclerView coordinatorRecycleView, memberRecycleView;
    //private RecyclerView.LayoutManager userLayoutManager;
    private RecyclerView.LayoutManager memberLayoutManager;
    //private LinearLayoutManager coordinatorLayoutManager;
    //private RecyclerView.LayoutManager memberLayoutManager;

    private Button addMemberBtn;
    private String currentUserID, userName, userEmail, userProfile;
    private String memberEmail;
    private User user;
    private String groupID;
    private TextView groupName;
    //private sydney.edu.au.teammeet.UserViewAdapter userAdapter;
    private sydney.edu.au.teammeet.GroupProfilerCoordinatorAdapter coordinatorAdapter;
    private sydney.edu.au.teammeet.GroupProfileMemberAdapter membersAdapter;
    //private GroupProfilerCoordinatorAdapter groupProfilerAdapter;

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    CollectionReference users;
    CollectionReference groups;
    FirebaseUser currentUser;
    DocumentReference userDoc;


    private boolean coordinates;
    DocumentReference groupDoc;

    // private String documentId = "z5N4PPcb2UCm6zuVDZBG";
    private Group group;
    private final int ADD_MEMBER = 205;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_group);

        //set up global nav drawer
        setUpGlobalNav(GroupProfileActivity.this, "Group Profile");

        groupName = (TextView) findViewById(R.id.group_name);

        String groupname = getIntent().getStringExtra("groupname");
        groupID = getIntent().getStringExtra("groupid");
        groupName.setText(groupname);
        saveGroupId();

        //userRecyclerView = (RecyclerView) findViewById(R.id.list_of_users);
        coordinatorRecycleView = (RecyclerView) findViewById(R.id.list_of_coordinator);
        memberRecycleView = (RecyclerView) findViewById(R.id.list_of_members);
        //userRecyclerView.setHasFixedSize(true);
        coordinatorRecycleView.setHasFixedSize(true);
        memberRecycleView.setHasFixedSize(true);

        //userLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager coordinatorLayoutManager = new LinearLayoutManager(this);
        memberLayoutManager = new LinearLayoutManager(this);
        coordinatorLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        //userRecyclerView.setLayoutManager(userLayoutManager);
        coordinatorRecycleView.setLayoutManager(coordinatorLayoutManager);
        memberRecycleView.setLayoutManager(memberLayoutManager);

        //set up global nav drawer
        setSupportActionBar(toolbar);

        //fetch details of the user who is currently logged in
        mFirestore = FirebaseFirestore.getInstance();
        groups = mFirestore.collection("Groups");
        groupDoc = groups.document(groupID);
        users = mFirestore.collection("Users");

        coordinates = getIntent().getBooleanExtra("coordinates", false);
        showUsers();

        addMemberBtn = (Button) findViewById(R.id.add_new_member);
        if (!coordinates) {
            addMemberBtn.setVisibility(View.INVISIBLE);
        } else {

            addMemberBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(GroupProfileActivity.this, AddNewMemberActivity.class);
                    intent.putExtra("groupDocId", groupID);
                    startActivityForResult(intent, ADD_MEMBER);
                }
            });
        }

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(memberRecycleView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            showUsers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUsers();
    }

    private void showUsers() {
        groupDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final Group group = documentSnapshot.toObject(Group.class);
                        assert group != null;

                        final ArrayList<String> userIDs = new ArrayList<String>();
                        final ArrayList<String> usernames = new ArrayList<String>();
                        final ArrayList<String> groupMemberIDs = new ArrayList<String>();
                        final ArrayList<String> coordinatorIDs = new ArrayList<String>();
                        final ArrayList<String> coordinatorName = new ArrayList<String>();
                        final ArrayList<String> userPhotos = new ArrayList<String>();
                        final ArrayList<String> coordPhotos = new ArrayList<String>();

                        coordinatorIDs.addAll(group.getCoordinators());
                        userIDs.addAll(group.getMembers());

                        for (String user : userIDs) {

                            //final DocumentReference userDoc = users.document(memberId);
                            final DocumentReference userDoc = mFirestore.collection("Users").document(user);
                            userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = documentSnapshot.toObject(User.class);
                                    userName = user.getUsername();
                                    userProfile = user.getPhoto();
                                    String userId = userDoc.getId();

                                    usernames.add(userName);
                                    groupMemberIDs.add(userId);
                                    userPhotos.add(userProfile);
//                                 Log.e("TAGGING THIS SHIT", "The size is " + usernames.size());

                                    if (usernames.size() == userIDs.size()) {

                                        //userAdapter = new sydney.edu.au.teammeet.UserViewAdapter(usernames.toArray(new String[usernames.size()]),groupMember, GroupProfileActivity.this);
                                        membersAdapter = new GroupProfileMemberAdapter(usernames, groupMemberIDs, userPhotos, GroupProfileActivity.this);
                                        membersAdapter.setOnClick(GroupProfileActivity.this);
                                        memberRecycleView.setAdapter(membersAdapter);
                                    }
                                }
                            });
                        }

                        //TODO: 遍历循环group里的coordinator
                        for (String coordinator : coordinatorIDs) {

                            final DocumentReference coordinatorDoc = mFirestore.collection("Users").document(coordinator);
                            coordinatorDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = documentSnapshot.toObject(User.class);
                                    userName = user.getUsername();
                                    userProfile = user.getPhoto();
                                    coordinatorName.add(userName);
                                    coordPhotos.add(userProfile);

                                    if (coordinatorName.size() == coordinatorIDs.size()) {
                                        coordinatorAdapter = new GroupProfilerCoordinatorAdapter(coordinatorName, coordPhotos, GroupProfileActivity.this);
                                        coordinatorRecycleView.setAdapter(coordinatorAdapter);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    //save 'groupID' to SharePreference
    public void saveGroupId() {
        final String groupID = getIntent().getStringExtra("groupid");
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("groupId", groupID);
        editor.commit();
    }

    //retrieve groupId from sharepreference
    public String getGroupId(){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        String groupId = mPref.getString("groupId", "" );
        return groupId;
    }

    //retrieve deleted memberName from sharepreference
    public String getDeletedMemberName() {

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        String deletedMemberName = mPref.getString("deletedMemberName", "");
        return deletedMemberName;
    }

    //retrieve deleted memberId from sharepreference
    public String getDeletedMemberId(){

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        String deletedMemberId = mPref.getString("deletedMemberId", "" );
        return deletedMemberId;
    }

    //deletes a group by swiping left
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            if (coordinates) {
                membersAdapter.deleteGroupMember(GroupProfileActivity.this, position);
                //membersAdapter.removeDeletedMember(position);
                //showUsers();

                final DocumentReference groupRef = mFirestore.collection("Groups").document(getGroupId());
                groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group = documentSnapshot.toObject(Group.class);
                        ArrayList<String> memberList = group.getMembers();

                        if(!memberList.contains(getDeletedMemberName())){
                            Snackbar.make(memberRecycleView, getDeletedMemberName(), Snackbar.LENGTH_LONG)
                                    .setAction("Undo", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            membersAdapter.insertDeletedMemberToTheGroup(getGroupId(), position);

                                            /*Intent intent = new Intent(GroupProfileActivity.this,
                                                    GroupProfileActivity.class);
                                            intent.putExtra("groupname", groupName.getText());
                                            intent.putExtra("groupid", groupID);
                                            intent.putExtra("coordinates", coordinates);
                                            startActivity(intent);*/
                                        }
                                    })
                                    .show();
                        }
                    }
                });

            }

        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0 | 0;
            int swipeFlags = -1;

            if (coordinates) {
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            } else {
                swipeFlags = 0 | 0;
            }
            return makeMovementFlags(dragFlags, swipeFlags);
        }

    };

    public void gotoGroupMeetingTimes(View view) {
        Intent intent = new Intent(GroupProfileActivity.this, GroupTimetableActivity.class);
        intent.putExtra("groupID", groupID);
        startActivity(intent);
    }

    @Override
    public void onItemClick(final int position) {
        if (coordinates) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Make " + membersAdapter.getMemberNameList(position) + " a coordinator?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    final DocumentReference groupDoc = mFirestore.collection("Groups").document(groupID);
                    groupDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Group group = documentSnapshot.toObject(Group.class);
                            group.removeMember(membersAdapter.getMemberList(position));
                            boolean addedCoordinator = group.addCoordinator(membersAdapter.getMemberList(position));
                            if (addedCoordinator) {

                                groupDoc.set(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        final DocumentReference userDoc = mFirestore.collection("Users").document(membersAdapter.getMemberList(position));
                                        userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                User user = documentSnapshot.toObject(User.class);

                                                user.removeFromMembers(groupID);
                                                boolean updatedUser = user.addToCoordinates(groupID, groupName.getText().toString());

                                                if (updatedUser) {
                                                    userDoc.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            String savedName = membersAdapter.getMemberNameList(position);
                                                            membersAdapter.deleteMemberFromList(GroupProfileActivity.this, position);
                                                            coordinatorAdapter.addMemberToAdapter(GroupProfileActivity.this, savedName);
                                                            Toast.makeText(GroupProfileActivity.this, "Success!", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(GroupProfileActivity.this, membersAdapter.getMemberNameList(position) + " is already a coordinator!", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                });

                            } else {
                                Toast.makeText(GroupProfileActivity.this, membersAdapter.getMemberNameList(position) + " is already a coordinator!", Toast.LENGTH_LONG).show();
                            }
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

        } else {

        }
    }
}

