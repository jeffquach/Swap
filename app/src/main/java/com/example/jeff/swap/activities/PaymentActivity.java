package com.example.jeff.swap.activities;

import android.app.Fragment;

import com.example.jeff.swap.fragments.PaymentFragment;
import com.example.jeff.swap.fragments.PaymentPersonalDetailThirdFragment;

/**
 * Created by jeff on 15-04-04.
 */
public class PaymentActivity extends SingleFragmentActivity {
    protected PaymentPersonalDetailThirdFragment.BaseBackPressedListener onBackPressedListener;
    @Override
    protected Fragment createFragment(){
        return new PaymentFragment();
    }

    @Override
    public void onBackPressed() {
        if(onBackPressedListener != null){
            onBackPressedListener.doBack();
        }
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void setOnBackPressedListener(PaymentPersonalDetailThirdFragment.BaseBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }
}
