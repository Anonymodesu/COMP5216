package sydney.edu.au.teammeet;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        recyclerView = (RecyclerView) findViewById(R.id.list_of_groups);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        //final CollectionReference users = mFirestore.collection("Users");

        String[] strings = {"Hello", "This is a test"};
        mAdapter = new MyAdapter(strings);

        recyclerView.setAdapter(mAdapter);
    }
}
