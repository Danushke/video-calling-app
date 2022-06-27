package com.desirecode.videocallingapp.findfriend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.desirecode.videocallingapp.ProfileViewActivity;
import com.desirecode.videocallingapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class SendFriendRequestActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView findFriendRecyclerView;
    DatabaseReference userRef,friendsRef,receivedRequestRef,sendRequestRef,currentUserSendRequestRef,friendRequestRef;

/***********************************************************************/

    String receiverUserID,currentState,senderUserID;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_friend_request);

        firebaseAuth=FirebaseAuth.getInstance();
        senderUserID=firebaseAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        receivedRequestRef=FirebaseDatabase.getInstance().getReference().child("Received Friend Requests");
        sendRequestRef=FirebaseDatabase.getInstance().getReference().child("Send Friend Requests");
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        friendRequestRef=FirebaseDatabase.getInstance().getReference().child("Friend Requests").getRef();

        currentState="new";

        //receiverUserID=getIntent().getExtras().get("visit_user_ID").toString();

        toolbar = findViewById(R.id.find_friend_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);//add title and back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Send Requests");

        findFriendRecyclerView = findViewById(R.id.send_friend_recycler_view);
        findFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //manageFriendRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*sendRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(senderUserID)){
                    currentUserSendRequestRef=sendRequestRef.child(senderUserID);
                    setRecycler();
                }else {
                    Toast.makeText(SendFriendRequestActivity.this,"you have not send any request",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        friendRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    setRecycler();
                }else
                    Toast.makeText(SendFriendRequestActivity.this,"you have not send any request",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setRecycler() {
        FirebaseRecyclerOptions<FindFriendsGetterSetter> options =
                new FirebaseRecyclerOptions.Builder<FindFriendsGetterSetter>()
                        .setQuery(friendRequestRef.child(senderUserID),FindFriendsGetterSetter.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriendsGetterSetter, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<FindFriendsGetterSetter, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendViewHolder holder, final int position, @NonNull FindFriendsGetterSetter model) {


                        final String list_user_id=getRef(position).getKey();
                        DatabaseReference getRquestTypeRef=getRef(position).child("request_type").getRef();

                        getRquestTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String type=dataSnapshot.getValue().toString();
                                    if (type.equals("send")){
                                        retrieveUserInfo(list_user_id,holder);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        /**
                         * **********************************************************************************************
                         * when i use following these the filtering is ok but after i click the camcel button app was crash.
                         * but database also changes are work correctly please check whats the reason for that **/

                        /*friendRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(getRef(position).getKey()).hasChildren())
                                if (dataSnapshot.getValue().toString().equals("send")){
                                    String userID=getRef(position).getKey();
                                    retrieveUserInfo(userID,holder);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });*/
                        /*********************************************************************************/


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id=getRef(position).getKey(); //get the user id via position in recycler view
                                Toast.makeText(SendFriendRequestActivity.this,visit_user_id,Toast.LENGTH_SHORT).show();

                                Intent intent=new Intent(SendFriendRequestActivity.this, ProfileViewActivity.class);
                                intent.putExtra("visit_user_id",visit_user_id);
                                startActivity(intent);
                            }
                        });

                        holder.messageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                receiverUserID=getRef(position).getKey();
                                //holder.itemView.setVisibility(View.INVISIBLE);
                                Toast.makeText(SendFriendRequestActivity.this,"it will implement later",Toast.LENGTH_SHORT).show();
                            }
                        });

                        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                receiverUserID=getRef(position).getKey();
                                //holder.itemView.setVisibility(View.INVISIBLE);
                                cancelRequest(holder);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_request_layout, parent, false);
                        FindFriendViewHolder findFriendViewHolder = new FindFriendViewHolder(view);
                        return findFriendViewHolder;
                    }
                };
        findFriendRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView userImage;
        ImageView onlineImage;
        Button cancelButton,messageButton;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.send_request_username_tv);
            userStatus=itemView.findViewById(R.id.send_request_status_tv);
            userImage=itemView.findViewById(R.id.send_request_profile_img);
            onlineImage=itemView.findViewById(R.id.send_request_online_img);
            cancelButton=itemView.findViewById(R.id.send_request_cancel_btn);
            messageButton=itemView.findViewById(R.id.send_request_message_btn);
        }
    }


    private void cancelRequest(final FindFriendViewHolder holder){
        friendRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState="new";
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


    private void retrieveUserInfo(String userID, final FindFriendViewHolder holder) {
        /**  Retrieving user information of send the request from the firebase database  **/

        userRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")){
                    String profileImage=dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(profileImage).into(holder.userImage);
                }

                    String profileName=dataSnapshot.child("username").getValue().toString();
                    String profileStatus=dataSnapshot.child("status").getValue().toString();

                    holder.userName.setText(profileName);
                    holder.userStatus.setText(profileStatus);

                if (dataSnapshot.child("UserState").hasChild("onlineState")){
                    String state=dataSnapshot.child("UserState").child("onlineState").getValue().toString();

                    if (state.equals("online")){

                        holder.onlineImage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}