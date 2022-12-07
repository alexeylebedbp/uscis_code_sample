package com.brightpattern.mobileagent;

import org.json.JSONException;

public class JSONUtils {
  public static String getStringFromJSONObject (String fieldName, org.json.JSONObject jsonObject) {
    String field = null;

    if (jsonObject != null) {
      if (jsonObject.has(fieldName)) {
        try {
          field = jsonObject.getString(fieldName);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }

    return field;
  }
}
