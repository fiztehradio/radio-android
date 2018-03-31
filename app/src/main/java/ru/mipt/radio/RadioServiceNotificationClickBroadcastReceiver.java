package ru.mipt.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import static ru.mipt.radio.RadioForegroundService.NOTIFICATION_ACTION_ACTIVITY;

/**
 * Created by Gor on 31.03.2018.
 */

//--Отдельный файл нужен, т.к. для notification.deletIntent BroadcastReceiver должен быть прописан в манифесте
public class RadioServiceNotificationClickBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setAction(NOTIFICATION_ACTION_ACTIVITY);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }
}

