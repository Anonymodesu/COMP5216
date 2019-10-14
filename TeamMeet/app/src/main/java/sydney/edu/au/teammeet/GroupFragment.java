package sydney.edu.au.teammeet;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class GroupFragment extends Fragment {
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseUser currentUser;
    DocumentReference userDoc;

    public GroupFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.group_fragment, container, false);

        final RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.list_of_member_groups);
        rv.setHasFixedSize(true);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        users = mFirestore.collection("Users");

        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        String currentUserID = currentUser.getUid();
        userDoc = users.document(currentUserID);
        userDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        HashMap<String, String> map = user.getIsMemberOf() != null ? user.getIsMemberOf() : new HashMap<String, String>();
                        MyAdapter adapter = new MyAdapter(map, false);
                        rv.setAdapter(adapter);

                        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                        rv.setLayoutManager(llm);
                    }
                });

        return rootView;
    }

}
