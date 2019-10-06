package sydney.edu.au.teammeet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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
    private ExtendedFloatingActionButton createGroupsBtn;
    private String currentUserID, userName, userEmail;
    private TextView pageName, leaveGroup;

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
        setUpGlobalNav(GroupsActivity.this, "Groups");

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        users = mFirestore.collection("Users");

        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();
        userDoc = users.document(currentUserID);


        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), GroupsActivity.this);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

 //This part of code moved to groupFragment and CoordinateFragment
//        coordinatedRecyclerView = (RecyclerView) findViewById(R.id.list_of_coord_groups);
//        memberRecyclerView = (RecyclerView) findViewById(R.id.list_of_member_groups);
//        coordinatedRecyclerView.setHasFixedSize(true);
//        memberRecyclerView.setHasFixedSize(true);
//
//        coordLayoutManager = new LinearLayoutManager(this);
//        coordinatedRecyclerView.setLayoutManager(coordLayoutManager);
//
//        memberLayoutManager = new LinearLayoutManager(this);
//        memberRecyclerView.setLayoutManager(memberLayoutManager);
//        leaveGroup = findViewById(R.id.link_leave);
//        leaveGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        createGroupsBtn = findViewById(R.id.create_new_group);
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
//            showCoordinatorGroups();
            new CoordinateFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CoordinateFragment();
//        showCoordinatorGroups();
    }


//    private void showMemberGroups() {
//        //fetch all groups (names) the user is currently in
//        userDoc.get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        user = documentSnapshot.toObject(User.class);
//                        userName = user.getUsername();
//                        userEmail = user.getEmail();
//
//                        DrawerUtil.getDrawer(GroupsActivity.this, toolbar, userName, userEmail);
//
//                        HashMap<String, String> map = user.getIsMemberOf() != null ? user.getIsMemberOf() : new HashMap<String, String>();
//
//                        memberAdapter = new MyAdapter(map, true);
//                        memberRecyclerView.setAdapter(memberAdapter);
//                    }
//                });
//    }
//
//
//    //there will be an equivalent for member groups when we distinguish further between coordinators and members
//    //PROBLEM IS THAT GROUPS MAY NOT BE LOADED IN ORDER OF CREATION DATE (????)
//    private void showCoordinatorGroups() {
//
//        //fetch all groups (names) the user is currently in
//        userDoc.get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        user = documentSnapshot.toObject(User.class);
//                        userName = user.getUsername();
//                        userEmail = user.getEmail();
//
//                        DrawerUtil.getDrawer(GroupsActivity.this, toolbar, userName, userEmail);
//
//                        HashMap<String, String> map = user.getCoordinates() != null ? user.getCoordinates() : new HashMap<String, String>();
//                        coordAdapter = new MyAdapter(map, true);
//                        coordinatedRecyclerView.setAdapter(coordAdapter);
//                    }
//                });
//    }
    class PagerAdapter extends FragmentPagerAdapter {

        Context context;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return 2;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab1);
                case 1:
                    return getString(R.string.tab2);
                }
                return null;
    }
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new CoordinateFragment();
                case 1:
                    return new GroupFragment();

            }

            return null;
        }

    }

}
