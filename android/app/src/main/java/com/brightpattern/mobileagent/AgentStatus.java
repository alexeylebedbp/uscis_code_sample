package com.brightpattern.mobileagent;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import org.json.*;

/**
 * Implementation of App Widget functionality.
 */
public class AgentStatus extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        SharedPreferences preferences = context.getSharedPreferences("group.com.brightpattern.mobile", Context.MODE_PRIVATE);
        String jsonString = preferences.getString("widgetData", "");
        String acd_state="Unknown";
        String state_reason="";
        String title="";
        long dateSeconds= System.currentTimeMillis();

        try {
            JSONObject obj = new JSONObject(jsonString);
            acd_state = obj.optString("agentState");
            state_reason = obj.optString("reason");
            title = obj.optString("title");
            if(obj.has("timeLastStateChange") && !obj.isNull("timeLastStateChange")){
                dateSeconds = (long) obj.optDouble("timeLastStateChange")*1000;
            }else{
                obj.put("timeLastStateChange", dateSeconds /1000);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("group.com.brightpattern.mobile",obj.toString());
                editor.commit();
            }

        } catch (JSONException ignored) {}

        long CurrentTime=System.currentTimeMillis();
        long RealTime = SystemClock.elapsedRealtime();
        long elapsedTime = dateSeconds - CurrentTime + RealTime;
        AgentStatusUtils util = new AgentStatusUtils();

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.agent_status);
        views.setViewVisibility(R.id.agentstatus_agentState,View.VISIBLE);
        if(util.isIncomingInteraction(acd_state)){
            views.setViewVisibility(R.id.agentstatus_time,View.INVISIBLE);
        } else {
            views.setViewVisibility(R.id.agentstatus_time, View.VISIBLE);
        }
        views.setViewVisibility(R.id.agentstatus_imageView,View.VISIBLE);
        views.setTextViewText(R.id.agentstatus_agentState,
                util.formatText(util.determinTitleToDisplay(acd_state,title)));
        if(state_reason.length()==0 || util.isAgentState(state_reason)) {
            views.setViewVisibility(R.id.agentstatus_reason, View.GONE);
            views.setTextViewTextSize(R.id.agentstatus_placer, TypedValue.COMPLEX_UNIT_DIP,10);
            views.setViewVisibility(R.id.agentstatus_placer,View.VISIBLE);
        }else{
            views.setViewVisibility(R.id.agentstatus_reason, View.VISIBLE);
            views.setTextViewTextSize(R.id.agentstatus_placer, TypedValue.COMPLEX_UNIT_DIP,0);
            views.setTextViewText(R.id.agentstatus_reason, util.formatText(state_reason));
        }
        views.setImageViewResource(R.id.agentstatus_frameImage, R.drawable.agentstatus_background);
        views.setImageViewResource(R.id.agentstatus_imageView, util.getImage(acd_state));
        views.setChronometer(R.id.agentstatus_time,elapsedTime,null,true);
        views.setOnClickPendingIntent(R.id.agentStatus_widgetFrame,pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is create
        Intent intent = new Intent(context, AgentStatus.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, AgentStatus.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}