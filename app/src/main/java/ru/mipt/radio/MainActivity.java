package ru.mipt.radio;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView playButton;
    private ImageView miptPlanet;

    private BroadcastReceiver notificationClickReceiver;

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

        IntentFilter filter = new IntentFilter(RadioForegroundService.NOTIFICATION_CLICKED_ACTION);
        notificationClickReceiver = new RadioServiceNotificationClickBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationClickReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlanetRotation();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationClickReceiver);
        notificationClickReceiver = null;
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


    class RadioServiceNotificationClickBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setPlayingState();
        }
    }
}

