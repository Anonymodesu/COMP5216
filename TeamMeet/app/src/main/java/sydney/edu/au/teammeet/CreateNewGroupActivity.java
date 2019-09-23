package sydney.edu.au.teammeet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class CreateNewGroupActivity extends BaseActivity {

    private EditText editText;
    private Button backToGroups;
    private Button addToDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        editText = (EditText) findViewById(R.id.new_group);
        backToGroups = (Button) findViewById(R.id.back_to_groups);
        addToDatabase = (Button) findViewById(R.id.add_to_db);

        backToGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        addToDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                final CollectionReference groups = mFirestore.collection("Groups");
                //final CollectionReference users = mFirestore.collection("Users");

                ArrayList<String> coordinators = new ArrayList<String>();
                ArrayList<String> members = new ArrayList<String>();
                coordinators.add(mAuth.getCurrentUser().getUid());
                Group group = new Group(coordinators, members, editText.getText().toString());
                groups.add(group);

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
