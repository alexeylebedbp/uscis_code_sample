package com.brightpattern.mobileagent;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

public class NotificationChannels {

    public static void createChannels(Context context) {
        createIncomingCallOngoingChannel(context);
        createIncomingCallChannel(context);
        createInitialNotificationChannel(context);
        createExternalNotificationChannel(context);
        createInternalNotificationChannel(context);
        createMissedNotificationChannel(context);
        createUpdateNotificationChannel(context);
        createOtherNotificationChannel(context);
        createNetworkStateNotificationChannel(context);

        deleteChannels(context, new String[] {"InitialNotifications"});
    }

    private static void createChannel(Context context, NotificationChannel channel) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private static void createIncomingCallOngoingChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Ongoing Call";
        String description = "Provides access the microphone during the call";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(
                "OngoingCall",
                name,
                importance
        );
        channel.setDescription(description);
        createChannel(context, channel);
    }

    private static void createIncomingCallChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Incoming Calls";
        String description = "Provides notifications when call is received";
        int importance = NotificationManager.IMPORTANCE_MAX;

        @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(
                "IncomingCalls",
                name,
                importance
        );
        channel.setDescription(description);

        long[] vibrate = { 1000, 1000, 1000, 1000 };
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build();
        Uri ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        channel.setSound(ringtoneURI, attributes);
        channel.enableVibration(true);
        channel.setVibrationPattern(vibrate);

        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);

        createChannel(context, channel);
    }

    private static void createInitialNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Initial notifications";
        String description = "Provides notifications of new chat sessions";
        int importance = NotificationManager.IMPORTANCE_MAX;

        NotificationChannel channel = new NotificationChannel(
                "InitialNotificationsNew",
                name,
                importance
        );
        
        channel.setDescription(description);

        long[] vibrate = { 1000, 1000, 1000, 1000 };
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build();
        Uri ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        channel.setSound(ringtoneURI, attributes);
        channel.enableVibration(true);
        channel.setVibrationPattern(vibrate);

        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);

        createChannel(context, channel);
    }

    private static void createExternalNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "External notifications";
        String description = "Provides notifications of external messages";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(
                "ExternalNotifications",
                name,
                importance
        );
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);
        createChannel(context, channel);
    }

    private static void createInternalNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Internal notifications";
        String description = "Provides notifications of internal messages";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(
                "InternalNotifications",
                name,
                importance
        );
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);
        createChannel(context, channel);
    }

    private static void createMissedNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Missed notifications";
        String description = "Provides notifications of missed calls and chats";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(
                "MissedNotifications",
                name,
                importance
        );
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);
        createChannel(context, channel);
    }

    private static void createUpdateNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Update notifications";
        String description = "Provides notifications of updating your app";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(
                "UpdateNotifications",
                name,
                importance
        );
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);
        createChannel(context, channel);
    }

    private static void createOtherNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Other notifications";
        String description = "Provides all other notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(
                "OtherNotifications",
                name,
                importance
        );
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);
        createChannel(context, channel);
    }

    private static void createNetworkStateNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        CharSequence name = "Network state notifications";
        String description = "Provides notifications of network quality";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(
                "NetworkStateNotifications",
                name,
                importance
        );
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);
        createChannel(context, channel);
    }

    private static void deleteChannels (Context context, String[] channels) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return;

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        for (int i = 0; i < channels.length; i++) {
            String channel = channels[i];

            notificationManager.deleteNotificationChannel(channel);
        }
    }
}
