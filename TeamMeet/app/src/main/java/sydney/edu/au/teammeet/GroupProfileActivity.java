package sydney.edu.au.teammeet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupProfileActivity extends BaseActivity {

    private Toolbar toolbar;
    private RecyclerView userRecyclerView;
    //private RecyclerView memberRecyclerView;
    private RecyclerView.LayoutManager userLayoutManager;
    //private RecyclerView.LayoutManager memberLayoutManager;

    private Button addMemberBtn;
    private String currentUserID, userName, userEmail;
    private User user;
    private TextView groupName;
    private sydney.edu.au.teammeet.UserViewAdapter userAdapter;

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    CollectionReference users;
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

        groupName = (TextView) findViewById(R.id.group_name);

        String groupname = getIntent().getStringExtra("groupname");
        final String groupID = getIntent().getStringExtra("groupid");
        groupName.setText(groupname);

        userRecyclerView = (RecyclerView) findViewById(R.id.list_of_users);
        userRecyclerView.setHasFixedSize(true);

        userLayoutManager = new LinearLayoutManager(this);
        userRecyclerView.setLayoutManager(userLayoutManager);


        //set up global nav drawer
        setSupportActionBar(toolbar);

        mFirestore = FirebaseFirestore.getInstance();
        CollectionReference groups = mFirestore.collection("Groups");
        groupDoc = groups.document(groupID);

        //fetch details of the user who is currently logged in



        /*
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        users = mFirestore.collection("Users");

        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();
        userDoc = users.document(currentUserID);
        */

        /*
        String[] moreStrings = {"What", "How we doin"};
        memberAdapter = new MyAdapter(moreStrings);

        memberRecyclerView.setAdapter(memberAdapter);
        */

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

                        ArrayList<String> users = new ArrayList<String>();
                        users.addAll(group.getCoordinators());
                        users.addAll(group.getMembers());

                        userAdapter = new sydney.edu.au.teammeet.UserViewAdapter(users.toArray(new String[users.size()]));
                        userRecyclerView.setAdapter(userAdapter);
                    }
                });
    }

}
