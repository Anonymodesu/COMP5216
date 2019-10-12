package sydney.edu.au.teammeet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupProfileActivity extends BaseActivity {

    private Toolbar toolbar;
    private RecyclerView userRecyclerView;
    //private RecyclerView memberRecyclerView;
    private RecyclerView.LayoutManager userLayoutManager;
    //private RecyclerView.LayoutManager memberLayoutManager;

    private Button addMemberBtn;
    private String currentUserID, userName, userEmail;
    private String memberEmail;
    private User user;
    private TextView groupName;
    private sydney.edu.au.teammeet.UserViewAdapter userAdapter;
    private GroupProfilerAdapter groupProfilerAdapter;

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    CollectionReference users;
    CollectionReference groups;
    FirebaseUser currentUser;
    DocumentReference userDoc;


    DocumentReference groupDoc;
    // private String documentId = "z5N4PPcb2UCm6zuVDZBG";
    private Group group;
    private final int ADD_MEMBER = 205;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_group);
        //set up global nav drawer
        setUpGlobalNav(GroupProfileActivity.this,"Group Profile");

        groupName = (TextView) findViewById(R.id.group_name);

        String groupname = getIntent().getStringExtra("groupname");
        final String groupID = getIntent().getStringExtra("groupid");
        groupName.setText(groupname);
        saveGroupId();

        userRecyclerView = (RecyclerView) findViewById(R.id.list_of_users);
        userRecyclerView.setHasFixedSize(true);

        userLayoutManager = new LinearLayoutManager(this);
        userRecyclerView.setLayoutManager(userLayoutManager);

        //set up global nav drawer
        setSupportActionBar(toolbar);

        //fetch details of the user who is currently logged in
        mFirestore = FirebaseFirestore.getInstance();
        groups = mFirestore.collection("Groups");
        groupDoc = groups.document(groupID);
        users = mFirestore.collection("Users");

        boolean coordinates = getIntent().getBooleanExtra("coordinates", false);
        showUsers();

        addMemberBtn = (Button) findViewById(R.id.add_new_member);
        if (!coordinates) {
            addMemberBtn.setVisibility(View.GONE);
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

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(userRecyclerView);

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
        //showUsers();
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
                        //userIDs.addAll(group.getCoordinators());
                        userIDs.addAll(group.getMembers());

                       for (String user: userIDs) {

                           //final DocumentReference userDoc = users.document(memberId);
                           final DocumentReference userDoc = mFirestore.collection("Users").document(user);
                           userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                               @Override
                               public void onSuccess(DocumentSnapshot documentSnapshot) {
                                   User user = documentSnapshot.toObject(User.class);
                                   userName = user.getUsername();
                                   String userId = userDoc.getId();

                                   usernames.add(userName);
                                   groupMemberIDs.add(userId);
//                                 Log.e("TAGGING THIS SHIT", "The size is " + usernames.size());

                                   if (usernames.size() == userIDs.size()) {

                                       //userAdapter = new sydney.edu.au.teammeet.UserViewAdapter(usernames.toArray(new String[usernames.size()]),groupMember, GroupProfileActivity.this);
                                       userAdapter = new UserViewAdapter(usernames, groupMemberIDs, GroupProfileActivity.this);
                                       userRecyclerView.setAdapter(userAdapter);
                                       }
                                   }
                               });
                       }
                    }
                });
    }

    //save 'groupID' to SharePreference
   public void saveGroupId(){
       final String groupID = getIntent().getStringExtra("groupid");
       SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
       SharedPreferences.Editor editor = mPref.edit();
       editor.putString("groupId", groupID);
       editor.commit();
   }

    //deletes a group by swiping left
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
    {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            position = viewHolder.getAdapterPosition();
            userAdapter.deleteGroupMember(GroupProfileActivity.this, position);
            //showUsers();

        }

    };


}
