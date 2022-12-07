package com.brightpattern.mobileagent;

import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

// Attempting to Detect Screen on or off
public class LockModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;

    LockModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }
    @Override
    public String getName() {
        return "LockDetection";
    }


    @ReactMethod
    public void registerforDeviceLockNotif() {}

    @ReactMethod
    public void invokeApp() {
        Intent reactIntent = new Intent(reactContext, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("runApplication", true);

        reactContext.startActivity(reactIntent);
    }
}
