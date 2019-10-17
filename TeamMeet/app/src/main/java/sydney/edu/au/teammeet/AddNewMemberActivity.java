package sydney.edu.au.teammeet;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AddNewMemberActivity extends BaseActivity {

    private EditText txtMemberEmail;
    private Button backToGroupProfile;
    private Button addToGroup;
    private User user;
    private String currentUserID;
    DocumentReference userDoc;
    FirebaseUser currentUser;
    CollectionReference groups;
    CollectionReference users;
    private String groupDocId;

    private DocumentReference newMemberDoc;

    private String groupId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_member);
        setUpGlobalNav(AddNewMemberActivity.this, "Add Member");
        groupDocId = getIntent().getStringExtra("groupDocId");

        txtMemberEmail = (EditText) findViewById(R.id.new_member);
        backToGroupProfile = (Button) findViewById(R.id.back_to_group_profile);
        addToGroup = (Button) findViewById(R.id.addMemberBtn);

        //navigate back to the Groups page if they click on the Back Button
        backToGroupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        //save Group to database if they click on th other button
        addToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupDocId = getIntent().getStringExtra("groupDocId");
                updateGroupWithNewMember(groupDocId);
            }
        });
    }

    private DocumentReference GetUserDocByEmail() {
        CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
        users.whereEqualTo("email", txtMemberEmail.getText().toString().toLowerCase())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null || snapshots.size() == 0) {
                            Toast.makeText(AddNewMemberActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        } else {
                            for (DocumentChange userDoc : snapshots.getDocumentChanges()) {
                                User user = userDoc.getDocument().toObject(User.class);
                                if (user.getEmail() != null) {
                                    if (userDoc.getType() == DocumentChange.Type.ADDED
                                            || userDoc.getType() == DocumentChange.Type.MODIFIED) {
                                        newMemberDoc = userDoc.getDocument().getReference();
                                        System.out.println("========= user email docuemnt ID: " + userDoc.getDocument().getId());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
        return newMemberDoc;
    }
    private void updateGroupWithNewMember(String groupId){
        newMemberDoc = GetUserDocByEmail();
        final DocumentReference groupDoc = FirebaseFirestore.getInstance()
                                        .collection("Groups")
                                        .document(groupId);
        groupDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final Group group = documentSnapshot.toObject(Group.class);
                        //group.addMember(txtMemberEmail.getText().toString());

                        //TODO:添加Name
                        if (!group.getCoordinators().contains(newMemberDoc.getId())
                                && !group.getMembers().contains((newMemberDoc.getId())))
                        {
                            group.addMember(newMemberDoc.getId().toString());

                            showSnackbar("Member has been added successfully", AddNewMemberActivity.this);

                            //add user details to the database
                            groupDoc.set(group).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                            newMemberDoc.get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot memberDocumentSnapshot) {
                                            User newMemberUser = memberDocumentSnapshot.toObject(User.class);
                                            assert newMemberUser != null;

                                            newMemberUser.addToMemberOf(groupDoc.getId(), group.getGroupName());


                                            // add to the database
                                            newMemberDoc.set(newMemberUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Intent intent = new Intent();
                                                    setResult(RESULT_OK, intent);
                                                    finish();
                                                }
                                            });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showSnackbar("Error in add new member", AddNewMemberActivity.this);
                                }
                            });

                        }else {
                            Toast.makeText(AddNewMemberActivity.this, "User is already in the group", Toast.LENGTH_SHORT).show();
                        }
                    }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showSnackbar("Error in add new member", AddNewMemberActivity.this);
            }
        });
    }


    public static class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //getting the remote input bundle from intent
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

            //if there is some input
            if (remoteInput != null) {

                //getting the input value
                CharSequence name = remoteInput.getCharSequence(MainActivity.NOTIFICATION_REPLY);

                //updating the notification with the input value
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, MainActivity.CHANNNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_menu_info_details)
                        .setContentTitle("Hey Thanks, " + name);
                NotificationManager notificationManager = (NotificationManager) context.
                        getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(MainActivity.NOTIFICATION_ID, mBuilder.build());
            }

            //if help button is clicked
            if (intent.getIntExtra(MainActivity.KEY_INTENT_ACCEPT, -1) == MainActivity.REQUEST_CODE_ACCEPT) {
                Toast.makeText(context, "You Clicked ACCEPT", Toast.LENGTH_LONG).show();
            }

            //if more button is clicked
            if (intent.getIntExtra(MainActivity.KEY_INTENT_DECLINE, -1) == MainActivity.REQUEST_CODE_DECLINE) {
                Toast.makeText(context, "You Clicked DECLINE", Toast.LENGTH_LONG).show();
            }
        }
    }
}


