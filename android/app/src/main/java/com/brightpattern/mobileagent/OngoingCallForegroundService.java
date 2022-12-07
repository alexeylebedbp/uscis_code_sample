package com.brightpattern.mobileagent;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class OngoingCallForegroundService extends Service {
    private static final String CHANNEL_DEFAULT_IMPORTANCE = "OngoingCall";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        String title = "Ongoing call";

        String name = intent.getStringExtra("name");
        if (name == null || name.equals("")) name = "Bright Pattern";

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(name)
                .setTicker(title)
                .setSilent(true)
                .build();

        this.startForeground(IncomingCall.ONGOING_CALL_NOTIFICATION_ID, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
