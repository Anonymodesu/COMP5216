package sydney.edu.au.teammeet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

public class GroupsActivity extends BaseActivity {

    private Toolbar toolbar;
    private RecyclerView coordinatedRecyclerView;
    private RecyclerView memberRecyclerView;
    private RecyclerView.LayoutManager coordLayoutManager;
    private RecyclerView.LayoutManager memberLayoutManager;
    private MyAdapter coordAdapter;
    private MyAdapter memberAdapter;
    private Button createGroupsBtn;
    private String currentUserID, userName, userEmail;
    private User user;

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseUser currentUser;
    DocumentReference userDoc;

    private final int CREATE_GROUP = 204;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        coordinatedRecyclerView = (RecyclerView) findViewById(R.id.list_of_coord_groups);
        memberRecyclerView = (RecyclerView) findViewById(R.id.list_of_member_groups);
        coordinatedRecyclerView.setHasFixedSize(true);
        memberRecyclerView.setHasFixedSize(true);

        coordLayoutManager = new LinearLayoutManager(this);
        coordinatedRecyclerView.setLayoutManager(coordLayoutManager);

        memberLayoutManager = new LinearLayoutManager(this);
        memberRecyclerView.setLayoutManager(memberLayoutManager);

        //set up global nav drawer
        setSupportActionBar(toolbar);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        users = mFirestore.collection("Users");

        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();
        userDoc = users.document(currentUserID);

        showMemberGroups();
        showCoordinatorGroups();

        createGroupsBtn = (Button) findViewById(R.id.create_new_group);
        createGroupsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupsActivity.this, CreateNewGroupActivity.class);
                startActivityForResult(intent, CREATE_GROUP);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            showCoordinatorGroups();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCoordinatorGroups();
    }


    private void showMemberGroups() {
        //fetch all groups (names) the user is currently in
        userDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user = documentSnapshot.toObject(User.class);
                        userName = user.getUsername();
                        userEmail = user.getEmail();

                        DrawerUtil.getDrawer(GroupsActivity.this, toolbar, userName, userEmail);

                        HashMap<String, String> map = user.getIsMemberOf() != null ? user.getIsMemberOf() : new HashMap<String, String>();

                        memberAdapter = new MyAdapter(map, true);
                        memberRecyclerView.setAdapter(memberAdapter);
                    }
                });
    }


    //there will be an equivalent for member groups when we distinguish further between coordinators and members
    //PROBLEM IS THAT GROUPS MAY NOT BE LOADED IN ORDER OF CREATION DATE (????)
    private void showCoordinatorGroups() {

        //fetch all groups (names) the user is currently in
        userDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user = documentSnapshot.toObject(User.class);
                        userName = user.getUsername();
                        userEmail = user.getEmail();

                        DrawerUtil.getDrawer(GroupsActivity.this, toolbar, userName, userEmail);

                        HashMap<String, String> map = user.getCoordinates() != null ? user.getCoordinates() : new HashMap<String, String>();

                        coordAdapter = new MyAdapter(map, true);
                        coordinatedRecyclerView.setAdapter(coordAdapter);
                    }
                });
    }
}
