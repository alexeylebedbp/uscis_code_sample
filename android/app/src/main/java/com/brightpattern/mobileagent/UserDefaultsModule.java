package com.brightpattern.mobileagent;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class UserDefaultsModule extends ReactContextBaseJavaModule {
    public static final String APP_PREFERENCES = "group.com.brightpattern.mobile";

    private static ReactApplicationContext reactContext;

    UserDefaultsModule (ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName () {
        return "UserDefaultsModule";
    }

    @ReactMethod
    public static void saveToUserDefaults (String key, String value) {
        SharedPreferences sharedPreferences = reactContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getUserDefaults (String key) {
        SharedPreferences sharedPreferences = reactContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }
}
