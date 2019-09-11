package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {
    private FirebaseAuth mAuth;

    //Views
    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        UserEmail = (EditText) findViewById(R.id.Siginup_Email);
        UserPassword = (EditText) findViewById(R.id.Signup_Password);
        UserConfirmPassword = (EditText) findViewById(R.id.Signup_Confirmpw);
        CreateAccountButton = (Button) findViewById(R.id.Signup_Button);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        //can be deleted depend on main page
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void CreateNewAccount(){
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();
        if (!validateForm()){
            return;
        }
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            SendUserToMainActivity();

                            Toast.makeText(RegisterActivity.this, R.string.auth_success, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, R.string.auth_failed + message, Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = UserEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            UserEmail.setError("Required.");
            valid = false;
        } else {
            UserEmail.setError(null);
        }

        String password = UserPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            UserPassword.setError("Required.");
            valid = false;
        } else {
            UserPassword.setError(null);
        }
        String confirmPassword = UserConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(confirmPassword)) {
            UserConfirmPassword.setError("Required.");
            valid = false;
        } else if(!password.equals(confirmPassword)){
            UserConfirmPassword.setError("Passwords does not match.");
        }else{
            UserConfirmPassword.setError(null);
        }
        return valid;
    }
}
