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

import java.util.HashMap;

public class GroupProfileActivity extends BaseActivity {

    private Toolbar toolbar;
    private RecyclerView coordinatedRecyclerView;
    private RecyclerView memberRecyclerView;
    private RecyclerView.LayoutManager coordLayoutManager;
    private RecyclerView.LayoutManager memberLayoutManager;
    private MyAdapter coordAdapter;
    private MyAdapter memberAdapter;
    private Button addMemberBtn;
    private String currentUserID, userName, userEmail;
    private User user;
    private TextView groupName;

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseUser currentUser;
    DocumentReference userDoc;

    DocumentReference groupDoc;
    private String documentId = "z5N4PPcb2UCm6zuVDZBG";
    private Group group;
    private final int ADD_MEMBER = 205;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_group);

        groupName = (TextView) findViewById(R.id.group_name);


        //coordinatedRecyclerView = (RecyclerView) findViewById(R.id.list_of_coord_groups);
        //memberRecyclerView = (RecyclerView) findViewById(R.id.list_of_member_groups);
        //coordinatedRecyclerView.setHasFixedSize(true);
        //memberRecyclerView.setHasFixedSize(true);

//        coordLayoutManager = new LinearLayoutManager(this);
//        coordinatedRecyclerView.setLayoutManager(coordLayoutManager);
//
//        memberLayoutManager = new LinearLayoutManager(this);
//        memberRecyclerView.setLayoutManager(memberLayoutManager);

        //set up global nav drawer
        setSupportActionBar(toolbar);

        //groupName.setText("team");

        mFirestore = FirebaseFirestore.getInstance();
        CollectionReference groups = mFirestore.collection("Groups");
        groupDoc = groups.document(documentId);



        /*
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        users = mFirestore.collection("Users");

        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();
        userDoc = users.document(currentUserID);
        */
        showGroupProfile();


        /*
        String[] moreStrings = {"What", "How we doin"};
        memberAdapter = new MyAdapter(moreStrings);

        memberRecyclerView.setAdapter(memberAdapter);
        */
        addMemberBtn = (Button) findViewById(R.id.add_new_member);
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupProfileActivity.this, AddNewMemberActivity.class);
                intent.putExtra("groupDocId", documentId);
                startActivityForResult(intent, ADD_MEMBER);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {

            //showGroupProfile();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //showGroupProfile();
    }

    //there will be an equivalent for member groups when we distinguish further between coordinators and members
    //PROBLEM IS THAT GROUPS MAY NOT BE LOADED IN ORDER OF CREATION DATE (????)
    private void showGroupProfile() {

        //fetch all groups (names) the user is currently in
        groupDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        group = documentSnapshot.toObject(Group.class);
                        assert group != null;

                        groupName.setText(group.getGroupName());

                        /*
                        user = documentSnapshot.toObject(User.class);
                        userName = user.getUsername();
                        userEmail = user.getEmail();

                        DrawerUtil.getDrawer(GroupProfileActivity.this, toolbar, userName, userEmail);

                        String[] strings = {};
                        HashMap<String, String> map = user.getCoordinates();

                        if (map != null) {
                            strings = map.values().toArray(new String[map.size()]);
                        }
                        */
                        //coordAdapter = new MyAdapter(strings);
                        //coordinatedRecyclerView.setAdapter(coordAdapter);



                    }
                });
    }
}
