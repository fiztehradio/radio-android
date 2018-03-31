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
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static android.media.AudioAttributes.CONTENT_TYPE_MUSIC;
import static android.media.AudioAttributes.USAGE_MEDIA;


public class RadioForegroundService extends Service {
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_PLAYING = false;

    public static final String url = "http://radio.mipt.ru:8410/stream";

    public static String MAIN_ACTION = "com.marothiatechs.foregroundservice.action.main";
    public static String PLAY_ACTION = "com.marothiatechs.foregroundservice.action.play";
    public static String PLAY_FROM_NOTIFICATION_ACTION = "com.marothiatechs.foregroundservice.action.play.notification";
    public static String PAUSE_ACTION = "com.marothiatechs.foregroundservice.action.pause";
    public static String PAUSE_FROM_NOTIFICATION_ACTION = "com.marothiatechs.foregroundservice.action.pause.notification";
    public static String NOTIFICATION_CLICKED_ACTION = "com.marothiatechs.foregroundservice.action.clicked";
    public static String STOPFOREGROUND_ACTION = "com.marothiatechs.foregroundservice.action.stopforeground";
    public static int FOREGROUND_SERVICE = 101;

    private String CHANNEL_ID = "CHANNEL_ID";

    AudioManager audioManager;


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
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


    private SimpleExoPlayer mediaPlayer;
    private BandwidthMeter bandwidthMeter;
    private ExtractorsFactory extractorsFactory;
    private TrackSelection.Factory trackSelectionFactory;
    private TrackSelector trackSelector;
    private DefaultBandwidthMeter defaultBandwidthMeter;
    private DataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;

    private void preparePlayer() {
//        String url = "http://ep32.streamr.ru";

        bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();

        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        defaultBandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "mipt-radio"), defaultBandwidthMeter);

        mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(extractorsFactory)
                .createMediaSource(Uri.parse(url));

        mediaPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        mediaPlayer.prepare(mediaSource);

        mediaPlayer.setPlayWhenReady(true);
    }


    private void playRadio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        preparePlayer();

    }

    private void stopRadio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(request);
        } else {
            audioManager.abandonAudioFocus(listener);
        }
    }

    private void playClicked() {
        if (!requestAudioFocus()) {
            Toast.makeText(this, "Ошибка воспроизведения", Toast.LENGTH_LONG).show();
            return;
        }

        IS_PLAYING = true;
        Log.i(LOG_TAG, "Clicked Play");
        if (mediaPlayer == null || notification == null) {
            showNotification();
        }

        notification.contentView.setViewVisibility(R.id.notificationPlayButton, View.GONE);
        notification.contentView.setViewVisibility(R.id.notificationPauseButton, View.VISIBLE);
        startForeground(FOREGROUND_SERVICE, notification);

        playRadio();
    }


    private void pauseClicked() {
        IS_PLAYING = false;
        Log.i(LOG_TAG, "Clicked pause");

        notification.contentView.setViewVisibility(R.id.notificationPlayButton, View.VISIBLE);
        notification.contentView.setViewVisibility(R.id.notificationPauseButton, View.GONE);
        startForeground(FOREGROUND_SERVICE, notification);
        stopForeground(false);
        stopRadio();
    }

    Notification notification;

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

        //--Попытка достать название трека
        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(url);
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        builder.setContentTitle(artist);*/
        //--

        notification = builder.build();

        notification.contentView = views;
        notification.icon = R.mipmap.ic_launcher;
        notification.contentIntent = pendingIntent;
        startForeground(FOREGROUND_SERVICE, notification);
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

    AudioManager.OnAudioFocusChangeListener listener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            pauseClicked();
        }
    };

    AudioFocusRequest request;

    private boolean requestAudioFocus() {
        int result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes mAudioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(USAGE_MEDIA)
                            .setContentType(CONTENT_TYPE_MUSIC)
                            .build();
            request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mAudioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(listener)
                    .build();
            result = audioManager.requestAudioFocus(request);
        } else {
            result = audioManager.requestAudioFocus(listener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
}
