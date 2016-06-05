package com.emilgras.boxdrops.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.emilgras.boxdrops.extras.Util;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
        Log.d("MILO", "BootReceiver: ");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MILO", "onReceive: ");
        Util.scheduleAlarm(context);
    }
}
