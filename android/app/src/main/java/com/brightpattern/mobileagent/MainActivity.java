package com.brightpattern.mobileagent;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.window.SplashScreenView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;

public class MainActivity extends ReactActivity {
    private static final String LOG_TAG = "MainActivity";

    @Override
    protected String getMainComponentName() {
        return "mob";
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("runAppByChat", false)) {
            Intent dataIntent = new Intent("data")
                    .putExtras(intent.getExtras())
                    .putExtra("sendChatEventToJS", true);

            LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);

            WindowFlags.showOnLockedScreen(this);
        }

        if (intent.getBooleanExtra("runApplication", false)) {
            Intent dataIntent = new Intent("data")
                    .putExtras(intent.getExtras())
                    .putExtra("sendEventsToJS", true);

            LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);

            if (intent.getBooleanExtra("accepted", false)) {
                if (!intent.getBooleanExtra("isAppOnForeground", false) || isPhoneLocked()) {
                    if (!MainViewController.isMounted()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            MainViewController.showSplash("splash", "");
                        } else {
                            MainViewController.showSplash("splash", "Building the app...");
                        }
                    }
                }
            }

            WindowFlags.showOnLockedScreen(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getExtras() != null) Log.d(LOG_TAG, "onCreate:Extras: " + getIntent().getExtras());

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("data"));

        setTheme(R.style.AppTheme);
        WindowFlags.setup(this);

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 31) {
            this.getSplashScreen().setOnExitAnimationListener(SplashScreenView::remove);
        }

        NotificationChannels.createChannels(this);

        if (
            getIntent().getBooleanExtra("runApplication", false) ||
            getIntent().getBooleanExtra("runAppByChat", false)
        ) {
            WindowFlags.showOnLockedScreen(this);
        }

        MainViewController.setup(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MainViewController.showSplash("splash", "");
        } else {
            MainViewController.showSplash("splash", "Building the app...");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        IncomingCall.destroyOngoingCallForegroundService();
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected Bundle getLaunchOptions() {
                Bundle extras = new Bundle();
                Bundle bundle = getIntent().getExtras();

                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        if (key.equals("accepted"))          extras.putBoolean(key, bundle.getBoolean(key));
                        if (key.equals("runApplication"))    extras.putBoolean(key, bundle.getBoolean(key));
                        if (key.equals("isAppOnForeground")) extras.putBoolean(key, bundle.getBoolean(key));
                        if (key.equals("interactionType"))   extras.putString(key, bundle.getString(key));
                        if (key.equals("action"))            extras.putString(key, bundle.getString(key));
                        if (key.equals("phonenumber"))       extras.putString(key, bundle.getString(key));
                        if (key.equals("name"))              extras.putString(key, bundle.getString(key));
                        if (key.equals("uuid"))              extras.putString(key, bundle.getString(key));
                        if (key.equals("notificationData"))  extras.putString(key, bundle.getString(key));
                    }
                }

                return extras;
            }
        };
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("toggleFlags", false)) {
                if (intent.getBooleanExtra("setFlags", false)) {
                    WindowFlags.showOnLockedScreen(MainActivity.this);
                } else {
                    WindowFlags.hideOnLockedScreen(MainActivity.this);
                }
            }
        }
    };

    private boolean isPhoneLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }
}
