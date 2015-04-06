package com.example.jeff.swap.activities;

import android.app.Fragment;

import com.example.jeff.swap.fragments.PaymentFragment;

/**
 * Created by jeff on 15-04-04.
 */
public class PaymentActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment(){
        return new PaymentFragment();
    }
}
