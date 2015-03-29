package com.example.jeff.swap.activities;

import android.app.Fragment;

import com.example.jeff.swap.fragments.PostShowFragment;

/**
 * Created by jeff on 15-03-25.
 */
public class PostShowActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PostShowFragment();
    }
}
