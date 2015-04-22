package com.example.jeff.swap.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.JSONParser;
import com.example.jeff.swap.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeff on 15-01-11.
 */
public class LoginFragment extends Fragment {
    SharedPreferences prefs;
    EditText username, phoneNumber;
    Button login;
    List<NameValuePair> params;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        prefs = getActivity().getSharedPreferences("Chat",0);
        username = (EditText) view.findViewById(R.id.username);
        phoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        login = (Button) view.findViewById(R.id.log_btn);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Registering ... ");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("REGISTRATION_PHONE_NUMBER",phoneNumber.getText().toString());
                edit.putString("USERNAME", username.getText().toString());
                edit.commit();
                new Login().execute();
            }
        });
        return view;
    }

    private class Login extends AsyncTask<String, String, JSONObject>{
        @Override
        protected JSONObject doInBackground(String... args){
            JSONParser json = new JSONParser();
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username",username.getText().toString()));
            params.add(new BasicNameValuePair("phoneNumber",phoneNumber.getText().toString()));
            params.add(new BasicNameValuePair("registration_id",prefs.getString("REGISTRATION_ID","")));
            ///// CHANGE THIS LATER!!!! //////
            params.add(new BasicNameValuePair("longitude",String.valueOf(-79.490167)));
            params.add(new BasicNameValuePair("latitude",String.valueOf(43.666165)));
            ///// CHANGE THIS LATER!!!! //////
            JSONObject jsonObject = json.getJSONFromUrl(BuildConfig.SERVER_URL+"/login",params);
            return jsonObject;
        }
        @Override
        protected void onPostExecute(JSONObject json){
            progressDialog.dismiss();
            try{
                String response = json.getString("response");
                if (response.equals("Successfully Registered")){
                    Fragment reg = new UserFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame,reg);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }else if(response.equals("Successfully logged in!")){
                    Fragment reg = new UserFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame,reg);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                }else{
                    Toast.makeText(getActivity(),response,Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
