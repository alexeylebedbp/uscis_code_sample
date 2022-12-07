package com.brightpattern.mobileagent;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class WidgetHelper extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;

    @Override
    public String getName() {
        return "WidgetModule";
    }

    public WidgetHelper(ReactApplicationContext context){
        super(context);
        reactContext=context;
    }

    @ReactMethod
    public void update(){
        Intent intent = new Intent(reactContext, AgentStatus.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(reactContext).getAppWidgetIds(new ComponentName(reactContext, AgentStatus.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        reactContext.sendBroadcast(intent);
    }

}
