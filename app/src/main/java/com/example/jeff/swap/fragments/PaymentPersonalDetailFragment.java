package com.example.jeff.swap.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;

/**
 * Created by jeff on 15-04-05.
 */
public class PaymentPersonalDetailFragment extends Fragment {

    private String canadianProvinces[] = {"AB","BC","MB","NB","NL","NS","NT","NU","ON","PE","QC","SK","YT"};
    private String americanStates[] = {"AL","AK","AZ","AR","CA","CO","CT","DC","DE","FL","GA","HI","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV","WI","WY"};
    private Spinner countrySpinner;
    private Spinner provinceSpinner;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editSharedPreferences;

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mCity;
    private EditText mPostalCode;
    private EditText mAddress;
    private Button mNextButton;

    private String countrySelection;
    private String provinceSelection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        sharedPreferences = getActivity().getSharedPreferences("personalInformation",0);
        editSharedPreferences = sharedPreferences.edit();
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
        mFirstName = (EditText) view.findViewById(R.id.firstName);
        mLastName = (EditText) view.findViewById(R.id.lastName);
        mCity = (EditText) view.findViewById(R.id.city);
        mPostalCode = (EditText) view.findViewById(R.id.postalCode);
        mAddress = (EditText) view.findViewById(R.id.address);
        mNextButton = (Button) view.findViewById(R.id.nextButton);
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countrySelection = parent.getItemAtPosition(position).toString();
                Log.i("onItemSelected","$$$ countrySelection $$$: "+countrySelection);
                setSpinner(countrySelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                provinceSelection = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setSpinner("Canada");
        setFormValidators();
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

    private void setFormValidators(){
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String errorMessage = "";
                boolean hasErrors = false;
//                if(mFirstName.length() == 0){
//                    errorMessage+="First name cannot be blank\n";
//                    hasErrors = true;
//                }
//                if(mLastName.length() == 0){
//                    errorMessage+="Last name cannot be blank\n";
//                    hasErrors = true;
//                }
//                if(mCity.length() == 0){
//                    errorMessage+="City cannot be blank\n";
//                    hasErrors = true;
//                }
//                if(mPostalCode.length() == 0){
//                    errorMessage+="Postal code cannot be blank\n";
//                    hasErrors = true;
//                }
//                if(mAddress.length() == 0){
//                    errorMessage+="Address cannot be blank\n";
//                    hasErrors = true;
//                }
//                if(hasErrors){
//                    DialogFragment fragment = InformationDialogFragment.newInstance(errorMessage,"Error");
//                    fragment.show(getActivity().getFragmentManager(), "error");
//                }else{
                    editSharedPreferences.putString("firstName",mFirstName.getText().toString());
                    editSharedPreferences.putString("lastName",mLastName.getText().toString());
                    editSharedPreferences.putString("city",mCity.getText().toString());
                    editSharedPreferences.putString("postalCode",mPostalCode.getText().toString());
                    editSharedPreferences.putString("address",mAddress.getText().toString());
                    editSharedPreferences.putString("country",countrySelection);
                    editSharedPreferences.putString("province",provinceSelection);
                    editSharedPreferences.commit();
                    Fragment personalDetailsSecondFragment = new PaymentPersonalDetailSecondFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.fragmentContainer,personalDetailsSecondFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack("PaymentPersonalDetailSecondFragment");
                    ft.commit();
                //}
            }
        });
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
