package com.desirecode.videocallingapp.notification;

import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification()!=null){
            String title=remoteMessage.getNotification().getTitle();
            String body=remoteMessage.getNotification().getBody();

            NotificationHelper.displayNotification(getApplicationContext(),title,body);
        }
    }
}
