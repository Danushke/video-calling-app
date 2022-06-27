package com.desirecode.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.Publisher;

public class VideoChatActivity extends AppCompatActivity
        implements Session.SessionListener,
        PublisherKit.PublisherListener {

    private static String API_Key="47048864";
    private static String SESSION_ID="1_MX40NzA0ODg2NH5-MTYwODEzMjk3MzYxOX5EU0xzQ09xN0xld0FkcjZ4bTdyK1p4T3J-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00NzA0ODg2NCZzaWc9ODI3ZDU0YmZkNDQ1NTI2YzIyNWZlNmZiYjk1ZDljMTVhMTBmZDNhNDpzZXNzaW9uX2lkPTFfTVg0ME56QTBPRGcyTkg1LU1UWXdPREV6TWprM016WXhPWDVFVTB4elEwOXhOMHhsZDBGa2NqWjRiVGR5SzFwNFQzSi1mZyZjcmVhdGVfdGltZT0xNjA4MTMzMTM1Jm5vbmNlPTAuNDk3OTg3MzQ2NTYwODY0MiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjEwNzI1MTMyJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private static final int RC_SETTING_SCREEN_PERM = 123;

    private ImageView videoChatCloseImageButton;
    private DatabaseReference usersRef;
    private String userID;

    private FrameLayout mPublisherViewController,mSubscriberViewController;
    private Session mSession;
    private com.opentok.android.Publisher mPublisher;
    private Subscriber mSubscriber;


    //45c1d438d22f096d2bd59013342acec2ed44998b

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        videoChatCloseImageButton = findViewById(R.id.video_Chat_close_img_btn);
        videoChatCloseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing")){
                            usersRef.child(userID).child("Ringing").removeValue();

                            if (mPublisher!=null)
                                mPublisher.destroy();
                            if (mSubscriber!=null)
                                mSubscriber.destroy();

                            startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                            finish();
                        }
                        if (dataSnapshot.child(userID).hasChild("Calling")){
                            usersRef.child(userID).child("Calling").removeValue();

                            if (mPublisher!=null)
                                mPublisher.destroy();
                            if (mSubscriber!=null)
                                mSubscriber.destroy();

                            startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                            finish();
                        }
                        else {
                            startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }


    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String[] perms={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this,perms)){
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //initialize and connect the session
            mSession= new Session.Builder(this,API_Key,SESSION_ID).build();
            //mSession = new com.opentok.android.Session(this,API_Key,SESSION_ID);
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }else {
            EasyPermissions.requestPermissions(this,"please allow the required permission permissions",RC_VIDEO_APP_PERM);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(com.opentok.android.Session session) {
        Log.i(LOG_TAG,"Session connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mSubscriberViewController.addView(mPublisher.getView());

        if (mPublisher.getView()instanceof GLSurfaceView){
            ((GLSurfaceView)mPublisher.getView()).setZOrderOnTop(true);
        }
mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");

    }

    @Override
    public void onStreamReceived(com.opentok.android.Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");
        if (mSubscriber==null){
            mSubscriber = new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(com.opentok.android.Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");
        if (mSubscriber!=null){
            mSubscriber=null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(com.opentok.android.Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }



    /********************************************************************************************/

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onREsume");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}