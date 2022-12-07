package com.brightpattern.mobileagent;

import android.content.Intent;
import android.content.pm.PackageManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

// Attempting to Detect Screen on or off
public class TelephonyAvailableModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;

    TelephonyAvailableModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }
    @Override
    public String getName() {
        return "TelephonyAvaliableModule";
    }


    @ReactMethod
    public void hasTelephony(Callback booleanCallback) {
        PackageManager mgr = reactContext.getPackageManager();
        boolean hasTelephony = mgr.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        booleanCallback.invoke(hasTelephony);
    }

}
