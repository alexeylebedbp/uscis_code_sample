package com.brightpattern.mobileagent;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.UUID;

public class IncomingCall extends ReactContextBaseJavaModule {
    private static final String LOG_TAG = "IncomingCall";
    public static final int FIRING_ALARM_NOTIFICATION_ID = 123094;
    public static final int ONGOING_CALL_NOTIFICATION_ID = 123095;
    private static Intent ongoingCallForegroundService = null;

    public static ReactApplicationContext reactContext;
    private static int activeCallsCount = 0;
    private static String lastCallId;
    private static long receivingTime = 0;

    public IncomingCall(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        LocalBroadcastManager.getInstance(context).registerReceiver(uiResult, new IntentFilter("data"));
    }

    @Override
    public String getName() {
        return "IncomingCall";
    }

    @ReactMethod
    public static void showOngoingCallForegroundService(String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ongoingCallForegroundService = new Intent(reactContext, OngoingCallForegroundService.class)
                    .putExtra("name", name);
            reactContext.getApplicationContext().startForegroundService(ongoingCallForegroundService);
        }
    }

    @ReactMethod
    public static void destroyOngoingCallForegroundService() {
        if (ongoingCallForegroundService != null) {
            reactContext.getApplicationContext().stopService(ongoingCallForegroundService);
            ongoingCallForegroundService = null;
        }
    }

    private static void setLastCallId (String uuid) {
        lastCallId = uuid;
    }

    public static void showIncomingCall(Context context, String name, String firstName, String lastName, String userId, String number, String uuid, String service, int totalQueueTime, long receivingTimeArg, String data) {
        endPreviousCalls(context);
        setLastCallId(uuid);

        receivingTime = receivingTimeArg;

        Intent incomingCallIntent = new Intent(context, IncomingCallUi.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("name", name)
                .putExtra("firstName", firstName)
                .putExtra("lastName", lastName)
                .putExtra("userId", userId)
                .putExtra("phonenumber", number)
                .putExtra("uuid", uuid)
                .putExtra("totalQueueTime", totalQueueTime)
                .putExtra("receivingTime", receivingTime)
                .putExtra("notificationData", data)
                .putExtra("callNumber", activeCallsCount)
                .putExtra("service", service);

        showedCallUi(data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activeCallsCount == 0) {
            IncomingCallUtils.displayCallUiNotification(context, incomingCallIntent);
        } else {
            context.startActivity(incomingCallIntent);
        }
    }

    private static void showedCallUi(String notificationData) {
        if (notificationData == null) return;

        WritableMap data = Arguments.createMap();
        data.putString("notificationData", notificationData);

        try {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("androidCallShown", data);
        } catch (RuntimeException ignored) {}
    }

    private void acceptedResult(String item_id, boolean accepted) {
        if (item_id == null || item_id.equals("")) return;

        WritableMap data = Arguments.createMap();
        data.putString("callUUID", item_id);
        data.putBoolean("accepted", accepted);

        if (IncomingCallUtils.isAppOnForeground(reactContext)) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("callAcceptedResult", data);
        }
    }

    private static void endPreviousCalls(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        NotificationManager notificationManager = getNotificationManager(context);
        for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
            if (notification.getId() == FIRING_ALARM_NOTIFICATION_ID) {
                if (lastCallId != null) {
                    API.endCall(context, lastCallId, false);
                }
            }
        }
    }

    public static void closeNotification(int notificationId) {
        NotificationManager notificationManager = getNotificationManager(reactContext);
        notificationManager.cancel(notificationId);
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @ReactMethod
    public static void showOnLockedScreen(boolean enabled) {
        Intent intent = new Intent("data");
        intent.putExtra("toggleFlags", true);
        intent.putExtra("setFlags", enabled);

        LocalBroadcastManager.getInstance(reactContext).sendBroadcast(intent);
    }

    @ReactMethod
    public void scheduleNotificationAutoCancel(int id, int milliseconds){
        Runnable aRunnable = new Runnable() {
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.P) Thread.sleep(milliseconds);
                } catch (InterruptedException e) {}
                NotificationManager notificationManager = getNotificationManager(reactContext);
                notificationManager.cancel(id);
            }
        };

        String token = UUID.randomUUID().toString();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(aRunnable, token, milliseconds);
        } else {
            aRunnable.run();
        }
    }

    @ReactMethod
    public void cancelNotificationItemId(String itemId) {
        NotificationManager notificationManager = getNotificationManager(reactContext);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification:notifications) {
                Notification n = notification.getNotification();
                if((n.extras.getString("item_id").equals(itemId) || itemId.equals(""))
                        && n.extras.getString("category").equals("SESSION_MSG")) {
                    notificationManager.cancel(notification.getId());
                }
            }
        } else {
            notificationManager.cancelAll();
        }
    }

    @ReactMethod
    public void getCallCounter(Callback callback) {
        callback.invoke(activeCallsCount);
    }

    @ReactMethod
    public void setCallCounter(int count) {
        activeCallsCount = count;
        if (activeCallsCount < 0) activeCallsCount = 0;
    }

    @ReactMethod
    public static void endCallUiNow(String id) {
        if (!id.equals(lastCallId)) return;

        closeNotification(FIRING_ALARM_NOTIFICATION_ID);

        Intent intent = new Intent("data");
        intent.putExtra("IncomingCallUiEndImmediately","true");
        LocalBroadcastManager.getInstance(reactContext).sendBroadcast(intent);
    }

    @ReactMethod
    public void endCallPost(String uuid) {
        API.endCall(reactContext, uuid);
    }

    private BroadcastReceiver uiResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG,"uiResult extras: " + intent.getExtras());

            if (intent.getBooleanExtra("sendEventsToJS", false)) {
                boolean accepted = intent.getBooleanExtra("accepted", false);
                String uuid = intent.getStringExtra("uuid");
                String notificationData = intent.getStringExtra("notificationData");

                acceptedResult(uuid, accepted);
            }
        }
    };

    @ReactMethod
    public void getReceivingTime(Callback callback) {
        callback.invoke(String.valueOf(receivingTime));
    }
}
