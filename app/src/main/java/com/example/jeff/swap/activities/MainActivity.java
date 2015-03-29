package com.example.jeff.swap.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.jeff.swap.fragments.LoginFragment;
import com.example.jeff.swap.R;
import com.example.jeff.swap.fragments.UserFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "59317488312";
    static final String TAG = "BALLIN'";
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    Context context;
    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("Chat",0);
        context = getApplicationContext();

        if (!prefs.getString("REGISTRATION_PHONE_NUMBER","").isEmpty()){
            Fragment user = new UserFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame,user);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }else if(!prefs.getString("REGISTRATION_ID","").isEmpty()){
            Fragment reg = new LoginFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame,reg);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }else if (checkPlayServices()){
            Log.i("register","$$$ New ID registered $$$");
            new Register().execute();
        }else{
            Toast.makeText(getApplicationContext(),"This device ain't support homes!",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else{
                Log.i(TAG,"This device ain't support homes");
                finish();
            }
            return false;
        }
        return true;
    }

    private class Register extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args){
            try{
                if (gcm == null){
                    gcm = GoogleCloudMessaging.getInstance(context);
                    regid = gcm.register(SENDER_ID);
                    Log.e("RegId",regid);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("REGISTRATION_ID",regid);
                    edit.commit();
                }
                return regid;
            }catch(IOException e){
                Log.e("Error",e.getMessage());
                return "Failure";
            }
        }
        @Override
        protected void onPostExecute(String json){
            Fragment reg = new LoginFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame,reg);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }
    }
}
