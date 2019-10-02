package sydney.edu.au.teammeet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
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

public class CoordinateFragment extends Fragment {
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    CollectionReference users;
    FirebaseUser currentUser;
    DocumentReference userDoc;

    public CoordinateFragment(){

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
                        HashMap<String, String> coordinateMap = user.getCoordinates() != null ? user.getCoordinates() : new HashMap<String, String>();
                        MyAdapter adapter = new MyAdapter(coordinateMap, true);
                        rv.setAdapter(adapter);

                        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                        rv.setLayoutManager(llm);
                    }
                });

        return rootView;
    }
}
