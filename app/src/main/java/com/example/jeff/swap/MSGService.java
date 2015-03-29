package com.example.jeff.swap;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.jeff.swap.activities.ChatActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by jeff on 15-01-11.
 */
public class MSGService extends IntentService {
    SharedPreferences prefs;
    NotificationCompat.Builder notification;
    NotificationManager notificationManager;
    private SharedPreferences.Editor editSharedPreferences;
    private static final String NAME_OF_SENDER = "usernameOfSender";
    private static final String CHAT_MESSAGE = "chatMessage";
    private static final String PHONE_NUMBER_FROM_INTENT = "phoneNumberFromIntent";

    public MSGService(){
        super("MSGService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        prefs = getSharedPreferences("Chat",0);
        if (!extras.isEmpty()){
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)){
                Log.e("MESSAGE_TYPE_SEND_ERROR","Error sending message");
            }else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)){
                Log.e("MESSAGE_TYPE_DELETED","Error with message being deleted");
            }else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
                Log.i("onHandleIntent","$$$ prefs.getString(\"CURRENTLY_ACTIVE\",\"\") $$$: "+(prefs.getString("CURRENTLY_ACTIVE","")));
                Log.i("onHandleIntent","$$$ extras.getString(\"phoneNumber\") $$$: "+(extras.getString("phoneNumber")));
                Log.i("onHandleIntent","$$$ !prefs.getString(\"CURRENTLY_ACTIVE\",\"\").equals(extras.getString(\"phoneNumber\")) $$$: "+(!prefs.getString("CURRENTLY_ACTIVE","").equals(extras.getString("phoneNumber"))));
                editSharedPreferences = prefs.edit();
                String senderName = extras.getString("username");
                String chatMessage = extras.getString("msg");
                String phoneNumberFromIntent = extras.getString("phoneNumber");
                String currentlyActiveValue = prefs.getString("CURRENTLY_ACTIVE","");
                if (!currentlyActiveValue.equals(extras.getString("phoneNumber"))){
                    editSharedPreferences.putString(PHONE_NUMBER_FROM_INTENT,phoneNumberFromIntent);
                    Log.i("CurrentActiveValue","$$$ currentlyActiveValue $$$: "+(currentlyActiveValue.equals("")));
                    if (!currentlyActiveValue.equals("")){
                        editSharedPreferences.putBoolean("doNotDisconnectFromSocketServer",false);
                    }
                    editSharedPreferences.commit();
                    sendNotification(chatMessage,extras.getString("phoneNumber"), senderName);
                }
                Log.i("MESSAGE RECEIVED FROM INTENT","MESSAGE: "+extras.getString("msg"));
            }
        }
        MSGReceiver.completeWakefulIntent(intent);
    }
    private void sendNotification(String msg, String phoneNumber, String username){
        Bundle args = new Bundle();
        args.putString("phoneNumber",phoneNumber);
        args.putString("username",username);
        args.putString("msg",msg);
        Intent chat = new Intent(this, ChatActivity.class);
        chat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chat.putExtra("INFO",args);
        notification = new NotificationCompat.Builder(this);
        notification.setContentTitle(username);
        notification.setContentText(msg);
        notification.setTicker("New message haters!");
        notification.setSmallIcon(R.mipmap.ic_launcher);
        PendingIntent contentIntent = PendingIntent.getActivity(this,1000,chat,PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(contentIntent);
        notification.setAutoCancel(true);
        notification.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("chatMessageNotification",0,notification.build());
    }
}
