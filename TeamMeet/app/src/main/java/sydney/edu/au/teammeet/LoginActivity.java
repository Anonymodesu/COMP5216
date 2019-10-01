package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private static final int RC_SIGN_IN = 9001;

    private EditText userEmail, userPassword;
    private TextView pageName;
    private ImageView googleSignInButton, facebookSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
//for facebook sign in
    private CallbackManager mCallbackManager;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //set up page name
        pageName=findViewById(R.id.page_name);
        pageName.setText("Log In");
       //setup soft keyboard setting for login
        setupUI(findViewById(R.id.signin_form), LoginActivity.this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null){
            //user is signed in log user out
            signOut();
        }
        mCallbackManager = CallbackManager.Factory.create();

        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        googleSignInButton = findViewById(R.id.google_signin_button);
        facebookSignInButton = findViewById(R.id.facebook_signin_button);
        facebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookSignIn();
            }
        });
        //Sign In Button
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });
        //signup and forget pw link
        findViewById(R.id.link_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });
        findViewById(R.id.link_forgetpw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToForgetPwActivity();
            }
        });

        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                GoogleSignIn();
            }
        });
    }
    private void GoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]
    private void facebookSignIn(){
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
        loginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
//                            Toast.makeText(LoginActivity.this, R.string.auth_success, Toast.LENGTH_SHORT).show();
                            showSnackbar(getResources().getString(R.string.auth_success), LoginActivity.this);
                            //FirebaseUser user = mAuth.getCurrentUser();
                            SendUserToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().getMessage();
                            //Toast.makeText(LoginActivity.this, R.string.auth_failed + message, Toast.LENGTH_SHORT).show();
                            showSnackbar(R.string.auth_failed + message, LoginActivity.this);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]
    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            SendUserToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                            showSnackbar("Authenticating with Facebook has failed", LoginActivity.this);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_facebook]
    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                String message = task.getException().getMessage();
                showSnackbar(R.string.auth_failed + message, LoginActivity.this);
//                Toast.makeText(LoginActivity.this, R.string.auth_failed + message, Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        }else{
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);}
    }
    // [END onactivityresult]


    private void AllowUserToLogin(){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            assert currentUser != null;
                            if(!currentUser.isEmailVerified()){
//                                Toast.makeText(LoginActivity.this, R.string.pls_verify_email,
//                                        Toast.LENGTH_SHORT).show();
                                showSnackbar(getResources().getString(R.string.pls_verify_email), LoginActivity.this);
                                hideProgressDialog();
                                return;
                            }
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            SendUserToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
//                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
//                                    Toast.LENGTH_SHORT).show();
                            showSnackbar("Login failed. Please check your email and password", LoginActivity.this);
                        }

                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = userEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            userEmail.setError("Required.");
            valid = false;
        } else {
            userEmail.setError(null);
        }

        String password = userPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            userPassword.setError("Required.");
            valid = false;
        } else {
            userPassword.setError(null);
        }
        return valid;
    }
    private void signOut() {
        if (user != null) {
            for(UserInfo userInfo: user.getProviderData()){
                if(userInfo.getProviderId().equals("facebook.com")){
                    facebookSignOut();
                }
            }
            mAuth.signOut();
        }
    }
    private void facebookSignOut(){
        LoginManager.getInstance().logOut();
    }
    private void googleSignOut(){
        // Google sign out
        mGoogleSignInClient.signOut();
    }

    public void SendUserToRegisterActivity(){
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
    public void SendUserToMainActivity(){
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        //can be deleted depend on main page
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    public void SendUserToForgetPwActivity(){
        Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }
}
