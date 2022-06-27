package com.desirecode.videocallingapp.messages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.desirecode.videocallingapp.R;
import com.desirecode.videocallingapp.VideoCallingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateChatActivity extends AppCompatActivity {

    private String messageReceiverID,messageReceiverName,messageReceiverImage,messageSenderID,saveCurrentTime,saveCurrentDate,calledBy;
    private TextView userName,userLastseen;
    private CircleImageView userImage;
    private EditText messageInputText;
    private ImageButton sendMessageButton;

    private FirebaseAuth auth;
    private DatabaseReference rootRef,usersRef;
    private FirebaseUser currentUser;

    private List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;

    /**make sure to import androidx.appcompat.widget  not android.widget
     * ...... ####.....
     * if you are not import proper one it will generate error when you Writing the code
     */
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        auth=FirebaseAuth.getInstance();
        messageSenderID=auth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();;

        toolbar = findViewById(R.id.private_chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chatting");
        getSupportActionBar().setDisplayShowHomeEnabled(true);//add title and back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_baseline_supervisor_account_24);

        initialization();

        //userName.setText(messageReceiverName);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDateTimeSetter();
                sendMessage();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();


        if (messageSenderID!=null)
            userOnlineUpdater("online");

        checkForReceivingCall();


        /** retrieving the messages by using message adapter class*/
        rootRef.child("Private Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()); //to automatic scroll
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkForReceivingCall() {
        usersRef.child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Ringing").hasChild("ringing")){
                    calledBy=dataSnapshot.child("ringing").getValue().toString();
                    //calledBy=dataSnapshot.child("Ringing").child("ringing").getValue().toString();

                    Intent intent=new Intent(PrivateChatActivity.this, VideoCallingActivity.class);
                    intent.putExtra("receiver_id",calledBy);
                    startActivity(intent);
                }
                if (dataSnapshot.child("Calling").hasChild("calling")){

                    Intent intent=new Intent(PrivateChatActivity.this, VideoCallingActivity.class);
                    intent.putExtra("receiver_id",messageReceiverID);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (messageSenderID!=null)
            userOnlineUpdater("offline");
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
            sendUserToVideoCall();
        }
        if (id == R.id.voice_call_img_btn) {
            //sendUserToProfile();
        }

        return super.onOptionsItemSelected(item);
    }


    private void initialization() {

        /*ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);*/

        /*LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.chat_app_bar_layout,null);
        actionBar.setCustomView(actionbarView);*/

        sendMessageButton=findViewById(R.id.private_send_message_img_btn);
        messageInputText=findViewById(R.id.private_input_message);
        //userName=findViewById(R.id.);

        messageAdapter=new MessageAdapter(messagesList);
        recyclerView=findViewById(R.id.private_chat_recycler_view);
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
    }

    private void sendMessage() {
        String messageText=messageInputText.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)){
            String messageSenderRefToSave = "Private Messages/"+messageSenderID+"/"+messageReceiverID;
            String messageReceiverRefToSave = "Private Messages/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference userMessageKeyRef=rootRef.child("Private Messages").child(messageSenderID)
                    .child(messageReceiverID).push();

            String messagePushID=userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",messageSenderID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetails=new HashMap();
           messageBodyDetails.put(messageSenderRefToSave+"/"+messagePushID,messageTextBody);
           messageBodyDetails.put(messageReceiverRefToSave+"/"+messagePushID,messageTextBody);

           rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if (task.isSuccessful()){
                       Toast.makeText(PrivateChatActivity.this,"message send successfully",Toast.LENGTH_SHORT).show();
                       messageInputText.setText(null);
                   }
               }
           });

        }
    }




    private void currentDateTimeSetter() {

        Calendar calander= Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        saveCurrentDate=currentDate.format(calander.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calander.getTime());

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

        rootRef.child("Users").child(messageSenderID).child("UserState").updateChildren(onlineStateMap);

    }





    private void sendUserToVideoCall() {
        Intent intent=new Intent(this, VideoCallingActivity.class);
        intent.putExtra("receiver_id",messageReceiverID);
        startActivity(intent);
        finish();
    }
}