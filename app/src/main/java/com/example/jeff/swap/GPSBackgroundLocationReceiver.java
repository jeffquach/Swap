package com.example.jeff.swap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeff on 15-04-17.
 */
public class GPSBackgroundLocationReceiver extends BroadcastReceiver {

    SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent){
        sharedPreferences = context.getSharedPreferences("Chat",0);
        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        if (location != null){
            Toast.makeText(context,"$$$$$ GPS location changed! latitude: " + (location.getLatitude()) + " , longitude: " + (location.getLongitude()),Toast.LENGTH_LONG).show();
            new UploadLocation().execute(String.valueOf(location.getLongitude()),String.valueOf(location.getLatitude()));
        }
    }

    private class UploadLocation extends AsyncTask<String,Void,JSONObject>{

        @Override
        protected JSONObject doInBackground(String... tingz) {
            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phoneNumber", sharedPreferences.getString("REGISTRATION_PHONE_NUMBER","")));
            params.add(new BasicNameValuePair("username", sharedPreferences.getString("USERNAME","")));
            params.add(new BasicNameValuePair("longitude", tingz[0]));
            params.add(new BasicNameValuePair("latitude", tingz[1]));
            JSONObject jsonObject = jsonParser.getJSONFromUrl(BuildConfig.SERVER_URL+"/users/updateLocation",params);
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            String response = null;
            try{
                response = json.getString("response");
                if (response.equals("Successfully updated location")){
                    Log.e("POSTY","$$$$$ LOCATION UPDATED SON! $$$$$");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
