package com.brightpattern.mobileagent;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.widget.TextView;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.util.HashMap;

public class MainViewController extends ReactContextBaseJavaModule {
    private static HashMap<String, Dialog> _splashDialogs = new HashMap<>();
    private static Activity _activity;
    private static boolean appIsMounted = false;

    MainViewController (ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName () {
        return "MainViewController";
    }

    public static boolean isMounted () {
        return appIsMounted;
    }

    public static void setup (Activity activity) {
        _activity = activity;
    }

    public static void showSplash (String name, String text) {
        if (_activity == null) return;

        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!_activity.isFinishing()) {
                    Dialog splashDialog = new Dialog(_activity, R.style.SplashTheme);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        splashDialog.setContentView(R.layout.splash_v31);
                    } else {
                        splashDialog.setContentView(R.layout.splash);
                    }
                    splashDialog.setCancelable(false);

                    TextView textView = (TextView) splashDialog.findViewById(R.id.text);
                    textView.setText(text);

                    if (!splashDialog.isShowing()) {
                        splashDialog.show();
                        WindowFlags.setup(splashDialog);
                    }

                    _splashDialogs.put(name, splashDialog);
                }
            }
        });
    }

    @ReactMethod
    public void setAppIsMounted (boolean isMounted) {
        appIsMounted = isMounted;
    }

    @ReactMethod
    public void isShowing (String name, Callback callback) {
        Dialog splashDialog = _splashDialogs.get(name);
        callback.invoke(splashDialog != null && splashDialog.isShowing());
    }

    @ReactMethod
    public void changeText (String name, String text) {
        Dialog splashDialog = _splashDialogs.get(name);
        TextView textView = (TextView) splashDialog.findViewById(R.id.text);
        textView.setText(text);
    }

    @ReactMethod
    public void splash (String name, String text) {
        MainViewController.showSplash(name, text);
    }

    @ReactMethod
    public void hideSplash (String name) {
        if (_activity == null) return;

        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog splashDialog = _splashDialogs.get(name);

                if (splashDialog != null && splashDialog.isShowing()) {
                    if (!_activity.isFinishing() && !_activity.isDestroyed()) {
                        splashDialog.dismiss();
                    }
                    _splashDialogs.remove(name);
                }
            }
        });
    }
}
