package com.desirecode.videocallingapp.findfriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.desirecode.videocallingapp.ProfileViewActivity;
import com.desirecode.videocallingapp.R;
import com.desirecode.videocallingapp.VideoCallingActivity;
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

import java.util.HashMap;

public class FindFriendActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView findFriendRecyclerView;
    DatabaseReference usersRef,sendRequestRef,receivedRequestRef,requestRef,friendsRef,friendRequestNotificationRef;

/***********************************************************************/

    String receiverUserID,currentState,senderUserID;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        firebaseAuth=FirebaseAuth.getInstance();
        senderUserID=firebaseAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        sendRequestRef=FirebaseDatabase.getInstance().getReference().child("Send Friend Requests");
        receivedRequestRef=FirebaseDatabase.getInstance().getReference().child("Received Friend Requests");
        requestRef=FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends").child(senderUserID);
        friendRequestNotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        currentState="new";

        //receiverUserID=getIntent().getExtras().get("visit_user_ID").toString();

        toolbar = findViewById(R.id.find_friend_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);//add title and back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find New Friend");


        findFriendRecyclerView = findViewById(R.id.find_friend_recycler_view);
        findFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        //manageFriendRequest();
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        //usersFilter();
        setRecycler();

    }

    private void setRecycler() {

        FirebaseRecyclerOptions<FindFriendsGetterSetter> options =
                new FirebaseRecyclerOptions.Builder<FindFriendsGetterSetter>()
                        .setQuery(usersRef,FindFriendsGetterSetter.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriendsGetterSetter, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<FindFriendsGetterSetter, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendViewHolder holder, final int position, @NonNull FindFriendsGetterSetter model) {

                        String userID=getRef(position).getKey();
                        //retrieving data
                        usersFilter(userID,holder,model);


                        /** user online checker */
                        final String usersID=getRef(position).getKey();
                        usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child("UserState").hasChild("onlineState")){
                                    String state=dataSnapshot.child("UserState").child("onlineState").getValue().toString();

                                    if (state.equals("online")){
                                        holder.onlineImage.setVisibility(View.VISIBLE);
                                    }else
                                        holder.onlineImage.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        /** User click events **/
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id=getRef(position).getKey(); //get the user id via position in recycler view
                                Toast.makeText(FindFriendActivity.this,visit_user_id,Toast.LENGTH_SHORT).show();

                                Intent intent=new Intent(FindFriendActivity.this, ProfileViewActivity.class);
                                intent.putExtra("visit_user_id",visit_user_id);
                                startActivity(intent);
                            }
                        });

                        holder.addButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                receiverUserID=getRef(position).getKey();
                                //holder.itemView.setVisibility(View.INVISIBLE);
                                sendRequest(holder);
                            }
                        });

                        holder.messageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                receiverUserID=getRef(position).getKey();
                                //holder.itemView.setVisibility(View.INVISIBLE);
                                sendMessage(holder);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_find_layout, parent, false);
                        FindFriendViewHolder findFriendViewHolder = new FindFriendViewHolder(view);
                        return findFriendViewHolder;
                    }
                };
        adapter.startListening();
        findFriendRecyclerView.setAdapter(adapter);
    }



    private void usersFilter(final String userID, final FindFriendViewHolder holder, final FindFriendsGetterSetter model) {
        requestRef.child(senderUserID).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()){
                    friendsRef.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.hasChildren()){
                                holder.itemView.setVisibility(View.VISIBLE);
                                holder.username.setText(model.getUserName());
                                holder.status.setText(model.getStatus());
                                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(holder.profileImage);
                            }else
                            holder.itemView.setVisibility(View.GONE);
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

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView username, status;
        CircleImageView profileImage;
        ImageView onlineImage;
        Button messageButton,addButton;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.find_friend_username_tv);
            status=itemView.findViewById(R.id.find_friend_status_tv);
            profileImage=itemView.findViewById(R.id.find_friend_profile_img);
            onlineImage=itemView.findViewById(R.id.find_friend_online_img);
            messageButton=itemView.findViewById(R.id.find_friend_message_btn);
            addButton=itemView.findViewById(R.id.find_friend_add_btn);
        }
    }



    public void sendRequest(final FindFriendViewHolder holder) {

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
                                                    /*holder.addButton.setEnabled(true);
                                                    holder.addButton.setText("Cancel");*/

                                                    HashMap<String,String>requestNotificationMap=new HashMap<>();
                                                    requestNotificationMap.put("from",senderUserID);
                                                    requestNotificationMap.put("type","request");

                                                    friendRequestNotificationRef.child(receiverUserID)
                                                            .setValue(requestNotificationMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        currentState="request_sent";
                                                                        Toast.makeText(FindFriendActivity.this,"Send Successfully",Toast.LENGTH_SHORT).show();
                                                                        holder.addButton.setText("Request Send");
                                                                        holder.addButton.setEnabled(false);
                                                                        currentState="new";
                                                                    }
                                                                }
                                                            });

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

    private void sendMessage(FindFriendViewHolder holder) {
        Toast.makeText(this,"It will implement later",Toast.LENGTH_SHORT).show();
    }



    public void manageFriendRequest(){
/*  chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild(receiverUserID)){
            String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
            if (request_type.equals("sent")){
                currentState="request_sent";
                holder.addButton.setText("Request Send");
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});
        //cannot send request to your own account
        if (!senderUserID.equals(receiverUserID)){
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addButton.setEnabled(false);
                    if (currentState.equals("new")){
                        sendRequest();

                    }
                }
            });
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_block_user) {
            //sendUserToProfile();
        }
        if (id == R.id.video_call_img_btn) {
            /*Intent intent=new Intent(this, VideoCallingActivity.class);
            intent.putExtra("receiver_id",messageReceiverID);
            startActivity(intent);*/
        }
        if (id == R.id.voice_call_img_btn) {
            //sendUserToProfile();
        }

        return super.onOptionsItemSelected(item);
    }

}