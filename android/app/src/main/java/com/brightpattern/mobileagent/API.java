package com.brightpattern.mobileagent;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class API {
  public static void endCall (Context context, String callId) {
    endCall(context, callId, true);
  }

  public static void endCall (Context context, String callId, Boolean closeOnComplete) {
    JSONObject localSettingsJSON = Storage.getStorage(context, "@bpinc/ad-local-settings-global");

    String sessionId = JSONUtils.getStringFromJSONObject("sessionId", localSettingsJSON);
    String serverOrigin = JSONUtils.getStringFromJSONObject("serverOrigin", localSettingsJSON);
    String tenantUrl = JSONUtils.getStringFromJSONObject("tenantUrl", localSettingsJSON);

    String serverUrl = serverOrigin.length() != 0 ? serverOrigin : tenantUrl;

    String url = "https://" + serverUrl + "/agentdesktop/mobile/agent_notification_result";

    MediaType mediaType = MediaType.parse("application/json");

    String jsonString = null;

    try {
        jsonString = new JSONObject()
                .put("action", "agent_notification_result")
                .put("session_id", sessionId)
                .put("item_id", callId)
                .put("rc", "1")
                .toString();
    } catch (JSONException e) {
        e.printStackTrace();
    }

    RequestBody body = RequestBody.create(mediaType, jsonString);
    Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Cookie", "X-BP-SESSION-ID=" + sessionId)
            .post(body)
            .build();
    new Thread(new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();

            try {
                Response response = client.newCall(request).execute();

                if (closeOnComplete) {
                     if (response.code() == 200) {
                        IncomingCall.closeNotification(IncomingCall.FIRING_ALARM_NOTIFICATION_ID);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    })
            .start();
  }
}
