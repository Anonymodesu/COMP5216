package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.Scroller;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class RegisterActivity extends BaseActivity {
    private static final String TAG = "Register";
    private FirebaseAuth mAuth;

    //Views
    private EditText UserEmail, UserPassword, UserConfirmPassword,UserName;
    private TextView GoBackButton, termsLink;
    private Button CreateAccountButton;
    private MaterialCheckBox checkbox;
    private GroupSelfDialog selfDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideTitleBar();
        setContentView(R.layout.activity_register);
        setupUI(findViewById(R.id.register_form), RegisterActivity.this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
       // setUpGlobalNav(RegisterActivity.this, "Register");
        UserEmail = (EditText) findViewById(R.id.Siginup_Email);
        UserPassword = (EditText) findViewById(R.id.Signup_Password);
       // UserConfirmPassword = (EditText) findViewById(R.id.Signup_Confirmpw);
        UserName = (EditText) findViewById(R.id.Siginup_Username);
        CreateAccountButton = (Button) findViewById(R.id.Signup_Button);
        GoBackButton = (TextView) findViewById(R.id.go_back);
        termsLink = (TextView) findViewById(R.id.terms_cons);
        checkbox = findViewById(R.id.check_box);



        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    CreateAccountButton.setEnabled(true);
                }else{
                    CreateAccountButton.setEnabled(false);
                }
            }
        });

        termsLink.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             selfDialog = new GroupSelfDialog(RegisterActivity.this);
                                             selfDialog.setTitle("Terms & Conditions");
                                             selfDialog.setMessage(getResources().getString(R.string.terms_and_conditions));
                                             selfDialog.setYesOnclickListener("Accept", new GroupSelfDialog.onYesOnclickListener() {
                                                 @Override
                                                 public void onYesClick() {
                                                     checkbox.setChecked(true);
                                                     selfDialog.dismiss();
                                                 }
                                             });
                                             selfDialog.setNoOnclickListener("Cancel", new GroupSelfDialog.onNoOnclickListener() {
                                                 @Override
                                                 public void onNoClick() {
                                                     selfDialog.dismiss();
                                                 }
                                             });
                                             selfDialog.show();
                                         }
                                     });

        GoBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
//        String confirmPassword = UserConfirmPassword.getText().toString();
        final String userName = UserName.getText().toString();
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userName).build();
                            assert user != null;
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendEmailVerification();
                                                SendUserToLoginActivity();
                                            }
                                        }
                                    });

                            //Toast.makeText(RegisterActivity.this, R.string.auth_success, Toast.LENGTH_SHORT).show();
                            showSnackbar(getResources().getString(R.string.register_success), RegisterActivity.this);
                        } else {
                            String message = task.getException().getMessage();
                            //Toast.makeText(RegisterActivity.this, R.string.auth_failed + message, Toast.LENGTH_SHORT).show();
                            showSnackbar(R.string.auth_failed + message, RegisterActivity.this);
                        }

                        hideProgressDialog();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;
        String displayName = UserName.getText().toString();
        if (TextUtils.isEmpty(displayName)) {
            UserName.setError("User Name is required");
            valid = false;
        } else {
            UserEmail.setError(null);
        }

        String email = UserEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            UserEmail.setError("Email address is required");
            valid = false;
        } else {
            UserEmail.setError(null);
        }

        String password = UserPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            UserPassword.setError("Password is Required.");
            valid = false;
        } else {
            UserPassword.setError(null);
        }
//        String confirmPassword = UserConfirmPassword.getText().toString();
//        if (TextUtils.isEmpty(confirmPassword)) {
//            UserConfirmPassword.setError("Please confirm password!");
//            valid = false;
//        } else if (!password.equals(confirmPassword)) {
//            UserConfirmPassword.setError("The two passwords you entered does not match!");
//        } else {
//            UserConfirmPassword.setError(null);
//        }
        return valid;
    }

    private void sendEmailVerification() {

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(RegisterActivity.this,
////                                    "Verification email sent to " + user.getEmail(),
////                                    Toast.LENGTH_SHORT).show();
                            showSnackbar("Verification email has been sent to " + user.getEmail(), RegisterActivity.this);
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
//                            Toast.makeText(RegisterActivity.this,
//                                    "Failed to send verification email.",
//                                    Toast.LENGTH_SHORT).show();
                            showSnackbar("Failed to send verification email.", RegisterActivity.this);
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }
}
