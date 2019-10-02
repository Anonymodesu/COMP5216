package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends BaseActivity {
    private static final String TAG = "ChangePasswordActivity";

    private EditText userEmail;
    private FirebaseAuth mAuth;
    private ProgressDialog dialog;
    private Button updatePasswordBtn;
    private TextView pageName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        //views
        pageName = findViewById(R.id.page_name);
        //set page title
        pageName.setText("Update Password");

        mAuth = FirebaseAuth.getInstance();

        userEmail = findViewById(R.id.pw_reset_email);
        updatePasswordBtn = findViewById(R.id.UpdatePasswordBtn);
        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResetPwEmail();
//                change(view);
            }
        });
    }

    public void sendResetPwEmail(){
        showProgressDialog();
        mAuth.sendPasswordResetEmail(userEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "SendResetPasswordLink:success");
//                            Toast.makeText(ChangePasswordActivity.this, "Reset password link has been sent to you email address", Toast.LENGTH_SHORT).show();
                            showSnackbar("Reset password link has been sent to you email address", ChangePasswordActivity.this);
                            SendUserToLoginActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "SendResetPasswordLink:failure", task.getException());
                            String message = task.getException().getMessage();
//                            Toast.makeText(ChangePasswordActivity.this, "send reset password email failed: " + message, Toast.LENGTH_SHORT).show();
                            showSnackbar("send reset password email failed: ", ChangePasswordActivity.this);
                        }
                        hideProgressDialog();
                    }
                });
    }
//    public void change(View v) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        System.out.println("Current user email: "+ user.getEmail()+" "+user.getDisplayName());
//        if (user != null) {
//            dialog.setMessage("Changing Password, Please Wait...");
//            user.updatePassword(e1.getText().toString())
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()) {
//                                dialog.dismiss();
//                                System.out.println("coming here, sending user back to main");
//                                SendUserToMainActivity();
//                                Toast.makeText(getApplicationContext(),"Your Password has been Changed",Toast.LENGTH_LONG).show();
//                            }
//                            else{
//                                dialog.dismiss();
//                                Toast.makeText(getApplicationContext(), "Password could not be Changed",Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });
//        }
//
//    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
