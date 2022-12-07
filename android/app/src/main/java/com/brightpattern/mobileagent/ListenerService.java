package com.brightpattern.mobileagent;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dieam.reactnativepushnotification.modules.RNPushNotificationListenerService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class ListenerService extends FirebaseMessagingService {
    private static final String LOG_TAG = "ListenerService";

    private final RNPushNotificationListenerService rnPushNotificationListenerService;

    private Context context;

    public ListenerService() {
        super();
        context = this;
        rnPushNotificationListenerService = new com.dieam.reactnativepushnotification.modules.RNPushNotificationListenerService(this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        rnPushNotificationListenerService.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        rnPushNotificationListenerService.onMessageReceived(message);

        JSONObject jsonObject = new JSONObject();

        for(Map.Entry<String, String> entry : message.getData().entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String category = null;
        try {
            category = jsonObject.getString("category");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String name = null;
        try {
            name = jsonObject.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String firstName = null;
        try {
            firstName = jsonObject.getString("caller_first_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String lastName = null;
        try {
            lastName = jsonObject.getString("caller_last_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String userId = null;
        try {
            userId = jsonObject.getString("caller_user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String number = null;
        try {
            number = jsonObject.getString("number");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uuid = null;
        try {
            uuid = jsonObject.getString("item_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int totalQueueTime = 0;
        try {
            totalQueueTime = jsonObject.getInt("total_queue_time");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String service = null;
        try {
            service = jsonObject.getString("service");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        long receivingTime = System.currentTimeMillis();

        if (category.equals("INITIAL_MSG")) {
            String title = null;
            try {
                title = jsonObject.getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            IncomingChat.showInteraction(context, uuid, title, service, totalQueueTime);
            return;
        }

        if (!category.equals("VOIP")) return;

        if (uuid.equals(CallModule.getLastFakeCallId())) {
            Log.i(LOG_TAG, "onMessageReceived VOIP notification is hidden because fake VOIP notification was shown. UUID = " + uuid);

            return;
        }

        String notificationData = jsonObject.toString();

        IncomingCall.showIncomingCall(context, name, firstName, lastName, userId, number, uuid, service, totalQueueTime, receivingTime, notificationData);
    }
}
