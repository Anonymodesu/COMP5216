package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sydney.edu.au.teammeet.Notifications.APIService;
import sydney.edu.au.teammeet.Notifications.Client;
import sydney.edu.au.teammeet.Notifications.Data;
import sydney.edu.au.teammeet.Notifications.MyResponse;
import sydney.edu.au.teammeet.Notifications.Sender;

public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";

    ImageButton btn_send;
    EditText txt_send;
    RecyclerView userView;
    TextView room_name;
    String groupId, groupname;
    FirebaseFirestore mDb;
    FirebaseUser fuser;
    ChatAdapter mChatMessageRecyclerAdapter;
    APIService apiService;
    ListenerRegistration mChatMessageEventListener;
    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        btn_send = findViewById(R.id.btn_send);
        txt_send = findViewById(R.id.txt_send);
        userView = findViewById(R.id.users_view);
        room_name = findViewById(R.id.room_name);
        groupname = getIntent().getStringExtra("groupName");
        groupId = getIntent().getStringExtra("groupId");
        setUpGlobalNav(ChatActivity.this, "Chat Room");
        room_name.setText(groupname);

        mDb = FirebaseFirestore.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();


        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = txt_send.getText().toString();
                if (msg.equals("")) {
                    showSnackbar("You can't send empty messages", ChatActivity.this);
                } else {
                    sendMessage(msg);
                    sendNotifications(msg);
                }
                txt_send.setText("");
            }
        });
        getChatMessages();
        initChatroomRecyclerView();
    }

    private void sendMessage(final String msg) {

        DocumentReference newMessageDoc = mDb
                .collection("Groups")
                .document(groupId)
                .collection("chat messages")
                .document();

        ChatMessage newChatMessage = new ChatMessage();
        newChatMessage.setMessage(msg);
        newChatMessage.setMessage_id(newMessageDoc.getId());

        User user = ((UserClient)(getApplicationContext())).getUser();
        newChatMessage.setUser(user);
        newChatMessage.setUserId(fuser.getUid());

        newMessageDoc.set(newChatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    txt_send.setText("");
                }else{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void sendNotifications(final String message) {
        DocumentReference groupRef = mDb
                .collection("Groups")
                .document(groupId);
        groupRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    Group group = documentSnapshot.toObject(Group.class);
                    for (String userId : group.getCoordinators()) {
                        if (userId != fuser.getUid()) {
                            Log.e(TAG, userId);
                            sendSingleNotification(userId, message);
                        }
                    }
                    for (String userId : group.getMembers()) {
                        if (userId != fuser.getUid()) {
                            Log.e(TAG, userId);
                            sendSingleNotification(userId, message);
                        }
                    }
                }
            }
        });
    }
    public void sendSingleNotification(final String userId, final String message){
        DocumentReference userRef = mDb
                .collection("Users")
                .document(userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot!=null){
                    User user = documentSnapshot.toObject(User.class);
                    Log.e(TAG, "notify users"+user.getUsername());
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher,user.getUsername()+" : "+message, groupname, userId, groupId, groupname);
                    Sender sender = new Sender(data, user.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            showSnackbar("Send notification failed!", ChatActivity.this);
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }
        });
        }


    private void getChatMessages(){
        CollectionReference messagesRef = mDb
                .collection("Groups")
                .document(groupId)
                .collection("chat messages");

        mChatMessageEventListener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                ChatMessage message = doc.toObject(ChatMessage.class);
                                if(!mMessageIds.contains(message.getMessage_id())){
                                    mMessageIds.add(message.getMessage_id());
                                    mMessages.add(message);

                                    userView.smoothScrollToPosition(mMessages.size() - 1);
                                }

                            }
                            mChatMessageRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }
    private void initChatroomRecyclerView(){
        mChatMessageRecyclerAdapter = new ChatAdapter(mMessages, new ArrayList<User>(), this);
        userView.setAdapter(mChatMessageRecyclerAdapter);
        userView.setLayoutManager(new LinearLayoutManager(this));

        userView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    userView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mMessages.size() > 0){
                                userView.smoothScrollToPosition(
                                        userView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }

}