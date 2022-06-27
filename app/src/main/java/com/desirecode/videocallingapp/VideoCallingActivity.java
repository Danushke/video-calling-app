package com.desirecode.videocallingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class VideoCallingActivity extends AppCompatActivity {
    TextView userName;
    CircleImageView callingUserImage;
    ImageView cancleCall,acceptCall;
    String receiverUserImage,receiverUsername,receiverUserID,receiverUserID_temp;
    String senderUserImage,senderUsername,currentUserID;
    String callerID="",ringingID="";
    String checker="";
    DatabaseReference userRef;
    ValueEventListener callerEventListener,receiverEventListener;

    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_calling);

        //checker="clicked";

        currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid(); //current user
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        receiverUserID=getIntent().getExtras().get("receiver_id").toString();
        //receiverUserID_temp=getIntent().getExtras().get("receiver_id").toString();

        mediaPlayer=MediaPlayer.create(this,R.raw.ringing_tone);

        initialization();

        cancleCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker="clicked";
                //mediaPlayer.stop();
                Toast.makeText(VideoCallingActivity.this,checker+" ll "+checker,Toast.LENGTH_SHORT).show();
                cancelCalling();

            }
        });
        acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();

                final  HashMap<String,Object> callingPickUpMap=new HashMap<>();
                callingPickUpMap.put("picked","picked");

                userRef.child(currentUserID).child("Ringing").updateChildren(callingPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete()){
                                    Intent intent=new Intent(getApplicationContext(),VideoChatActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
        getAndSetUserProfileInfo();

    }

    private void cancelCalling() {
        Toast.makeText(VideoCallingActivity.this,checker+" 2 "+checker,Toast.LENGTH_SHORT).show();


        /*************************      cancel call from sender side      ******************************/





        callerEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&& dataSnapshot.hasChild("calling")){
                    ringingID=dataSnapshot.child("calling").getValue().toString();

                    final HashMap<String,Object> cancelingInfo=new HashMap<>();
                    cancelingInfo.put("calling","cancel");

                    userRef.child(ringingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            userRef.child(currentUserID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    userRef.child(currentUserID).child("Calling").removeEventListener(callerEventListener);
                                    //removeActivityOfRinger(ringingID);
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    finish();
                                }
                            });
                        }
                    });
                }
                else {
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userRef.child(currentUserID).child("Calling").addValueEventListener(callerEventListener);
        //userRef.child(currentUserID).child("Calling").removeEventListener(eventListener);



/*************************      cancel call from receiver side      ******************************/

        receiverEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&& dataSnapshot.hasChild("ringing")){
                    callerID=dataSnapshot.child("ringing").getValue().toString();


                    final HashMap<String,Object> cancelingInfo=new HashMap<>();
                    cancelingInfo.put("calling","cancel");

                    userRef.child(callerID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            userRef.child(currentUserID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    ///////////////////////////////////////////////////////////////////////////////////////

                                    //removeActivityOfCaller(callerID);

                                    ////////////////////////////////////////////////////////////////////////////////////////

                                    userRef.child(currentUserID).child("Ringing").removeEventListener(receiverEventListener);
                                    //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    });
                }
                else {
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userRef.child(currentUserID).child("Ringing").addValueEventListener(receiverEventListener);


    }


    private void initialization() {
        userName=findViewById(R.id.calling_username_tv);
        callingUserImage=findViewById(R.id.calling_image);
        cancleCall=findViewById(R.id.cancel_call);
        acceptCall=findViewById(R.id.make_call);
    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverUserID).exists()){
                    //receiverUserImage=dataSnapshot.child(receiverUserID).child("").getValue().toString();
                    receiverUsername=dataSnapshot.child(receiverUserID).child("username").getValue().toString();
                    userName.setText(receiverUsername);
                }
                if (dataSnapshot.child(currentUserID).exists()){
                    //senderUserImage=dataSnapshot.child(senderUserID).child("").getValue().toString();
                    senderUsername=dataSnapshot.child(currentUserID).child("username").getValue().toString();
                    //userName.setText(senderUsername);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        //ValueEventListener eventListenerCallChecker=new

        /*** make sure the user has receiving a call*/
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentUserID).hasChild("Ringing") && !dataSnapshot.child(currentUserID).hasChild("Calling")){
                    mediaPlayer.start();
                    acceptCall.setVisibility(View.VISIBLE);
                }



                /*** carry a user (who is make a call) to video chat activity when answer the call. */
                if (dataSnapshot.child(receiverUserID).child("Ringing").hasChild("picked")){
                    mediaPlayer.stop();
                    Intent intent=new Intent(getApplicationContext(),VideoChatActivity.class);
                    startActivity(intent);
                    finish();
                }

                if(dataSnapshot.child(currentUserID).child("Calling").hasChild("cancel")){
                    mediaPlayer.stop();

                    userRef.child(currentUserID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                }else if (dataSnapshot.child(currentUserID).child("Ringing").hasChild("cancel")){
                    mediaPlayer.stop();
                    userRef.child(currentUserID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }


                /*** for carry the remote user to main activity when cancel the call by other side user*/
                /*if (dataSnapshot.child(currentUserID).hasChild("Cancel") && !dataSnapshot.child(currentUserID).hasChild("Calling") && !dataSnapshot.child(currentUserID).hasChild("Ringing")){
                    //mediaPlayer.stop();

                    userRef.child(currentUserID).child("Cancel").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //mediaPlayer.start();
        /** check receiver busy or not & update calling database**/
        userRef.child(receiverUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(VideoCallingActivity.this,checker+" busy",Toast.LENGTH_SHORT).show();
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling" )&& !dataSnapshot.hasChild("Ringing")){
                    final HashMap<String,Object> callingInfo=new HashMap<>();
                    /* callingInfo.put("uid",senderUserID);
                    callingInfo.put("name",senderUsername);
                    callingInfo.put("image",senderUserImage);*/
                    callingInfo.put("calling",receiverUserID);


                    userRef.child(currentUserID).child("Calling").updateChildren(callingInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        final HashMap<String,Object>ringingInfo=new HashMap<>();
                                       /* ringingInfo.put("uid",receiverUserID);
                                        ringingInfo.put("name",receiverUsername);
                                        ringingInfo.put("image",receiverUsername);*/
                                        ringingInfo.put("ringing",currentUserID);

                                        userRef.child(receiverUserID).child("Ringing").updateChildren(ringingInfo);
                                        //receiverUserID=null;


                                    }
                                }
                            });

                }else {
                    //finishActivity(1);

                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();

                    /**
                     * You opened the new activity from another activity with startActivityForResult.
                     * In that case you can just call the finishActivity() function from your code and it'll take you back to the previous activity.
                     *
                     * Keep track of the activity stack. Whenever you start a new activity with an intent you can specify an intent flag like
                     * FLAG_ACTIVITY_REORDER_TO_FRONT or FLAG_ACTIVITY_PREVIOUS_IS_TOP.
                     * You can use this to shuffle between the activities in your application.
                     * Haven't used them much though. Have a look at the flags here:
                     *
                     * **/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeActivityOfCaller(final String callerID) {

        userRef.child(callerID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final HashMap<String,Object> cancleCallingInfo=new HashMap<>();
                cancleCallingInfo.put("status","canceled");
                userRef.child(callerID).child("Cancel").updateChildren(cancleCallingInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void removeActivityOfRinger(final String ringingID) {
        userRef.child(ringingID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final HashMap<String,Object> cancleCallingInfo=new HashMap<>();
                cancleCallingInfo.put("status","canceled");
                userRef.child(ringingID).child("Cancel").updateChildren(cancleCallingInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cancelCalling();
        //checker="";
    }

    @Override
    protected void onStop() {
        super.onStop();
        //checker="";
        //userRef.child(currentUserID).child("Calling").removeEventListener(eventListener);
    }

    @Override
    public boolean isActivityTransitionRunning() {
        return super.isActivityTransitionRunning();
    }
}