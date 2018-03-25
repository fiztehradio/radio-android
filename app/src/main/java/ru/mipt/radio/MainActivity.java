package ru.mipt.radio;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageView playButton;
    private ImageView miptPlanet;

    private boolean isPlaying = false;

    private ValueAnimator planetRotation;
    private static int PLANET_ROTATION_DURATION = 30000;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.playButton);
        miptPlanet = findViewById(R.id.miptPlanet);

        playButton.setOnClickListener(v -> playButtonClicked());

    }

    private void playButtonClicked() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            playButton.setImageResource( R.drawable.pause);
            startPlanetRotation();
            playRadio();
        } else {
            playButton.setImageResource( R.drawable.play);
            stopPlanetRotation();
            stopRadio();
        }

        Intent service = new Intent(MainActivity.this, RadioForegroundService.class);
        if (!RadioForegroundService.IS_SERVICE_RUNNING) {
            service.setAction(RadioForegroundService.STARTFOREGROUND_ACTION);
            RadioForegroundService.IS_SERVICE_RUNNING = true;
        } else {
            service.setAction(RadioForegroundService.STOPFOREGROUND_ACTION);
            RadioForegroundService.IS_SERVICE_RUNNING = false;

        }
        startService(service);
    }

    private void playRadio() {
        String url = "http://radio.mipt.ru:8410/stream";
//        String url = "http://ep32.streamr.ru";
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare(); // might take long! (for buffering, etc)
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.start();

    }

    private void stopRadio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
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
}
