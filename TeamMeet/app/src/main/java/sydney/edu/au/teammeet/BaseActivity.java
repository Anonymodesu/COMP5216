package sydney.edu.au.teammeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class BaseActivity extends AppCompatActivity {

    private static final int HOME_ID = 1;
    private static final int PROFILE_ID = 2;
    private static final int TIMETABLE_ID = 3;
    private static final int SIGNOUT_ID = 4;
    private static final int GROUP_ID = 5;

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    //hide soft keyboard when clicking on places other than edit text and buttons
    public void setupUI(View view, final Activity activity) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public void setUpGlobalNav(final Activity activity, String pagename){

        Toolbar toolbar = findViewById(R.id.global_header);

        if(!(activity instanceof LoginActivity)&&!(activity instanceof RegisterActivity)&&!(activity instanceof ChangePasswordActivity)){
                User user =  ((UserClient)(getApplicationContext())).getUser();
                //set up global nav drawer
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                //set up page name
                TextView pageName = findViewById(R.id.page_name);
                pageName.setText(pagename);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setIcon(R.drawable.logo);
                if(user!=null) {
                    DrawerUtil.getDrawer(this, toolbar, user.getUsername(), user.getEmail());
                }else{
                    DrawerUtil.getDrawer(this, toolbar, "", "");
                }

            //[END_of setup page header and navigation]
        }else{
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //set up page name
            TextView pageName = findViewById(R.id.page_name);
            pageName.setText(pagename);
            getSupportActionBar().setIcon(R.drawable.logo);
        }

    }

    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();
        /*
         * If no view is focused, an NPE will be thrown
         *
         * Maxim Dmitriev
         */
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    protected void showSnackbar(@NonNull String message, Activity activity) {
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    public static class DrawerUtil {
        public static void getDrawer(final Activity activity, Toolbar toolbar, String username, String email) {
            //if you want to update the items at a later time it is recommended to keep it in a variable
            ProfileDrawerItem profileItem;
            User user =  ((UserClient)(activity.getApplicationContext())).getUser();
            if(user!=null&&user.getPhoto()!=null) {
                //initialize and create the image loader logic
                DrawerImageLoader.init(new AbstractDrawerImageLoader() {
                    @Override
                    public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                        Picasso.get().load(uri).fit().centerCrop().placeholder(placeholder).into(imageView);
                    }

                    @Override
                    public void cancel(ImageView imageView) {
                        Picasso.get().cancelRequest(imageView);
                    }
                });
                profileItem = new ProfileDrawerItem().withName(username).withEmail(email).withIcon(user.getPhoto());
            }else{
                profileItem = new ProfileDrawerItem().withName(username).withEmail(email).withIcon(R.drawable.profile);
            }


            PrimaryDrawerItem drawerItemHome = new PrimaryDrawerItem().withIdentifier(HOME_ID)
                    .withName(R.string.home).withIcon(R.drawable.home);
            PrimaryDrawerItem drawerItemProfile = new PrimaryDrawerItem().withIdentifier(PROFILE_ID)
                    .withName(R.string.profile).withIcon(R.drawable.ic_person_grey_24dp);
            PrimaryDrawerItem drawerItemTimeTable = new PrimaryDrawerItem().withIdentifier(TIMETABLE_ID)
                    .withName(R.string.time_table).withIcon(R.drawable.ic_looks_one_black_24dp);
            PrimaryDrawerItem drawerItemSignOut = new PrimaryDrawerItem().withIdentifier(SIGNOUT_ID)
                    .withName(R.string.sign_out).withIcon(R.drawable.logout);
            PrimaryDrawerItem drawerItemGroup = new PrimaryDrawerItem().withIdentifier(GROUP_ID)
                    .withName(R.string.group).withIcon(R.drawable.ic_group_black_24dp);

            AccountHeader headerResult = new AccountHeaderBuilder()
                    .withActivity(activity)
                    .withHeaderBackground(R.color.primary)
                    .addProfiles(
                            profileItem
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();
            //create the drawer and remember the `Drawer` result object
            Drawer result = new DrawerBuilder()
                    .withActivity(activity)
                    .withToolbar(toolbar)
                    .withActionBarDrawerToggle(true)
                    .withActionBarDrawerToggleAnimated(true)
                    .withTranslucentStatusBar(false)
                    .withDisplayBelowStatusBar(true)
                    .withAccountHeader(headerResult)
                    .withCloseOnClick(true)
                    .withSelectedItem(-1)
                    .withDrawerGravity(Gravity.START)
                    .addDrawerItems(
                            drawerItemHome,
                            drawerItemProfile,
                            drawerItemTimeTable,
                            drawerItemGroup,
                            drawerItemSignOut
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                            if (drawerItem.getIdentifier() == SIGNOUT_ID && !(activity instanceof LoginActivity)) {
                                // load tournament screen
                                Intent intent = new Intent(activity, LoginActivity.class);
                                view.getContext().startActivity(intent);

                            } else if (drawerItem.getIdentifier() == TIMETABLE_ID )  {
                                Intent intent = new Intent(activity, PersonalTimetableActivity.class);
                                view.getContext().startActivity(intent);
                            } else if (drawerItem.getIdentifier() == PROFILE_ID )  {
                                Intent intent = new Intent(activity, EditProfileActivity.class);
                                view.getContext().startActivity(intent);
                            } else if (drawerItem.getIdentifier() == HOME_ID )  {
                                Intent intent = new Intent(activity, MainActivity.class);
                                view.getContext().startActivity(intent);
                            } else if (drawerItem.getIdentifier() == GROUP_ID )  {
                                Intent intent = new Intent(activity, GroupsActivity.class);
                                view.getContext().startActivity(intent);
                            }
                            return true;
                        }
                    })
                    .build();
            result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        }

    }

}