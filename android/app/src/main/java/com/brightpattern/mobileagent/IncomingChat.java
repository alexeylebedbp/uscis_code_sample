package com.brightpattern.mobileagent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class IncomingChat extends ReactContextBaseJavaModule {
    private static final String LOG_TAG = "IncomingChat";
    public static final int FIRING_ALARM_NOTIFICATION_ID = 123095;

    public static ReactApplicationContext reactContext;

    public IncomingChat(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        LocalBroadcastManager.getInstance(context).registerReceiver(uiResult, new IntentFilter("data"));
    }

    @Override
    public String getName() {
        return "IncomingChat";
    }

    @ReactMethod
    public static void showInteraction(Context context, String uuid, String title, String service, int totalQueueTime) {
        if (isAppOnForeground(context)) return;

        Intent intent = new Intent(context, IncomingChatUi.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("uuid", uuid)
                .putExtra("title", title)
                .putExtra("service", service)
                .putExtra("totalQueueTime", totalQueueTime);

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            displayInitialMsgNotification(context, intent);
        } else {
            context.startActivity(intent);
        }
    }

    @ReactMethod
    public static void closeInteraction() {
        NotificationManager notificationManager = (NotificationManager) reactContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(FIRING_ALARM_NOTIFICATION_ID);

        Intent intent = new Intent("data");
        intent.putExtra("IncomingChatUiEndImmediately", true);
        LocalBroadcastManager.getInstance(reactContext).sendBroadcast(intent);
    }

    private static RemoteViews getButton(Context context, Intent intent, String action, String text, String color, boolean invoke) {
        Intent actionIntent = new Intent(context, IncomingChatReceiver.class)
            .putExtras(intent)
            .putExtra("invoke", invoke)
            .setAction(action);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent,
                               PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews buttonRemoteView = new RemoteViews(context.getPackageName(), R.layout.default_notification_button);
        buttonRemoteView.setTextViewText(R.id.button, text.toUpperCase());
        buttonRemoteView.setOnClickPendingIntent(R.id.button, pendingIntent);

        if (color != null) {
            buttonRemoteView.setTextColor(R.id.button, Color.parseColor(color));
        }

        return buttonRemoteView;
    }

    public static void displayInitialMsgNotification(Context context, Intent intent) {
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "InitialNotificationsNew")
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setSmallIcon(R.drawable.ic_notification)
                .setCategory(Notification.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(fullScreenPendingIntent);

        String title = intent.getStringExtra("title");
        String service = intent.getStringExtra("service");
        int totalQueueTime = intent.getIntExtra("totalQueueTime", 0);

        title = getNameFromTitle(title);

        RemoteViews customRemoteView = new RemoteViews(context.getPackageName(), R.layout.default_notification);
        customRemoteView.setTextViewText(R.id.app_name, getApplicationName(context));
        customRemoteView.setTextViewText(R.id.title, title);
        if (service != null && !service.equals("")) {
            customRemoteView.setViewVisibility(R.id.service_wrapper, View.VISIBLE);
            customRemoteView.setTextViewText(R.id.service, service);
        } else {
            customRemoteView.setViewVisibility(R.id.service_wrapper, View.GONE);
        }
        customRemoteView.setTextViewText(R.id.body, "New Chat Session. Wait time: " + IncomingCallUtils.getWaitTime(totalQueueTime));

        customRemoteView.setViewVisibility(R.id.actions, View.VISIBLE);

        customRemoteView.addView(R.id.actions, getButton(context, intent, "acceptAndOpen", "Accept and Open", null, true));
        customRemoteView.addView(R.id.actions, getButton(context, intent, "accept", "Accept", null, true));
        customRemoteView.addView(R.id.actions, getButton(context, intent, "decline", "Decline", "#FF0000", false));

        notificationBuilder.setCustomContentView(customRemoteView);
                
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_INSISTENT;
        notificationManager.notify(IncomingChat.FIRING_ALARM_NOTIFICATION_ID, notification);
    }

    public static String getNameFromTitle(String string) {
        String name = string.trim();

        String[] splittedName = name.split("-");
        if (splittedName.length > 0) {
            if (splittedName[0].trim().equals("Pending Chat Session") || splittedName[0].trim().equals("New Chat Session")) {
                name = TextUtils.join("-", Arrays.copyOfRange(splittedName, 1, splittedName.length)).trim();
            }
        }

        return name;
    }

    private BroadcastReceiver uiResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("sendChatEventToJS", false)) {
                String uuid = intent.getStringExtra("uuid");
                String action = intent.getStringExtra("action");

                if (uuid == null || uuid.equals("")) return;

                WritableMap data = Arguments.createMap();
                data.putString("uuid", uuid);
                data.putString("action", action);

                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("chatAcceptedResult", data);
            }
        }
    };

    private static boolean isAppOnForeground(Context context) {
        boolean isAppOnForeground = false;
        try {
            isAppOnForeground = new ForegroundCheckTask().execute(context).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return isAppOnForeground;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
