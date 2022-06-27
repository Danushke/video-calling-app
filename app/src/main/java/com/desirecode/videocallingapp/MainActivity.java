package com.desirecode.videocallingapp;

import android.content.Intent;
import android.os.Bundle;

import com.desirecode.videocallingapp.findfriend.FindFriendActivity;
import com.desirecode.videocallingapp.findfriend.ReceivedFriendRequestActivity;
import com.desirecode.videocallingapp.findfriend.SendFriendRequestActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    private ViewPager viewPager;
    private TabAccessAdapter tabAccessAdapter;
    private TabLayout tabLayout;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        auth=FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.main_activity_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Face Call");
        //getSupportActionBar().setIcon(R.drawable.ic_baseline_supervisor_account_24);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent fFriendIntent=new Intent(MainActivity.this, FindFriendActivity.class);
                startActivity(fFriendIntent);
            }
        });

        viewPager=findViewById(R.id.view_pager);
        tabAccessAdapter = new TabAccessAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAccessAdapter);

        tabLayout=findViewById(R.id.main_tab);
        tabLayout.setupWithViewPager(viewPager);

        //createToken();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            sendUserToLogin();
        }
        else {
            userOnlineUpdater("online");
            verifyUserExistance();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        userOnlineUpdater("offline");

    }

    /*@Override
    protected void onPostResume() {
        super.onPostResume();
        //userOnlineUpdater("online");
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        userOnlineUpdater("online");
    }

    /**If minimize the app*/
   /* @Override
    protected void onStop() {
        super.onStop();

        if (currentUser!=null)
        userOnlineUpdater("offline");
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentUser!=null)
            userOnlineUpdater("offline");
    }

    private void verifyUserExistance() {
        String currentUserId=auth.getCurrentUser().getUid();
        databaseReference.child("Users").child(currentUserId).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("username").exists())){
                    //Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();;

                }
                else {
                    sendUserToProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            sendUserToProfile();
        }
        if (id == R.id.action_settings) {
            sendUserToSettings();
        }
        if (id == R.id.action_block) {
            sendUserToBlockList();
        }

        if (id == R.id.action_send_request){
            sendUserToSendRequest();
        }

        if (id == R.id.action_logout) {
            auth.signOut();
            sendUserToLogin();
        }

        if (id == R.id.unknown_message) {
            sendUserToUnknownMessage();
        }
        if (id == R.id.friend_request) {
            sendUserToReceivedRequest();
        }


        return super.onOptionsItemSelected(item);

    }

    private void sendUserToBlockList() {

    }


    private void sendUserToSettings() {
        Intent intent=new Intent(MainActivity.this,Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToLogin() {
        Intent loginIntent=new Intent(MainActivity.this,Login.class);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToProfile() {
        Intent intent=new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(intent);
    }
    private void sendUserToSendRequest() {
        Intent intent=new Intent(MainActivity.this, SendFriendRequestActivity.class);
        startActivity(intent);
    }
    private void sendUserToReceivedRequest() {
        Intent intent=new Intent(MainActivity.this, ReceivedFriendRequestActivity.class);
        startActivity(intent);
    }

    private void sendUserToUnknownMessage() {
        Intent intent=new Intent(MainActivity.this, ReceivedFriendRequestActivity.class);
        startActivity(intent);
    }

    private void userOnlineUpdater(String state) {
        String saveCurrentTime,saveCurrentDate;

        Calendar calander= Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        saveCurrentDate=currentDate.format(calander.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calander.getTime());

        HashMap<String,Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("onlineState", state);

        currentUserID=currentUser.getUid();
        databaseReference.child("Users").child(currentUserID).child("UserState").updateChildren(onlineStateMap);

    }
    private void createToken() {
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()){
                            String token=FirebaseMessaging.getInstance().getToken().toString();
                            saveTokenInDatabase(token);
                        }
                    }
                });
    }

    private void saveTokenInDatabase(String token) {
        databaseReference.child(currentUserID).child("tkn").setValue(token);
    }

}