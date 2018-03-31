package ru.mipt.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by vadim.matsishin on 29/03/2018.
 */

public class HeadsetBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (isHeadSetPlugIntent(intent)) {
            int state = intent.getIntExtra("state", -1);
            boolean isHSConnected = state == 1;
            if (isHSConnected) {
                // do nothing
            } else {
                //pause
                Intent service = new Intent(context, RadioForegroundService.class);
                service.setAction(RadioForegroundService.PAUSE_FROM_NOTIFICATION_ACTION);
                context.startService(service);
            }
        }
    }


    private boolean isHeadSetPlugIntent(Intent intent) {
        String action;
        if (intent != null && intent.getAction() != null) {
            action = intent.getAction();
        } else {
            return false;
        }

        return action.equals("android.intent.action.HEADSET_PLUG") ||
                action.equals("android.bluetooth.headset.action.STATE_CHANGED") ||
                action.equals("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
    }
}
