package com.brightpattern.mobileagent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class SoundModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private static PowerManager powerManager;
    private static PowerManager.WakeLock wakeLock;
    private int savedSoundCategory = AudioManager.MODE_NORMAL;

    SoundModule (ReactApplicationContext context) {
        super(context);
        reactContext = context;
        powerManager = (PowerManager) reactContext.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public String getName () {
        return "SoundModule";
    }
    
    private void _turnOffProximitySensor () {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @SuppressLint("WakelockTimeout")
    private void _turnOnProximitySensor () {
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "mobileagent:SoundModule");
        }

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    @ReactMethod
    public void playSoundFromEar () {
        switchAudioOutput(false);
    }

    @ReactMethod
    public void playSoundFromSpeaker () {
        switchAudioOutput(true);
    }

    @ReactMethod
    public void turnOffProximitySensor () {
        _turnOffProximitySensor();
    }

    @SuppressLint("WakelockTimeout")
    @ReactMethod
    public void turnOnProximitySensor () {
        _turnOnProximitySensor();
    }

    @ReactMethod
    public void checkSoundCategory (Callback callback) {
        callback.invoke(savedSoundCategory);
    }

    @ReactMethod
    public void setSpeakerPhoneMode (Boolean isSpeakerPhoneOn) {
        if (isSpeakerPhoneOn) {
            savedSoundCategory = AudioManager.MODE_NORMAL;
        } else {
            savedSoundCategory = AudioManager.MODE_IN_COMMUNICATION;
        }
    }

    private void switchAudioOutput (Boolean isSpeakerPhoneOn) {
        AudioManager audioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);

        if (isSpeakerPhoneOn) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            _turnOffProximitySensor();
        } else {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            _turnOnProximitySensor();
        }

        audioManager.setSpeakerphoneOn(isSpeakerPhoneOn);
    }
}
