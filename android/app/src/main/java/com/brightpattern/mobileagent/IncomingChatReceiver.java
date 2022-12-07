package com.brightpattern.mobileagent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;

import java.util.concurrent.ExecutionException;

public class IncomingChatReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "IncomingChatReceiver";

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();

        boolean invoke = intent.getBooleanExtra("invoke", false);

        IncomingChat.closeInteraction();

        if (!invoke) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    // Construct and load our normal React JS code bundle
                    final ReactInstanceManager mReactInstanceManager = ((ReactApplication) context.getApplicationContext()).getReactNativeHost().getReactInstanceManager();
                    ReactContext context = mReactInstanceManager.getCurrentReactContext();
                    // If it's constructed, send a notification
                    if (context != null) {
                        sendJSEvent(context, action, intent);
                    } else {
                        // Otherwise wait for construction, then send the notification
                        mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                            public void onReactContextInitialized(ReactContext context) {
                                sendJSEvent(context, action, intent);
     
                                mReactInstanceManager.removeReactInstanceEventListener(this);
                            }
                        });
                        if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
                            // Construct it in the background
                            mReactInstanceManager.createReactContextInBackground();
                        }
                    }
                }
            });
        }

        if (invoke) {
            startIntent(context, action, intent);
        }
    }

    private void sendJSEvent(Context context, String action, Intent intent) {
        Intent dataIntent = new Intent("data")
                .putExtras(intent)
                .putExtra("interactionType", "chat")
                .putExtra("action", action)
                .putExtra("isAppOnForeground", isAppOnForeground(context))
                .putExtra("sendChatEventToJS", true);

        LocalBroadcastManager.getInstance(context).sendBroadcast(dataIntent);
    }

    private void startIntent(Context context, String action, Intent intent) {
        Intent reactIntent = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtras(intent)
                .putExtra("interactionType", "chat")
                .putExtra("action", action)
                .putExtra("isAppOnForeground", isAppOnForeground(context))
                .putExtra("runAppByChat", true);

        context.startActivity(reactIntent);
    }

    private boolean isAppOnForeground(Context context) {
        boolean isAppOnForeground = false;
        try {
            isAppOnForeground = new ForegroundCheckTask().execute(context).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return isAppOnForeground;
    }
}
