package com.brightpattern.mobileagent;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class IncomingCallUi extends AppCompatActivity {
    private static final String LOG_TAG = "IncomingCallUI";

    private NotificationManager notificationManager;
    private AudioManager audioManager;
    private Ringtone ringtone;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(this).registerReceiver(endCallBrodcast, new IntentFilter("data"));

        Intent intent = getIntent();

        setupNotificationManager();
        setupAudioManager();

        int callNumber = intent.getIntExtra("callNumber", 1);

        if (callNumber > 0) {
            setContentView(R.layout.incoming_call_ui_multiple);

            String uuid = intent.getStringExtra("uuid");

            Button dismissAndAnswerButton = findViewById(R.id.dimissAndAnswer);
            dismissAndAnswerButton.setOnClickListener(v -> {
                endPreviousCall(uuid);
                sendResult(true, intent);
                stopRinging();
                finish();
            });
        } else {
            setContentView(R.layout.incoming_call_ui);
        }

        startRinging();

        Button answerButton = findViewById(R.id.answer);
        answerButton.setOnClickListener(v -> {
            sendResult(true, intent);
            finish();
        });

        Button hangupButton = findViewById(R.id.hangup);
        hangupButton.setOnClickListener(v -> {
            sendResult(false, intent);
            finish();
        });

        WindowFlags.showOnLockedScreen(this);
        WindowFlags.setup(this);
        muteAll(true);

        fillOutLayout(intent);

        super.onCreate(savedInstanceState);
    }

    private void fillOutLayout(Intent intent) {
        long startTimeMillis = System.currentTimeMillis();

        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String name = intent.getStringExtra("name");
        String phoneNumber = intent.getStringExtra("phonenumber");
        String service = intent.getStringExtra("service");
        long receivingTime = intent.getLongExtra("receivingTime", startTimeMillis);

        TextView callerInitialsTextView = findViewById(R.id.callerInitials);
        View callerAvatarView = findViewById(R.id.callerAvatar);
        TextView callerNameTextView = findViewById(R.id.callerName);
        TextView phoneNumberTextView = findViewById(R.id.phoneNumber);
        TextView serviceTextView = findViewById(R.id.service);
        TextView waitTimeTextView = findViewById(R.id.waitTime);

        if (firstName != null && firstName.length() > 0 && lastName != null && lastName.length() > 0) {
            callerInitialsTextView.setText(firstName.substring(0, 1) + "" + lastName.substring(0, 1));
            callerAvatarView.setVisibility(View.GONE);
        } else {
            callerInitialsTextView.setVisibility(View.GONE);
            callerAvatarView.setVisibility(View.VISIBLE);
        }

        callerNameTextView.setText(name);

        if (phoneNumber == null || phoneNumber.equals("") || phoneNumber.equals("Anonymous")) {
            phoneNumberTextView.setText("Unknown phone number");
        } else {
            phoneNumberTextView.setText(PhoneNumberUtils.formatNumber(phoneNumber, "ET"));
        }

        if (service != null && !service.equals("")) {
            serviceTextView.setText(service);
            serviceTextView.setVisibility(View.VISIBLE);
        } else {
            serviceTextView.setVisibility(View.GONE);
        }

        waitTimeTextView.setVisibility(View.VISIBLE);
        // long initialTimeDiff = startTimeMillis - receivingTime;
        long totalQueueTime = intent.getIntExtra("totalQueueTime", 0);
        waitTimeTextView.setText("Wait time: " + IncomingCallUtils.getWaitTime(totalQueueTime));

        // Timer timer = new Timer();
        // timer.scheduleAtFixedRate(new TimerTask() {
        //     @Override
        //     public void run() {
        //         long timeDiff = System.currentTimeMillis() - startTimeMillis;
        //         waitTimeTextView.setText("Wait time: " + IncomingCallUtils.getWaitTime(totalQueueTime));
        //     }
        // }, 1000, 1000);
    }

    private void endPreviousCall(String uuid) {
        ReactApplicationContext context = IncomingCall.reactContext;
        WritableMap data = Arguments.createMap();
        data.putString("uuid", uuid);

        if (IncomingCallUtils.isAppOnForeground(context)) {
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("endPreviousCall", data);
        }
    }

    @Override
    public void finish() {
        stopRinging();
        notificationManager.cancel(IncomingCall.FIRING_ALARM_NOTIFICATION_ID);
        muteAll(false);

        super.finish();
    }

    private void setupAudioManager() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    private void setupNotificationManager() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void startRinging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return;

        Log.i(LOG_TAG, "startRinging");

        Uri ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneURI);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        ringtone.play();
        long[] pattern = {1000, 1000, 1000, 1000, 1000};
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            vibrator.vibrate(pattern, 0);
        }
    }

    private void muteAll(boolean mute) {
        audioManager.setStreamMute(AudioManager.STREAM_ALARM, mute);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
    }

    private void stopRinging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return;

        vibrator.cancel();
        ringtone.stop();
    }

    @Override
    protected void onDestroy() {
        stopRinging();
        notificationManager.cancel(IncomingCall.FIRING_ALARM_NOTIFICATION_ID);
        muteAll(false);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {}

    public void sendResult(boolean accepted, Intent intent) {
        if (!accepted) {
            String uuid = intent.getStringExtra("uuid");
            API.endCall(this, uuid);
            return;
        }

        IncomingCall.showOnLockedScreen(true);

        Intent reactIntent = new Intent(this, MainActivity.class)
                .putExtras(intent)
                .putExtra("accepted", accepted)
                .putExtra("isAppOnForeground", isAppOnForeground())
                .putExtra("runApplication", true);

        this.startActivity(reactIntent);
    }

    public BroadcastReceiver endCallBrodcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("IncomingCallUiEndImmediately") != null) {
                Log.i(LOG_TAG, "endCallBroadcast: ");
                finish();
            }
        }
    };

    private boolean isAppOnForeground() {
        boolean isAppOnForeground = false;
        try {
            isAppOnForeground = new ForegroundCheckTask().execute(this).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return isAppOnForeground;
    }
}
