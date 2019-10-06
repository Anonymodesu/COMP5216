package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.util.Assert;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends BaseActivity {
    private String TAG = "Edit Profile";
    //Views
    private EditText userName, userPhone;
    private TextView resetPw, userEmail;
    private CircleImageView ProfileImage;
    private FloatingActionButton saveBtn;
    private TextView pageName;
    DocumentReference currentUser;
    User user;

    //firebase authentication
    private FirebaseAuth mAuth;
    private String userId, name, email, imagePath, phoneNumber;
    private StorageReference UserProfileImageRef;
    Uri resultUri;
    //request codes
    private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 101;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setUpGlobalNav(EditProfileActivity.this, "Edit Profile");

        if (!marshmallowPermission.checkPermissionForReadfiles())
        {
            marshmallowPermission.requestPermissionForReadfiles();
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser userAuth = mAuth.getCurrentUser();
        assert userAuth != null;
        userId = userAuth.getUid();
        email = userAuth.getEmail();

        userEmail = (TextView) findViewById(R.id.email);
        userName = (EditText) findViewById(R.id.edit_screen_name);
        userPhone = (EditText) findViewById(R.id.edit_phone);
        saveBtn = (FloatingActionButton) findViewById(R.id.btn_save);
        resetPw = (TextView) findViewById(R.id.link_resetpw);
        ProfileImage = (CircleImageView) findViewById(R.id.edit_profile_image);


        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        currentUser = mFirestore.collection("Users").document(userId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        currentUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                assert user != null;
                imagePath = user.getPhoto();
                phoneNumber = user.getPhone();
                name = user.getUsername();
                userEmail.setText(email);
                userName.setText(name);
                userPhone.setText(phoneNumber);
                if(imagePath != null){
                    Picasso.get()
                            .load(imagePath)
                            .placeholder(R.drawable.profile)
                            .into(ProfileImage);
                }
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = userPhone.getText().toString();
                if (!phoneNo.matches("^[0-9]+$")) {
                    userPhone.setError("Phone number must only consist of digits");
                } else if (userName.getText().toString().trim().length() == 0){
                    userName.setError("You must enter a username");
                } else {
                    SaveUserInformation();
                }
            }
        });
        resetPw.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                SendUserToForgetPwActivity();
            }
        });
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent for picking a photo from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Bring up gallery to select a photo
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_PHOTOS);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHOTOS) {
            if (resultCode == RESULT_OK && data != null) {
                Uri photoUri = data.getData();
                CropImage.activity(photoUri)
                        .start(this);

            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(userId + ".jpg");
                UploadTask uploadTask = filePath.putFile(resultUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            assert downloadUri != null;
                            user.setPhoto(downloadUri.toString());
                            ProfileImage.setImageURI(resultUri);
                        }
                    }
                });
            }
        }
    }

    private void SaveUserInformation() {
        String name = userName.getText().toString();
        String phone = userPhone.getText().toString();
        showProgressDialog();

        user.setUsername(name);
        user.setPhone(phone);

        //if the user has uploaded a picture, include the picture with what needs to be saved in the database
        if (resultUri != null) {
            final StorageReference filePath = UserProfileImageRef.child(userId+".jpg");
            UploadTask uploadTask = filePath.putFile(resultUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                // Continue with the task to get the download URL
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        user.setPhoto(downloadUri.toString());
                        setUserInformation();
                        return;
                    }
                }
            });
        }

        //else save the changes they have made to username and phone number
        setUserInformation();

    }

    private void setUserInformation() {
        currentUser.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    hideProgressDialog();
                    showSnackbar("User Information has been updated successfully", EditProfileActivity.this);
                    SendUserToMainActivity();
                } else {
                    String message = task.getException().getMessage();
                    showSnackbar(message, EditProfileActivity.this);
                    hideProgressDialog();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
        //can be deleted depend on main page
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    public void SendUserToForgetPwActivity(){
        Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }
}
