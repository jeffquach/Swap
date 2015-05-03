package com.example.jeff.swap;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by jeff on 15-04-18.
 */
public class GPSBackgroundService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private static final String ACTION_LOCATION = "com.example.jeff.swap.GPS_SERVICE_ACTION_LOCATION";
    private SharedPreferences sharedPreferences;
    private int stopId;


    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            synchronized (this) {
                try {
                    Log.e("SERVICE","$$$$ onHandleIntent called! $$$$$");
                    onConnected(new Bundle());
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onCreate() {
        Log.e("SERVICE","$$$$ onCreated called! $$$$$");
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",0x0000000a);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        sharedPreferences = getApplicationContext().getSharedPreferences("GPSBackgroundService",0);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SERVICE","$$$$ onStartCommand called! $$$$$");
        mGoogleApiClient.connect();
        Message msg = mServiceHandler.obtainMessage();
        stopId = startId;
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }


    @Override
    public void onDestroy() {
        Log.e("SERVICE","$$$$ onDestroy called! $$$$$");
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        stopSelf(stopId);
        stopSelfResult(stopId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        runUpdate();
    }

    private void runUpdate(){
        sharedPreferences = getSharedPreferences("GPSBackgroundService",0);
        float displacementDistance = sharedPreferences.getFloat("desiredDistance",0f);
        if (displacementDistance == 0f){
            displacementDistance = 0f;
        }
        if(mGoogleApiClient.isConnected()){
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(8000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setSmallestDisplacement(displacementDistance);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            Intent broadcast = new Intent(ACTION_LOCATION);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, PendingIntent.getBroadcast(this, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
