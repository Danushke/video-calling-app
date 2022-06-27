package com.desirecode.videocallingapp.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.desirecode.videocallingapp.MainActivity;
import com.desirecode.videocallingapp.R;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID="Notification_System";
    private static final String CHANNEL_NAME="Time Table Notification";
    private static final String CHANNEL_DESC="Notifications";


    public static void displayNotification(Context context,String title,String body){

        Intent intent=new Intent(context, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(
                context,100,intent,PendingIntent.FLAG_CANCEL_CURRENT
        );

        NotificationCompat.Builder mBuilder=new
                NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.online_icon_50px)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)//cancel the notification when you clicked
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1,mBuilder.build());
    }
}
