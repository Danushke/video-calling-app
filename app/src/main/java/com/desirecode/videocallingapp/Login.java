package com.desirecode.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.service.media.MediaBrowserService;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class Login extends AppCompatActivity {

    FirebaseAuth auth;
    EditText loginEmail,loginPassword;
    Button loginButton,regButton;
    TextView registerTextView;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

        initialization();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAccount();
            }
        });
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Login.this,RegisterActivity.class);
                startActivity(intent);
            }
        });


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayShowHomeEnabled(true);//add title and back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.ic_baseline_supervisor_account_24);
    }

    private void loginAccount() {
        String email=loginEmail.getText().toString().trim();
        String password=loginPassword.getText().toString().trim();

        if (email.isEmpty()){
            Toast.makeText(this,"Please enter Email",Toast.LENGTH_SHORT).show();
            loginEmail.setError("****");
            loginEmail.requestFocus();
        }
        if (password.isEmpty()){
            Toast.makeText(this,"Please enter Password",Toast.LENGTH_SHORT).show();
            loginPassword.setError("enter Password");
            loginPassword.requestFocus();
        }

        else {
            progressDialog.setTitle("Login to Account");
            progressDialog.setMessage("Please a wait");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            
                            if (task.isSuccessful()){

                                String currentUserID=auth.getCurrentUser().getUid();
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                                //Task<String> deviceToken= FirebaseMessaging.getInstance().getToken();
                                //Task<InstallationTokenResult> deviceToken= FirebaseInstallations.getInstance().getToken(true);



                                /*FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                                    return;
                                                }

                                                // Get new FCM registration token
                                                String token = task.getResult();

                                                // Log and toast
                                                String msg = getString(R.string.msg_token_fmt, token);
                                                Log.d(TAG, msg);
                                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                            }
                                        });*/




                                databaseReference.child(currentUserID).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                sendUserToMainActivity();
                                                Toast.makeText(Login.this,"Successful Login",Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();

                                            }
                                        });


                            }else {
                                String message=task.getException().toString();
                                Toast.makeText(Login.this,"Error "+message,Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    })/*.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            })*/;

        }
    }

    private void registerUser() {
        String email=loginEmail.getText().toString().trim();
        String password=loginPassword.getText().toString().trim();

        if (email.isEmpty()){
            Toast.makeText(this,"Please enter Email",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter Password",Toast.LENGTH_SHORT).show();
        }if (password.length()<6){
            loginPassword.setError("Too short");
            loginPassword.requestFocus();
            return;
        }
        else {
            progressDialog.setTitle("Creating new Account");
            progressDialog.setMessage("Please a wait");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String currentUserID=auth.getCurrentUser().getUid();
                                databaseReference.child(currentUserID).setValue("");
                                sendUserToMainActivity();
                                Toast.makeText(Login.this,"Successful",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                            else if (task.getException() instanceof FirebaseAuthActionCodeException){
                                Toast.makeText(Login.this,"You are already registered in",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else {
                                String message=task.getException().toString();
                                Toast.makeText(Login.this,"Error"+message,Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

        }
    }


    private void initialization() {
        toolbar = findViewById(R.id.login_toolbar);
        loginEmail=findViewById(R.id.login_email);
        loginPassword=findViewById(R.id.login_pwd);
        loginButton=findViewById(R.id.login_btn);
        registerTextView=findViewById(R.id.send_register_to_textview);
        regButton=findViewById(R.id.reg_btn);
        progressDialog = new ProgressDialog(this);
    }


    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(Login.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}