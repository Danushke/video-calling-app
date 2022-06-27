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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReceivedFriendRequestActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView findFriendRecyclerView;
    DatabaseReference userRef,friendsRef,receivedRequestRef,sendRequestRef,currentUserReceivedRequestRef,friendRequestRef;

/***********************************************************************/

    String receiverUserID,currentState,senderUserID;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_friend_request);

        firebaseAuth=FirebaseAuth.getInstance();
        senderUserID=firebaseAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        sendRequestRef=FirebaseDatabase.getInstance().getReference().child("Send Friend Requests");
        receivedRequestRef=FirebaseDatabase.getInstance().getReference().child("Received Friend Requests");
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        friendRequestRef=FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        currentState="new";

        //receiverUserID=getIntent().getExtras().get("visit_user_ID").toString();


        toolbar = findViewById(R.id.find_friend_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);//add title and back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Received Requests");


        findFriendRecyclerView = findViewById(R.id.received_friend_recycler_view);
        findFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*receivedRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(senderUserID)){
                    currentUserReceivedRequestRef=receivedRequestRef.child(senderUserID);
                    setRecycler();
                }else {
                    Toast.makeText(ReceivedFriendRequestActivity.this,"you have not received any request",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ReceivedFriendRequestActivity.this,"you have not received any request",Toast.LENGTH_SHORT).show();
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
                                    if (type.equals("received")){
                                        retrieveUserInfo(list_user_id,holder);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        friendRequestRef.child(senderUserID).child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue().toString().equals("received")){
                                    String userID=getRef(position).getKey();
                                    /**  Retrieving user information of who send the request to you from database **/
                                    retrieveUserInfo(userID,holder);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });








                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id=getRef(position).getKey(); //get the user id via position in recycler view
                                Toast.makeText(ReceivedFriendRequestActivity.this,visit_user_id,Toast.LENGTH_SHORT).show();

                                sendUserToProfileViewActivity(visit_user_id);
                            }
                        });

                        holder.confirmButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                receiverUserID=getRef(position).getKey();
                                //holder.itemView.setVisibility(View.INVISIBLE);
                                confirmRequest(holder);
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
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_request_layout, parent, false);
                        FindFriendViewHolder findFriendViewHolder = new FindFriendViewHolder(view);
                        return findFriendViewHolder;
                    }
                };
        findFriendRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }



    private void sendUserToProfileViewActivity(String visit_user_id) {
        Intent intent=new Intent(ReceivedFriendRequestActivity.this, ProfileViewActivity.class);
        intent.putExtra("visit_user_id",visit_user_id);
        startActivity(intent);
    }


    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView userImage;
        ImageView onlineImage;
        Button cancelButton,confirmButton;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.received_request_username_tv);
            userStatus=itemView.findViewById(R.id.received_request_status_tv);
            userImage=itemView.findViewById(R.id.received_request_profile_img);
            onlineImage=itemView.findViewById(R.id.received_request_online_img);
            cancelButton=itemView.findViewById(R.id.received_request_cancel_btn);
            confirmButton=itemView.findViewById(R.id.received_request_confirm_btn);
        }
    }



    public void confirmRequest(final FindFriendViewHolder holder) {
        /*** add the data to Friends node in database*/
        friendsRef.child(senderUserID).child(receiverUserID)
                .child("Friends").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(senderUserID)
                                    .child("Friends").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                               deleteDataFromRequests(holder);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void deleteDataFromRequests(final FindFriendViewHolder holder) {
        /*** remove the data from send request node & received request node in database*/
        friendRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(ReceivedFriendRequestActivity.this,"now both you are a friends",Toast.LENGTH_SHORT).show();
                                                holder.itemView.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
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
                                                holder.confirmButton.setText("Add");
                                                holder.confirmButton.setEnabled(true);
                                                holder.cancelButton.setText("message");
                                            }
                                        }
                                    });

                        }
                    }
                });
    }



    private void retrieveUserInfo(String userID, final FindFriendViewHolder holder) {
        userRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")){
                    String profileImage=dataSnapshot.child("image").getValue().toString();
                    String profileName=dataSnapshot.child("username").getValue().toString();
                    String profileStatus=dataSnapshot.child("status").getValue().toString();

                    holder.userName.setText(profileName);
                    holder.userStatus.setText(profileStatus);
                    Picasso.get().load(profileImage).into(holder.userImage);
                }
                else {
                    String profileName=dataSnapshot.child("username").getValue().toString();
                    String profileStatus=dataSnapshot.child("status").getValue().toString();

                    holder.userName.setText(profileName);
                    holder.userStatus.setText(profileStatus);
                }

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