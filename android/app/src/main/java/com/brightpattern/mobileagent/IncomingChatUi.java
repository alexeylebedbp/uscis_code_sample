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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class IncomingChatUi extends AppCompatActivity {
    private static final String LOG_TAG = "IncomingChatUi";

    private NotificationManager notificationManager;
    private AudioManager audioManager;
    private Ringtone ringtone;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("data"));

        Intent intent = getIntent();

        setupNotificationManager();
        setupAudioManager();

        setContentView(R.layout.incoming_chat_ui);

        startRinging();

        WindowFlags.showOnLockedScreen(this);
        WindowFlags.setup(this);

        muteAll(true);

        fillOutLayout(intent);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        stopRinging();
        notificationManager.cancel(IncomingChat.FIRING_ALARM_NOTIFICATION_ID);
        muteAll(false);

        super.onDestroy();
    }
    
    @Override
    public void finish() {
        stopRinging();
        notificationManager.cancel(IncomingChat.FIRING_ALARM_NOTIFICATION_ID);
        muteAll(false);

        super.finish();
    }

    private void fillOutLayout(Intent intent) {
        String title = intent.getStringExtra("title");
        String service = intent.getStringExtra("service");
        long totalQueueTime = intent.getIntExtra("totalQueueTime", 0);

        title = IncomingChat.getNameFromTitle(title);

        TextView titleTextView = findViewById(R.id.title);
        TextView serviceTextView = findViewById(R.id.service);
        TextView waitTimeTextView = findViewById(R.id.waitTime);

        titleTextView.setText(title);
        
        if (service != null && !service.equals("")) {
            serviceTextView.setText(service);
            serviceTextView.setVisibility(View.VISIBLE);
        } else {
            serviceTextView.setVisibility(View.GONE);
        }

        waitTimeTextView.setText("Wait time: " + IncomingCallUtils.getWaitTime(totalQueueTime));

        Button declineButton = findViewById(R.id.decline);
        declineButton.setOnClickListener(v -> {
            sendResult("decline", intent, false);
            finish();
        });

        Button acceptButton = findViewById(R.id.accept);
        acceptButton.setOnClickListener(v -> {
            sendResult("acceptAndOpen", intent, true);
            finish();
        });
    }

    public void sendResult(String action, Intent intent, boolean invoke) {
        Context context = this.getApplicationContext();

        Intent actionIntent = new Intent(context, IncomingChatReceiver.class)
                .putExtras(intent)
                .putExtra("invoke", invoke)
                .setAction(action);

        sendBroadcast(actionIntent);
    }

    private void startRinging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return;

        Uri ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneURI);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        ringtone.play();
        long[] pattern = {1000, 1000, 1000, 1000, 1000};
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            vibrator.vibrate(pattern, 0);
        }
    }

    private void stopRinging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return;

        vibrator.cancel();
        ringtone.stop();
    }

    private void setupNotificationManager() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void setupAudioManager() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    private void muteAll(boolean mute) {
        audioManager.setStreamMute(AudioManager.STREAM_ALARM, mute);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("IncomingChatUiEndImmediately", false)) {
                finish();
            }
        }
    };
}
