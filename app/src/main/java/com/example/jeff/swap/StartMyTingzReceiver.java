package com.example.jeff.swap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jeff on 15-04-22.
 */
public class StartMyTingzReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, GPSBackgroundService.class);
        context.startService(startServiceIntent);
    }
}
