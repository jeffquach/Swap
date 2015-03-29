package com.example.jeff.swap.activities;

import android.app.Fragment;

import com.example.jeff.swap.fragments.PostListFragment;

/**
 * Created by jeff on 15-03-14.
 */
public class PostListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        String city = getIntent().getStringExtra("city");
        if (city != null){
            return PostListFragment.newInstance(city);
        }else{
            return new PostListFragment();
        }
    }
}
