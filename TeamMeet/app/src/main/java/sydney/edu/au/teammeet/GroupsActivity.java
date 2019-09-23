package sydney.edu.au.teammeet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupsActivity extends BaseActivity {

    private RecyclerView coordinatedRecyclerView;
    private RecyclerView memberRecyclerView;
    private RecyclerView.LayoutManager coordLayoutManager;
    private RecyclerView.LayoutManager memberLayoutManager;
    private MyAdapter mAdapter;
    private MyAdapter moreAdapter;
    private Button createGroupsBtn;

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

        //FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        //final CollectionReference users = mFirestore.collection("Users");

        String[] strings = {"Hello", "This is a test"};
        mAdapter = new MyAdapter(strings);

        coordinatedRecyclerView.setAdapter(mAdapter);

        String[] moreStrings = {"What", "How we doin"};
        moreAdapter = new MyAdapter(moreStrings);

        memberRecyclerView.setAdapter(moreAdapter);

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

    }
}
