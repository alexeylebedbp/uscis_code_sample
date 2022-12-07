package com.brightpattern.mobileagent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.ExecutionException;

public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(IncomingCall.FIRING_ALARM_NOTIFICATION_ID);

        if (action.equals("dismiss")) {
            String uuid = intent.getStringExtra("uuid");

            IncomingCall.endCallUiNow(uuid);

            API.endCall(context, uuid);
        }

        if (action.equals("answer")) {
            Boolean accepted = intent.getBooleanExtra("accepted", false);
            String name = intent.getStringExtra("name");
            String uuid = intent.getStringExtra("uuid");
            String phoneNumber = intent.getStringExtra("phoneNumber");
            String notificationData = intent.getStringExtra("notificationData");

            boolean isAppOnForeground = false;
            try {
                isAppOnForeground = new ForegroundCheckTask().execute(context).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            Intent appIntent = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("isAppOnForeground", isAppOnForeground)
                .putExtra("accepted", accepted)
                .putExtra("uuid", uuid)
                .putExtra("name", name)
                .putExtra("phonenumber", phoneNumber)
                .putExtra("notificationData", notificationData)
                .putExtra("runApplication", true);

            context.startActivity(appIntent);
        }
    }
}
