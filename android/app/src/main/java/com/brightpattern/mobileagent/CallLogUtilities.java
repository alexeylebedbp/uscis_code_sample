package com.brightpattern.mobileagent;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.CallLog.Calls;

import androidx.core.app.ActivityCompat;

public class CallLogUtilities {
    public static void addCallToLog(ContentResolver contentResolver, String number, long duration, int type, long time,Context androidContext) {
        ContentValues values = new ContentValues();
        values.put(Calls.NUMBER, number);
        values.put(Calls.DATE, time);
        values.put(Calls.DURATION, duration);
        values.put(Calls.TYPE, type);
        values.put(Calls.NEW, 1);
        values.put(Calls.CACHED_NAME, "");
        values.put(Calls.CACHED_NUMBER_TYPE, 0);
        values.put(Calls.CACHED_NUMBER_LABEL, "");
        if (ActivityCompat.checkSelfPermission(androidContext, android.Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        contentResolver.insert(Calls.CONTENT_URI, values);
    }
}

