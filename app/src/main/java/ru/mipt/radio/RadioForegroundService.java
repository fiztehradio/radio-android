package ru.mipt.radio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;

public class RadioForegroundService extends Service {
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_PLAYING = false;

    public static String MAIN_ACTION = "com.marothiatechs.foregroundservice.action.main";
    public static String PLAY_ACTION = "com.marothiatechs.foregroundservice.action.play";
    public static String PLAY_FROM_NOTIFICATION_ACTION = "com.marothiatechs.foregroundservice.action.play.notification";
    public static String PAUSE_ACTION = "com.marothiatechs.foregroundservice.action.pause";
    public static String PAUSE_FROM_NOTIFICATION_ACTION = "com.marothiatechs.foregroundservice.action.pause.notification";
    public static String NOTIFICATION_CLICKED_ACTION = "com.marothiatechs.foregroundservice.action.clicked";
    public static String STOPFOREGROUND_ACTION = "com.marothiatechs.foregroundservice.action.stopforeground";
    public static int FOREGROUND_SERVICE = 101;

    private String CHANNEL_ID = "CHANNEL_ID";


    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         if (intent.getAction().equals(PLAY_ACTION)) {
            playClicked();
         } else if (intent.getAction().equals(PAUSE_ACTION)) {
             pauseClicked();
         } else if (intent.getAction().equals(PLAY_FROM_NOTIFICATION_ACTION)) {
             playClicked();
             LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(NOTIFICATION_CLICKED_ACTION));
         } else if (intent.getAction().equals(PAUSE_FROM_NOTIFICATION_ACTION)) {
             pauseClicked();
             LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(NOTIFICATION_CLICKED_ACTION));
         } else if (intent.getAction().equals(
                STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void startPlayer() {
        Log.i(LOG_TAG, "Received Start Foreground Intent ");
        Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();
        showNotification();
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

    private void pauseRadio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void playClicked() {
        IS_PLAYING = true;
        Log.i(LOG_TAG, "Clicked Play");
        Toast.makeText(this, "Clicked Play!", Toast.LENGTH_SHORT).show();
        if (mediaPlayer == null || status == null) {
            startPlayer();
        }

        status.contentView.setViewVisibility(R.id.notificationPlayButton, View.GONE);
        status.contentView.setViewVisibility(R.id.notificationPauseButton, View.VISIBLE);
        startForeground(FOREGROUND_SERVICE, status);

        playRadio();
    }


    private void pauseClicked() {
        IS_PLAYING = false;
        Log.i(LOG_TAG, "Clicked pause");
        Toast.makeText(this, "Clicked pause!", Toast.LENGTH_SHORT).show();

        status.contentView.setViewVisibility(R.id.notificationPlayButton, View.VISIBLE);
        status.contentView.setViewVisibility(R.id.notificationPauseButton, View.GONE);
        startForeground(FOREGROUND_SERVICE, status);
        pauseRadio();
    }

    Notification status;

    private void showNotification() {
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.radio_foreground_service_notification);

        views.setViewVisibility(R.id.icon, View.VISIBLE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        Intent playIntent = new Intent(this, RadioForegroundService.class);
        playIntent.setAction(PLAY_FROM_NOTIFICATION_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);


        Intent pauseIntent = new Intent(this, RadioForegroundService.class);
        pauseIntent.setAction(PAUSE_FROM_NOTIFICATION_ACTION);
        PendingIntent ppauseIntent = PendingIntent.getService(this, 0,
                pauseIntent, 0);

        views.setOnClickPendingIntent(R.id.notificationPlayButton, pplayIntent);
        views.setOnClickPendingIntent(R.id.notificationPauseButton, ppauseIntent);
        views.setImageViewBitmap(R.id.notificationPlayButton, getBitmap(this, R.drawable.play));
        views.setImageViewBitmap(R.id.notificationPauseButton, getBitmap(this, R.drawable.pause));

        Notification.Builder builder = new Notification.Builder(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        status = builder.build();

        status.contentView = views;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.mipmap.ic_launcher;
        status.contentIntent = pendingIntent;
        startForeground(FOREGROUND_SERVICE, status);
    }

    public static Bitmap getBitmap(Context context, int resource) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    resource, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }
}