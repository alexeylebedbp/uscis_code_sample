package com.brightpattern.mobileagent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class CallModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private static String lastFakeCallId;

    CallModule (ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName () {
        return "CallModule";
    }
    
    public static String getLastFakeCallId () {
        return lastFakeCallId;
    }

    @ReactMethod
    public void fakeCallShown (String callId) {
        lastFakeCallId = callId;
    }
}
