package com.example.jeff.swap.activities;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;

import com.example.jeff.swap.fragments.ChatFragment;

/**
 * Created by jeff on 15-01-11.
 */
public class ChatActivity extends SingleFragmentActivity {

    public static final String NAME = "name";
    private Bundle bundle;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editSharedPreferences;

    @Override
    protected Fragment createFragment() {
        bundle = getIntent().getBundleExtra("INFO");
        String name = bundle.getString(NAME);
        Log.i("createFragment()","$$$ name is $$$: "+name);
        if (name != null){
            Log.i("createFragment()","$$$ createFragment() IF CONDITION CALLED $$$");
            return ChatFragment.newInstance(name);
        }else{
            Log.i("createFragment()","$$$ createFragment() ELSE CONDITION CALLED $$$");
            return new ChatFragment();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // For displaying the options menu and ancestral navigation for navigating upwards to the home activity/fragment
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(this) != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    protected void onNewIntent (Intent intent){
        sharedPreferences = getSharedPreferences("Chat", 0);
        editSharedPreferences = sharedPreferences.edit();
        Bundle bundleFromIntent = intent.getBundleExtra("INFO");
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
    }
}
