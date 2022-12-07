package com.brightpattern.mobileagent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.reactnativecommunity.asyncstorage.AsyncLocalStorageUtil;
import com.reactnativecommunity.asyncstorage.ReactDatabaseSupplier;

import org.json.JSONException;
import org.json.JSONObject;

public class Storage {
  public static JSONObject getStorage (Context context, String storageName) {
      SQLiteDatabase readableDatabase = null;
      readableDatabase = ReactDatabaseSupplier.getInstance(context).getReadableDatabase();

      if (readableDatabase == null) return null;

      String storage = AsyncLocalStorageUtil.getItemImpl(readableDatabase, storageName);

      if (storage == null) return null;

      JSONObject storageJSON = null;

      try {
         storageJSON = new JSONObject(storage);
      } catch (JSONException e) {
          e.printStackTrace();
      }

      return storageJSON;
  }
}
