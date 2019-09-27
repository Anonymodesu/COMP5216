package sydney.edu.au.teammeet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class CreateNewGroupActivity extends BaseActivity {

    private EditText editText;
    private Button backToGroups;
    private Button addToDatabase;
    private User user;
    private String currentUserID;
    DocumentReference userDoc;
    FirebaseUser currentUser;
    CollectionReference groups;
    CollectionReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        editText = (EditText) findViewById(R.id.new_group);
        backToGroups = (Button) findViewById(R.id.back_to_groups);
        addToDatabase = (Button) findViewById(R.id.add_to_db);

        //navigate back to the Groups page if they click on the Back Button
        backToGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        //save Group to database if they click on th other button
        addToDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int textLength = editText.getText().toString().trim().length();

                if (textLength == 0 || textLength > 32) {
                    editText.setError("The group name must contain between 1 and 32 characters.");
                } else {

                    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    currentUser = mAuth.getCurrentUser();
                    assert currentUser != null;
                    currentUserID = currentUser.getUid();

                    groups = mFirestore.collection("Groups");
                    users = mFirestore.collection("Users");
                    userDoc = users.document(currentUserID);

                    //fetch details of the user who is currently logged in
                    userDoc.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    user = documentSnapshot.toObject(User.class);
                                    assert user != null;

                                    //create new Group object, add current user as coordinator
                                    ArrayList<String> coordinators = new ArrayList<String>();
                                    ArrayList<String> members = new ArrayList<String>();
                                    coordinators.add(currentUserID);
                                    Group group = new Group(coordinators, members, editText.getText().toString());
                                    groups.add(group)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    //add group details to user's coordinates attribute
                                                    user.addToCoordinates(documentReference.getId(), editText.getText().toString());
                                                    showSnackbar("Group has been made successfully", CreateNewGroupActivity.this);

                                                    //add user details to the database
                                                    userDoc.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Intent intent = new Intent();
                                                            setResult(RESULT_OK, intent);
                                                            finish();
                                                        }
                                                    });
                                                }
                                            });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showSnackbar("Error in making new group", CreateNewGroupActivity.this);
                        }
                    });
                }
            }
        });
    }
}


