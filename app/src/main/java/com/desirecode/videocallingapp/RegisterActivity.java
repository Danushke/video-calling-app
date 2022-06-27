package com.desirecode.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    FirebaseAuth auth;
    EditText editTextFullName,editTextEmail,editTextPassword,editTextID,editTextUsername;
    TextView loginTextView;
    Button loginButton,regButton;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth= FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        initialization();
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,Login.class));
            }
        });


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayShowHomeEnabled(true);//add title and back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.ic_baseline_supervisor_account_24);
    }



    private void registerUser() {
        final String email=editTextEmail.getText().toString().trim();
        String password=editTextPassword.getText().toString().trim();
        final String IDNumber=editTextID.getText().toString().trim();
        final String fullName=editTextFullName.getText().toString().trim();
        final String username=editTextUsername.getText().toString().trim();

        if (email.isEmpty()){
            Toast.makeText(this,"Please enter Email",Toast.LENGTH_SHORT).show();
            editTextEmail.setError("required");
            editTextEmail.requestFocus();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter Password",Toast.LENGTH_SHORT).show();
            editTextPassword.setError("enter Password");
            editTextEmail.requestFocus();
        }else if(TextUtils.isEmpty(IDNumber)){
            Toast.makeText(this,"Please enter ID Number",Toast.LENGTH_SHORT).show();
            editTextID.setError(" enter ID Number");
            editTextEmail.requestFocus();
        }else if (IDNumber.length()<9 || IDNumber.length()>12) {
            Toast.makeText(this,"Please enter Valid ID Number",Toast.LENGTH_SHORT).show();
            editTextID.setError("Invalid ID Number");
            editTextEmail.requestFocus(10);
        }
        else {
            progressDialog.setTitle("Creating new Account");
            progressDialog.setMessage("Please a wait");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String currentUserID=auth.getCurrentUser().getUid();
                                databaseReference.child(currentUserID).setValue("");

                                setOtherUserDetails(currentUserID,email,IDNumber,fullName,username);

                                Toast.makeText(RegisterActivity.this,"Successful",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                            else if (task.getException() instanceof FirebaseAuthActionCodeException){
                                Toast.makeText(RegisterActivity.this,"You are already registered in",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else {
                                String message=task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error"+message,Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }

    private void setOtherUserDetails(String currentUserID, String email, String IDNumber, String fullName, String username) {

        String dob="";
        String gender="";
        String d="";
        String year;
        String month;
        int days=0;

        int months[]={31,29,31,30,31,30,31,31,30,31,30,31};


        if(IDNumber.length()==10){
            year="19"+IDNumber.substring(0,2);
            d=IDNumber.substring(2,5);
            days=Integer.parseInt(d);
        }else{
            year=IDNumber.substring(0,4);
            d=IDNumber.substring(4,7);
            days=Integer.parseInt(d);
        }


        if(days>500){
            gender="female";
            days=days-500;
        }else{
            gender="male";
        }

        int m=0;
        while (days>months[m]){
            days=days-months[m];
            m+=1;
        }
        //month=monthSetter(m);

        m+=1;
        if(m<10){
            dob=year+"-"+"0"+m+"-"+days;
        }else
            dob=year+"-"+m+"-"+days;



        //HashMap<String,String> profileMap = new HashMap<>();
        HashMap<String,Object> profileMap = new HashMap<>();
        profileMap.put("uid",currentUserID);
        profileMap.put("email",email);
        profileMap.put("username",username);
        profileMap.put("fullname",fullName);
        profileMap.put("id",IDNumber);
        profileMap.put("dob",dob);
        profileMap.put("gender",gender);
        profileMap.put("mobile","phone");
        //profileMap.put("status",setStatus);
        //databaseReference.child("Users").child(currentUserId).setValue(profileMap)
        databaseReference.child("Users").child(currentUserID).updateChildren(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this,"Profile Update Successful",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            sendUserToMainActivity();
                        }
                        else {
                            String message=task.getException().toString();
                            Toast.makeText(RegisterActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();;
                        }
                    }
                });
    }

    private String monthSetter(int m) {
        String month;
        switch (m){
            case 0:
               month="January";
               return month;
            case 1:
                month="February";
                return month;
            case 2:
                month="March";
                return month;
                //break;
            case 3:
                month="April";
                return month;
                //break;
            case 4:
                month="May";
                return month;
                //break;
            case 5:
                month="June";
                return month;
                //break;
            case 6:
                month="July";
                return month;
                //break;
            case 7:
                month="Auguest";
                return month;
                //break;
            case 8:
                month="September";
                return month;
                //break;
            case 9:
                month="October";
                return month;
                //break;
            case 10:
                month="November";
                return month;
                //break;
            case 11:
                month="December";
                return month;
                //break;
            default:
                month="";
        }
        return month;
    }

    private void initialization() {

        toolbar = findViewById(R.id.sign_up_toolbar);
        editTextEmail=findViewById(R.id.editTextTextEmailAddress);
        editTextPassword=findViewById(R.id.editTextTextPassword);
        editTextFullName=findViewById(R.id.editTextFullName);
        editTextID=findViewById(R.id.editTextIDNumber);
        editTextUsername=findViewById(R.id.editTextUserName);
        regButton=findViewById(R.id.btn_register);
        loginTextView=findViewById(R.id.send_to_login_textview);
        progressDialog = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}