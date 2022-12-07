package com.brightpattern.mobileagent;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class WindowFlags {
    private static final String LOG_TAG = "WindowFlags";

    public static void setup(Activity activity) {
        Log.i(LOG_TAG, "setup activity");

        if (Build.VERSION.SDK_INT >= 26) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
            activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }
    }

    public static void setup(Dialog dialog) {
        Log.i(LOG_TAG, "setup dialog");

        if (Build.VERSION.SDK_INT >= 26) {
            dialog.getWindow().setStatusBarColor(Color.TRANSPARENT);
            dialog.getWindow().setNavigationBarColor(Color.TRANSPARENT);
            dialog.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }
    }

    public static void showOnLockedScreen(Activity activity) {
        Log.i(LOG_TAG, "showOnLockedScreen");

        Window window = activity.getWindow();

        window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );
    }

    public static void hideOnLockedScreen(Activity activity) {
        Log.i(LOG_TAG, "hideOnLockedScreen");

        Window window = activity.getWindow();

        window.clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );
    }
}
