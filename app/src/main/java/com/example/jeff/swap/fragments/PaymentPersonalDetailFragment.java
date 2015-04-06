package com.example.jeff.swap.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;

/**
 * Created by jeff on 15-04-05.
 */
public class PaymentPersonalDetailFragment extends Fragment {

    private String canadianProvinces[] = {"AB","BC","MB","NB","NL","NS","NT","NU","ON","PE","QC","SK","YT"};
    private String americanStates[] = {"AL","AK","AZ","AR","CA","CO"};
    private Spinner countrySpinner;
    private Spinner provinceSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.payment_personal_detail_fragment, container, false);
        PaymentActivity paymentActivity = (PaymentActivity) getActivity();
        paymentActivity.getSupportActionBar().setTitle("Payment Information");
        countrySpinner = (Spinner) view.findViewById(R.id.countrySpinner);
        provinceSpinner = (Spinner) view.findViewById(R.id.provinceSpinner);
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String stringy = parent.getItemAtPosition(position).toString();
                Toast.makeText(getActivity(), "Country: " + stringy, Toast.LENGTH_LONG).show();
                setSpinner(stringy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setSpinner("Canada");
        return view;
    }

    private void setSpinner(String countryProvince){
        String stuff[];
        if (countryProvince.equals("Canada")){
            stuff = canadianProvinces;
        }else{
            stuff = americanStates;
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, stuff);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        provinceSpinner.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.help_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_help:
                DialogFragment fragment = InformationDialogFragment.newInstance("As a reminder, your personal and banking information will be required to verify your identity and to prevent fraud","Important");
                fragment.show(getActivity().getFragmentManager(), "paymentDetails");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
