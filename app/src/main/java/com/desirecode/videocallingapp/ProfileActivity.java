package com.desirecode.videocallingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    Button updateButton;
    EditText username,status;
    CircleImageView profileImage;
    Toolbar toolbar;
    String currentUserId;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    private static final int gallery=1;
    StorageReference userProfileImageRef;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth=FirebaseAuth.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile images");
        initialization();
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });
        retriveUserInfor();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallery);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);//add title and back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
    }

    private void retriveUserInfor() {
        databaseReference.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("username")&&dataSnapshot.hasChild("status")&&dataSnapshot.hasChild("userimage")){
                            String retrieveUsername=dataSnapshot.child("username").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            String retrieveUserImage=dataSnapshot.child("userimage").getValue().toString();

                            username.setText(retrieveUsername);
                            status.setText(retrieveStatus);
                            Picasso.get().load(retrieveUserImage).placeholder(R.drawable.ic_launcher_background).into(profileImage);
                            //retrieveImage(retrieveUserImage);
                        }
                        else if (dataSnapshot.exists() && dataSnapshot.hasChild("username")&&dataSnapshot.hasChild("status")){
                            String retrieveUsername=dataSnapshot.child("username").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            username.setText(retrieveUsername);
                            status.setText(retrieveStatus);
                        }else {
                            Toast.makeText(ProfileActivity.this,"Please update profile",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initialization() {
        updateButton=findViewById(R.id.update_btn);
        username=findViewById(R.id.user_name);
        status=findViewById(R.id.about_status);
        profileImage=findViewById(R.id.profile_image);
        progressDialog = new ProgressDialog(this);
    }

    /***To get result of gallery & crop*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallery && resultCode==RESULT_OK && data!=null){
            Uri imageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult activityResult=CropImage.getActivityResult(data);
            /**getting cropped image when clicked crop*/
            if (requestCode != RESULT_OK) { //in my reference video used the == sign in if condition. but in my case it is wrong. so that check it later with clear mind
                Uri resultUri = activityResult.getUri();

                progressDialog.setTitle("Set Profile image");
                progressDialog.setMessage("Please wait your profile is uploading");
                progressDialog.setCanceledOnTouchOutside(true);


                /***storing image in firebase storage*/
                StorageReference filePathRef = userProfileImageRef.child(currentUserId+".jpg"); //every image is save as user is .jpg

                filePathRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ProfileActivity.this,"Profile Image is Upload Successfully...",Toast.LENGTH_SHORT).show();

                            /*** get the link of image & storing firebase database*/
                            final String downloadURL = task.getResult().getStorage().getDownloadUrl().toString();
                            databaseReference.child("Users").child(currentUserId).child("userimage") //creating node to store link
                                    .setValue(downloadURL)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(ProfileActivity.this,"Image saved database successfully",Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }else {
                                                String message=task.getException().toString();
                                                Toast.makeText(ProfileActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                        }else {
                            String message=task.getException().toString();
                            Toast.makeText(ProfileActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = activityResult.getError();
                progressDialog.dismiss();
            }
        }
    }

    private void updateUserProfile() {
        String setUserName=username.getText().toString();
        String setStatus=status.getText().toString();
        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(ProfileActivity.this,"Empty Field",Toast.LENGTH_SHORT).show();
        }
        if (setStatus.isEmpty()){
            Toast.makeText(ProfileActivity.this,"Empty Field",Toast.LENGTH_SHORT).show();
        }

        else {

            //HashMap<String,String> profileMap = new HashMap<>();
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("username",setUserName);
            profileMap.put("status",setStatus);
            //databaseReference.child("Users").child(currentUserId).setValue(profileMap)
            databaseReference.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this,"Profile Update Successful",Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                            }
                            else {
                                String message=task.getException().toString();
                                Toast.makeText(ProfileActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();;
                            }
                        }
                    });
        }
    }

    private void retrieveImage(String url){
        Glide.with(ProfileActivity.this).load(url).into(profileImage);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(ProfileActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}