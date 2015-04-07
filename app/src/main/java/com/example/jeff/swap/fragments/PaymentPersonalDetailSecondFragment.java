package com.example.jeff.swap.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;

/**
 * Created by jeff on 15-04-06.
 */
public class PaymentPersonalDetailSecondFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editSharedPreferences;

    private EditText mEmailAddress;
    private Spinner dob_month;
    private Spinner dob_day;
    private Spinner dob_year;
    private TextView personalIdTextView;
    private EditText mPersonalIdNumber;
    private Button mNextButton;

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
        View view = inflater.inflate(R.layout.payment_personal_detail_second_fragment, container, false);
        PaymentActivity paymentActivity = (PaymentActivity) getActivity();
        paymentActivity.getSupportActionBar().setTitle("Payment Information");
        mEmailAddress = (EditText) view.findViewById(R.id.firstName);
        dob_month = (Spinner) view.findViewById(R.id.monthSpinner);
        dob_day = (Spinner) view.findViewById(R.id.daySpinner);
        dob_year = (Spinner) view.findViewById(R.id.yearSpinner);
        personalIdTextView = (TextView) view.findViewById(R.id.personal_id_number_textview);
        mPersonalIdNumber = (EditText) view.findViewById(R.id.personal_id_number_edittext);
        mNextButton = (Button) view.findViewById(R.id.nextButton);

        Log.i("sharedPref","$$$ sharedPref -> country $$$: "+sharedPreferences.getString("country",""));
        Log.i("sharedPref","$$$ sharedPreferences.getString(\"country\",\"\").equals(\"Canada\") $$$: "+(sharedPreferences.getString("country","").equals("Canada")));
        Log.i("sharedPref","$$$ sharedPref -> province $$$: "+sharedPreferences.getString("province",""));
        if(sharedPreferences.getString("country","").equals("Canada")){
            personalIdTextView.setText("Social Insurance Number (SIN)");
        }else{
            personalIdTextView.setText("Personal ID number / SSN last 4 digits");
        }

        setSpinners();
        setFormValidators();
        return view;
    }

    private void setSpinners(){
        Integer monthValues[] = new Integer[12];
        for(int i = 0; i < 12; i++){
            monthValues[i] = Integer.valueOf(i+1);
        }
        ArrayAdapter<Integer> dobMonthArrayAdapter = new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_item, monthValues);
        dobMonthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        dob_month.setAdapter(dobMonthArrayAdapter);
        Integer dayValues[] = new Integer[31];
        for(int i = 0; i < 31; i++){
            dayValues[i] = Integer.valueOf(i+1);
        }
        ArrayAdapter<Integer> dobDayArrayAdapter = new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_item, dayValues);
        dobDayArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        dob_day.setAdapter(dobDayArrayAdapter);
        Integer yearValues[] = new Integer[89];
        for(int i = 0; i < 89; i++){
            yearValues[i] = Integer.valueOf(1920+i);
        }
        ArrayAdapter<Integer> dobYearArrayAdapter = new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_item, yearValues);
        dobYearArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        dob_year.setAdapter(dobYearArrayAdapter);
        dob_year.setSelection(75);
    }

    private void setFormValidators(){
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String errorMessage = "";
                boolean hasErrors = false;
                if(mEmailAddress.length() == 0){
                    errorMessage+="Email address cannot be blank\n";
                    hasErrors = true;
                }
                if(mPersonalIdNumber.length() == 0){
                    errorMessage+="Last name cannot be blank\n";
                    hasErrors = true;
                }
                if(hasErrors){
                    DialogFragment fragment = InformationDialogFragment.newInstance(errorMessage,"Error");
                    fragment.show(getActivity().getFragmentManager(), "error");
                }else{
                    Toast.makeText(getActivity(), "Everything good to go yo!", Toast.LENGTH_LONG).show();
                }
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
