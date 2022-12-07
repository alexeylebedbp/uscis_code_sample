package com.brightpattern.mobileagent;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;

public class IncomingCallUtils {
    private static final String LOG_TAG = "IncomingCallUtils";
    private static int counter;
    private static long timeDiff;

    public static void displayCallUiNotification(Context context, Intent reactIntent) {
        Log.i(LOG_TAG, "displayCallUiNotification");

        String name = reactIntent.getStringExtra("name");
        String phoneNumber = reactIntent.getStringExtra("phonenumber");
        String userId = reactIntent.getStringExtra("userId");
        String service = reactIntent.getStringExtra("service");

        String displayName = getDisplayName(name, phoneNumber, "Unknown");

        Intent dismissIntent = IncomingCallUtils.getCallResultIntent(context, reactIntent, "dismiss", false);
        Intent answerIntent = IncomingCallUtils.getCallResultIntent(context, reactIntent, "answer", true);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0, reactIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent answerPendingIntent = PendingIntent.getBroadcast(context, 0, answerIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews customRemoteView = new RemoteViews(context.getPackageName(), R.layout.notification);
        customRemoteView.setTextViewText(R.id.name, displayName);
        if (service != null && !service.equals("")) {
            customRemoteView.setViewVisibility(R.id.service, View.VISIBLE);
            customRemoteView.setTextViewText(R.id.service, service);
        } else {
            customRemoteView.setViewVisibility(R.id.service, View.GONE);
        }
        customRemoteView.setOnClickPendingIntent(R.id.hangup, dismissPendingIntent);
        customRemoteView.setOnClickPendingIntent(R.id.answer, answerPendingIntent);
        RemoteViews customContentView = new RemoteViews(customRemoteView, customRemoteView);

        if (userId.isEmpty()) {
            long totalQueueTime = reactIntent.getIntExtra("totalQueueTime", 0);
            customRemoteView.setTextViewText(R.id.waitTime, ". Wait time: " + getWaitTime(totalQueueTime));
        }

        final NotificationCompat.Builder notificationBuilder = buildNotification(context, customContentView, fullScreenPendingIntent);
        showNotification(context, notificationBuilder);

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && userId.isEmpty()) {
        //     long totalQueueTime = reactIntent.getIntExtra("totalQueueTime", 0);

        //     counter = 1;

        //     String finalName = name;
        //     long startTimeMillis = System.currentTimeMillis();

        //     Timer timer = new Timer();
        //     timer.scheduleAtFixedRate(new TimerTask() {
        //         @Override
        //         public void run() {
        //             customRemoteView.setTextViewText(R.id.name, finalName);

        //             if (counter % 3 == 0) {
        //                 // timeDiff = System.currentTimeMillis() - startTimeMillis;
        //                 customRemoteView.setTextViewText(R.id.name, "Wait time: " + getWaitTime(totalQueueTime));
        //             }

        //             RemoteViews customContentView = new RemoteViews(customRemoteView, customRemoteView);

        //             NotificationCompat.Builder notificationBuilder = buildNotification(context, customContentView, fullScreenPendingIntent);

        //             if (!isNotificationInStatusBar(context, IncomingCall.FIRING_ALARM_NOTIFICATION_ID)) {
        //                 timer.cancel();
        //                 return;
        //             }

        //             showNotification(context, notificationBuilder);

        //             counter++;
        //         }
        //     }, 1000, 1000);
        // }
    }

    public static Intent getCallResultIntent(Context context, Intent reactIntent, String action, Boolean accepted) {
        String name = reactIntent.getStringExtra("name");
        String firstName = reactIntent.getStringExtra("firstName");
        String lastName = reactIntent.getStringExtra("lastName");
        String uuid = reactIntent.getStringExtra("uuid");
        String phoneNumber = reactIntent.getStringExtra("phonenumber");
        String notificationData = reactIntent.getStringExtra("notificationData");

        return new Intent(context, IncomingCallReceiver.class)
                .setAction(action)
                .putExtra("accepted", accepted)
                .putExtra("uuid", uuid)
                .putExtra("name", name)
                .putExtra("firstName", firstName)
                .putExtra("lastName", lastName)
                .putExtra("notificationData", notificationData)
                .putExtra("phoneNumber", phoneNumber);
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static NotificationCompat.Builder buildNotification(Context context, RemoteViews customContentView, PendingIntent fullScreenPendingIntent) {
        return new NotificationCompat.Builder(context, "IncomingCalls")
                .setCustomContentView(customContentView)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setSmallIcon(R.drawable.ic_notification)
                .setCategory(Notification.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(fullScreenPendingIntent);
    }

    private static NotificationCompat.Builder updateNotificationName(RemoteViews customContentView, NotificationCompat.Builder builder, String text) {
        customContentView.setTextViewText(R.id.name, text);
        builder.setCustomContentView(customContentView);

        return builder;
    }

    private static void showNotification(Context context, NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_INSISTENT;
        notificationManager.notify(IncomingCall.FIRING_ALARM_NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isNotificationInStatusBar(Context context, int notificationId) {
        NotificationManager notificationService = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        for (StatusBarNotification notification : notificationService.getActiveNotifications()) {
            if (notification.getId() == notificationId) {
                return true;
            }
        }

        return false;
    }

    public static String getWaitTime(long totalQueueTime) {
        long totalQueueTimeSeconds = totalQueueTime / 1000;
        return String.format("%2d:%02d", (totalQueueTimeSeconds % 3600) / 60, (totalQueueTimeSeconds % 60));
    }

    public static String getDisplayName(String name, String phoneNumber, String defaultText) {
        if (name == null || name.equals("")) {
            if (phoneNumber == null || phoneNumber.equals("") || phoneNumber.equals("Anonymous")) {
                return defaultText;
            } else {
                return PhoneNumberUtils.formatNumber(phoneNumber, "ET");
            }
        } else {
            return name;
        }
    }
}
