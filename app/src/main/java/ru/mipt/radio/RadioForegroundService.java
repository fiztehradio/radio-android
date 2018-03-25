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
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class RadioForegroundService extends Service {
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;

    public static String MAIN_ACTION = "com.marothiatechs.foregroundservice.action.main";
    public static String INIT_ACTION = "com.marothiatechs.foregroundservice.action.init";
    public static String PREV_ACTION = "com.marothiatechs.foregroundservice.action.prev";
    public static String PLAY_ACTION = "com.marothiatechs.foregroundservice.action.play";
    public static String NEXT_ACTION = "com.marothiatechs.foregroundservice.action.next";
    public static String STARTFOREGROUND_ACTION = "com.marothiatechs.foregroundservice.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.marothiatechs.foregroundservice.action.stopforeground";
    public static int FOREGROUND_SERVICE = 101;

    private String CHANNEL_ID = "CHANNEL_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("description");
            NotificationManager notificationManager = getBaseContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            showNotification();
            Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals(PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");

            Toast.makeText(this, "Clicked Previous!", Toast.LENGTH_SHORT)
                    .show();
        } else if (intent.getAction().equals(PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");

            Toast.makeText(this, "Clicked Play!", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");

            Toast.makeText(this, "Clicked Next!", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(
                STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, RadioForegroundService.class);
        previousIntent.setAction(PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, RadioForegroundService.class);
        playIntent.setAction(PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, RadioForegroundService.class);
        nextIntent.setAction(NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("TutorialsFace Music Player")
                .setTicker("TutorialsFace Music Player")
                .setContentText("My song")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous",
                        ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next",
                        pnextIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        Notification notification = builder.build();
        startForeground(FOREGROUND_SERVICE, notification);

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