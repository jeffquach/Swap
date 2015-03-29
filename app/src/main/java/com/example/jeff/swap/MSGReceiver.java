package com.example.jeff.swap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Date;

/**
 * Created by jeff on 15-01-11.
 */
public class MSGReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        Intent messageIntent = new Intent("Msg");
        String message = extras.getString("msg");
        String sender = extras.getString("username");
        messageIntent.putExtra("msg",message);
        messageIntent.putExtra("phoneNumber",extras.getString("phoneNumber")); // *** 'fromu' is the phone number, RENAME! ***
        messageIntent.putExtra("username",sender);
        Log.i("MSGReceiver","$$$ extras.getString(\"msg\"), extras.getString(\"phoneNumber\"), extras.getString(\"username\") $$$: "+(extras.getString("msg"))+", "+(extras.getString("phoneNumber"))+", "+(extras.getString("username")));
        ChatManager.get(context).insertChatMessage(String.format("%tB %<te, %<tY, %<tr",new Date()),sender,sender,message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
        ComponentName componentName = new ComponentName(context.getPackageName(),MSGService.class.getName());
        startWakefulService(context,(intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
    }
}
