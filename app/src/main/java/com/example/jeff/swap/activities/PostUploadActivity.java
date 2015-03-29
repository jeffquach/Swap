package com.example.jeff.swap.activities;

import android.app.Fragment;

import com.example.jeff.swap.fragments.PostUploadFragment;

/**
 * Created by jeff on 15-03-07.
 */
public class PostUploadActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        return new PostUploadFragment();
    }
}