package sydney.edu.au.teammeet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final String TAG = "ChatAdapter";
    private Context mContext;
    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private ArrayList<User> mUsers = new ArrayList<>();
    FirebaseFirestore mDb;
    FirebaseUser fuser;


    public ChatAdapter(ArrayList<ChatMessage> messages, ArrayList<User> users, Context context){
        this.mContext = context;
        this.mUsers = users;
        this.mMessages = messages;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatAdapter.ViewHolder holder, final int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mDb = FirebaseFirestore.getInstance();
        ChatMessage chat = mMessages.get(position);
        User user = chat.getUser();
        String userId = chat.getUserId();
        holder.show_message.setText(chat.getMessage());
        if (!mMessages.get(position).getUser().getEmail().equals(fuser.getEmail())) {
            holder.user_name.setText(user.getUsername());
        }
        DocumentReference userDoc = mDb
                .collection("Users")
                .document(userId );
        userDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null) {
                    User user = documentSnapshot.toObject(User.class);
                    if (!mMessages.get(position).getUser().getEmail().equals(fuser.getEmail())) {
                        if(user.getStatus().equals("online")){
                        holder.img_online.setVisibility(View.VISIBLE);
                        holder.img_offline.setVisibility(View.GONE);
                        }else{
                            holder.img_online.setVisibility(View.GONE);
                            holder.img_offline.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        if(user.getPhoto() != null){
            Picasso.get()
                    .load(user.getPhoto())
                    .fit().centerCrop()
                    .placeholder(R.drawable.profile)
                    .into(holder.profile_image);
        }

        if (position == mMessages.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message, user_name;
        public ImageView profile_image, img_online, img_offline;
        public TextView txt_seen;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            user_name = itemView.findViewById(R.id.user_name);
            img_online = itemView.findViewById(R.id.img_online);
            img_offline = itemView.findViewById(R.id.img_offline);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessages.get(position).getUser().getEmail().equals(fuser.getEmail())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

}
