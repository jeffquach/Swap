package com.example.jeff.swap.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.JSONParser;
import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.ChatActivity;
import com.example.jeff.swap.activities.PostListActivity;
import com.example.jeff.swap.activities.PostUploadActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jeff on 15-01-11.
 */
public class UserFragment extends Fragment {
    ListView listView;
    ArrayList<HashMap<String,String>> users = new ArrayList<HashMap<String,String>>();
    Button refresh, logout;
    List<NameValuePair> params;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Create menu
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.user_fragment, container, false);
        prefs = getActivity().getSharedPreferences("Chat",0);
        editSharedPreferences = prefs.edit();
        Log.i("UserFragment","$$$ BEFORE! prefs.getString(\"CURRENTLY_ACTIVE\",\"\"); $$$: "+(prefs.getString("CURRENTLY_ACTIVE","")));
        editSharedPreferences.putString("CURRENTLY_ACTIVE", "").commit();
        Log.i("UserFragment","$$$ AFTER! prefs.getString(\"CURRENTLY_ACTIVE\",\"\"); $$$: "+(prefs.getString("CURRENTLY_ACTIVE","")));
        listView = (ListView) view.findViewById(R.id.listView);
        refresh = (Button) view.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                //ft.remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
                Fragment userFragment = new UserFragment();
                ft.replace(R.id.content_frame,userFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
            }
        });
        logout = (Button) view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Logout().execute();
            }
        });

        new Load().execute();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.main_options_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_create_new_post:
                Intent intent = new Intent(getActivity(),PostUploadActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_browse_posts:
                Intent intentBrowsePosts = new Intent(getActivity(),PostListActivity.class);
                startActivity(intentBrowsePosts);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class Load extends AsyncTask<String, String, JSONArray>{
        @Override
        protected JSONArray doInBackground(String... args){
            JSONParser jsonParser = new JSONParser();
//            params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("phoneNumber",prefs.getString("REGISTRATION_PHONE_NUMBER","")));
            JSONArray jsonArray = jsonParser.getJSONArray(BuildConfig.SERVER_URL+"/users?phoneNumber="+prefs.getString("REGISTRATION_PHONE_NUMBER",""));
            return jsonArray;
        }
        @Override
        protected void onPostExecute(JSONArray json){
            Log.i("jsonarray length","$$$ jsonArray length $$$: "+json);
            if (json != null && json.length() > 0){
                for(int i = 0; i < json.length(); i++){
                    JSONObject jsonObject = null;
                    try{
                        jsonObject = json.getJSONObject(i);
                        String username = jsonObject.getString("username");
                        String phoneNumber = jsonObject.getString("phoneNumber");
                        HashMap<String,String> map = new HashMap<String,String>();
                        map.put("username",username);
                        map.put("phoneNumber",phoneNumber);
                        users.add(map);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("onPostExecute","$$$ users array is $$$: "+(String.valueOf(users)));
                ListAdapter adapter = new SimpleAdapter(getActivity(),users,R.layout.user_list_single,new String[] {"username","phoneNumber"},new int[] {R.id.username, R.id.phoneNumber});
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        Bundle args = new Bundle();
                        args.putString("username",users.get(position).get("username"));
                        args.putString("phoneNumber",users.get(position).get("phoneNumber"));
                        Intent chat = new Intent(getActivity(),ChatActivity.class);
                        chat.putExtra("INFO",args);
                        startActivity(chat);
                    }
                });
            }
        }
    }
    private class Logout extends AsyncTask<String,String, JSONObject>{
        @Override
        protected JSONObject doInBackground(String... args){
            JSONParser jsonParser = new JSONParser();
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phoneNumber", prefs.getString("REGISTRATION_PHONE_NUMBER","")));
            JSONObject jsonObject = jsonParser.getJSONFromUrl(BuildConfig.SERVER_URL+"/logout",params);
            return jsonObject;
        }
        @Override
        protected void onPostExecute(JSONObject json){
            String response = null;
            try{
                response = json.getString("response");
                Toast.makeText(getActivity(),response,Toast.LENGTH_SHORT).show();
                if (response.equals("User was successfully removed!")){
                    Fragment reg = new LoginFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame,reg).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("REGISTRATION_PHONE_NUMBER","");
                    edit.commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
