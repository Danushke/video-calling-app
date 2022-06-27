package com.desirecode.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.desirecode.videocallingapp.findfriend.FindFriendActivity;
import com.desirecode.videocallingapp.findfriend.ReceivedFriendRequestActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileViewActivity extends AppCompatActivity {

    TextView username,status;
    Button addBtn,messageBtn;
    CircleImageView imageView;

    DatabaseReference databaseReference,friendsRef,notificationRef,sendRequestRef,receivedRequestRef,requestRef;

    /***********************************************************************/

    String receiverUserID,currentState,senderUserID,currentUserId;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        firebaseAuth=FirebaseAuth.getInstance();
        //currentUserId=firebaseAuth.getCurrentUser().getUid();
        receiverUserID=getIntent().getExtras().get("visit_user_id").toString();
        senderUserID=firebaseAuth.getCurrentUser().getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        requestRef=FirebaseDatabase.getInstance().getReference().child("Friend Requests").child(senderUserID);
        sendRequestRef=FirebaseDatabase.getInstance().getReference().child("Send Friend Requests");
        receivedRequestRef=FirebaseDatabase.getInstance().getReference().child("Received Friend Requests");

        currentState="new";

        initialization();
        retriveUserInfor();
        manageRequests();
    }

    private void initialization() {
        username=findViewById(R.id.p_v_user_name);
        status=findViewById(R.id.p_v_about_status);
        addBtn=findViewById(R.id.add_btn);
        messageBtn=findViewById(R.id.message_btn);
        imageView=findViewById(R.id.profile_image);
    }

    private void retriveUserInfor() {
        databaseReference.child(receiverUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("username")&&dataSnapshot.hasChild("status")&&dataSnapshot.hasChild("userimage")){
                            String retrieveUsername=dataSnapshot.child("username").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            String retrieveUserImage=dataSnapshot.child("userimage").getValue().toString();

                            username.setText(retrieveUsername);
                            status.setText(retrieveStatus);
                            Picasso.get().load(retrieveUserImage).placeholder(R.drawable.ic_launcher_background).into(imageView);
                            //retrieveImage(retrieveUserImage);
                        }
                        else if (dataSnapshot.exists() && dataSnapshot.hasChild("username")&&dataSnapshot.hasChild("status")){
                            String retrieveUsername=dataSnapshot.child("username").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            username.setText(retrieveUsername);
                            status.setText(retrieveStatus);
                        }else {
                            Toast.makeText(ProfileViewActivity.this,"Please update profile",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void manageRequests(){
        requestRef.child(senderUserID).child(receiverUserID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    Toast.makeText(ProfileViewActivity.this,"This is Send request",Toast.LENGTH_SHORT).show();
                    currentState="request_sent";
                    addBtn.setText("Cancel");
                }
                else {
                    requestRef.child(senderUserID).child(receiverUserID)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        Toast.makeText(ProfileViewActivity.this,"This is Received request",Toast.LENGTH_SHORT).show();
                                        currentState="request_received";
                                        addBtn.setText("Accept");
                                        messageBtn.setText("Delete");
                                        }else {
                                        friendsRef.child(senderUserID).child(receiverUserID)
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()){
                                                            addBtn.setText("unfriend");
                                                            currentState="friends";
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserID.equals(receiverUserID)){
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //addBtn.setEnabled(false);
                    if (currentState.equals("new")){
                        sendRequest();
                    }if (currentState.equals("request_sent")){
                        cancelRequest();
                    }
                    if (currentState.equals("request_received")){
                        confirmRequest();
                    }
                    if (currentState.equals("friends")){
                        removeSpecificFriend();
                    }
                }
            });
        }
        else {
            addBtn.setVisibility(View.INVISIBLE);
            messageBtn.setVisibility(View.INVISIBLE);
        }
    }




    private void sendRequest() {
        if (currentState.equals("new")){
            requestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                requestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    HashMap<String,String>friendRequestNotificatonMap=new HashMap<>();
                                                    friendRequestNotificatonMap.put("from",senderUserID);
                                                    friendRequestNotificatonMap.put("type","request");

                                                    notificationRef.child(receiverUserID).push()
                                                            .setValue(friendRequestNotificatonMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        currentState="request_sent";
                                                                        Toast.makeText(ProfileViewActivity.this,"Send Successfully",Toast.LENGTH_SHORT).show();
                                                                        addBtn.setText("Request Send");
                                                                        addBtn.setEnabled(false);
                                                                        //currentState="new";
                                                                    }
                                                                }
                                                            });

                                                    /*holder.addButton.setEnabled(true);
                                                    holder.addButton.setText("Cancel");*/
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
        else {
            Toast.makeText(this,"Already send the request",Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRequest() {
        requestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            requestRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState="new";
                                                addBtn.setText("Add");
                                                /*holder.messageButton.setText("Add");
                                                holder.messageButton.setEnabled(true);
                                                holder.cancelButton.setText("message");*/
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    public void confirmRequest() {

        /*** add the data to Friends node in database*/
        requestRef.child(senderUserID).child(receiverUserID)
                .child("Friends").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            requestRef.child(receiverUserID).child(senderUserID)
                                    .child("Friends").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                deleteDataFromRequests();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void deleteDataFromRequests() {
        /*** remove the data from send request node & received request node in database*/
        requestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            requestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                addBtn.setText("Unfriend");
                                                currentState="friends";
                                                Toast.makeText(ProfileViewActivity.this,"now both you are a friends",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void removeSpecificFriend() {
        friendsRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState="new";
                                                addBtn.setText("Add");
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void unfriend(){
        requestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState="new";
                                              /*  holder.confirmButton.setText("Add");
                                                holder.confirmButton.setEnabled(true);
                                                holder.cancelButton.setText("message");*/
                                            }
                                        }
                                    });

                        }
                    }
                });
    }


}