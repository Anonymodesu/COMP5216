package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText e1;
    FirebaseAuth auth;
    ProgressDialog dialog;
    private Button updatePasswordBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        e1 = findViewById(R.id.EditPasswordText);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        updatePasswordBtn = findViewById(R.id.UpdatePasswordBtn);
        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change(view);
            }
        });
    }

    public void change(View v) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        System.out.println("Current user email: "+ user.getEmail()+" "+user.getDisplayName());
        if (user != null) {
            dialog.setMessage("Changing Password, Please Wait...");
            user.updatePassword(e1.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                dialog.dismiss();
                                System.out.println("coming here, sending user back to main");
                                SendUserToMainActivity();
                                Toast.makeText(getApplicationContext(),"Your Password has been Changed",Toast.LENGTH_LONG).show();
                            }
                            else{
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Password could not be Changed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    public void SendUserToMainActivity(){
        Intent mainIntent = new Intent(ChangePasswordActivity.this, MainActivity.class);
        //can be deleted depend on main page
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
