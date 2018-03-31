package ru.mipt.radio;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import static ru.mipt.radio.RadioForegroundService.DISMISS_INTENT;
import static ru.mipt.radio.RadioForegroundService.NOTIFICATION_ACTION_ACTIVITY;
import static ru.mipt.radio.RadioForegroundService.NOTIFICATION_ACTION_SERVICE;

public class MainActivity extends Activity {

    private ImageView playButton;
    private ImageView miptPlanet;

    private BroadcastReceiver notificationClickReceiver;
    private BroadcastReceiver notificationClickReceiverActivity;
    private BroadcastReceiver headsetPlugReceiver;

    private Boolean isHSConnected = false;

    private ValueAnimator planetRotation;
    private static int PLANET_ROTATION_DURATION = 30000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.playButton);
        miptPlanet = findViewById(R.id.miptPlanet);

        playButton.setOnClickListener(v -> playButtonClicked());

    }

    private void playButtonClicked() {
        Intent service = new Intent(MainActivity.this, RadioForegroundService.class);

        if (!RadioForegroundService.IS_PLAYING) {
            playButton.setImageResource(R.drawable.pause);
            startPlanetRotation();
            service.setAction(RadioForegroundService.PLAY_ACTION);
        } else {
            playButton.setImageResource(R.drawable.play);
            stopPlanetRotation();
            service.setAction(RadioForegroundService.PAUSE_ACTION);
        }

        startService(service);
    }


    void setPlayingState() {
        if (RadioForegroundService.IS_PLAYING) {
            playButton.setImageResource(R.drawable.pause);
            startPlanetRotation();
        } else {
            playButton.setImageResource(R.drawable.play);
            stopPlanetRotation();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        setPlayingState();

        IntentFilter filter = new IntentFilter(NOTIFICATION_ACTION_SERVICE);
        notificationClickReceiver = new RadioServiceNotificationClickBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationClickReceiver, filter);
        notificationClickReceiverActivity = new ActivityBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                notificationClickReceiverActivity, new IntentFilter(NOTIFICATION_ACTION_ACTIVITY));
        headsetPlugReceiver = new HeadsetBroadcastReceiver();
        registerReceiver( headsetPlugReceiver, new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlanetRotation();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationClickReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationClickReceiverActivity);
        unregisterReceiver(headsetPlugReceiver);
        notificationClickReceiver = null;
        notificationClickReceiverActivity = null;
        headsetPlugReceiver = null;
    }

    private void startPlanetRotation() {
        float rotation = miptPlanet.getRotation();
        planetRotation = new ValueAnimator();
        planetRotation.setFloatValues(rotation, 360 + rotation);
        planetRotation.setDuration(PLANET_ROTATION_DURATION);
        planetRotation.setInterpolator(new LinearInterpolator());
        planetRotation.setRepeatMode(ValueAnimator.RESTART);
        planetRotation.setRepeatCount(ValueAnimator.INFINITE);

        planetRotation.addUpdateListener(animation ->
                miptPlanet.setRotation((float) animation.getAnimatedValue()));
        planetRotation.start();
    }

    private void stopPlanetRotation() {
        if (planetRotation != null) {
            planetRotation.cancel();
            planetRotation = null;
        }
    }

    private class ActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra(DISMISS_INTENT, false)) finish();
            else setPlayingState();
        }
    }

    private class HeadsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == AudioManager.ACTION_HEADSET_PLUG){
                int i = intent.getIntExtra("state", -1);
                switch (i){
                    case 1: {
                        isHSConnected = true;
                        break;
                    }
                    case 0: {
                        if(isHSConnected) {
                            playButtonClicked();
                            isHSConnected = false;
                            break;
                        }
                    }
                }
            }
        }
    }
}

