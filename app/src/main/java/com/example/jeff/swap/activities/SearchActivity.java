package com.example.jeff.swap.activities;

import android.app.Fragment;

import com.example.jeff.swap.fragments.SearchFragment;

/**
 * Created by jeff on 15-03-19.
 */
public class SearchActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        return new SearchFragment();
    }
}
