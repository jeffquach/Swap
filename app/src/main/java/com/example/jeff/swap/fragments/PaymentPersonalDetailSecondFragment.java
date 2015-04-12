package com.example.jeff.swap.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;

import java.util.regex.Pattern;

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
    private final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final String PERSONAL_ID_NUMBER_REGEX = "\\d*";
    private final String SIN_REGEX = "\\d{9}";
    private boolean isCanada = false;

    private String dobMonthString;
    private String dobDayString;
    private String dobYearString;

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
        View view = inflater.inflate(R.layout.activity_fragment, container, false);
        PaymentActivity paymentActivity = (PaymentActivity) getActivity();
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.fragmentContainer);
        TableLayout tableLayout = new TableLayout(getActivity());
        frameLayout.addView(inflater.inflate(R.layout._payment_personal_detail_second_partial, tableLayout, true));
        paymentActivity.getSupportActionBar().setTitle("Payment Information");
        mEmailAddress = (EditText) view.findViewById(R.id.emailAddress);
        dob_month = (Spinner) view.findViewById(R.id.monthSpinner);
        dob_day = (Spinner) view.findViewById(R.id.daySpinner);
        dob_year = (Spinner) view.findViewById(R.id.yearSpinner);
        personalIdTextView = (TextView) view.findViewById(R.id.personal_id_number_textview);
        mPersonalIdNumber = (EditText) view.findViewById(R.id.personal_id_number_edittext);
        mNextButton = (Button) view.findViewById(R.id.nextButton);

        if(sharedPreferences.getString("country","").equals("Canada")){
            personalIdTextView.setText("Social Insurance Number (SIN)");
            isCanada = true;
        }else{
            personalIdTextView.setText("Social Security Number (SSN)");
        }

        mEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!Pattern.matches(EMAIL_REGEX, mEmailAddress.getText().toString())){
                    mEmailAddress.setError("Your email address is in an incorrect format");
                }
            }
        });

        mPersonalIdNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isCanada){
                    if(!Pattern.matches(SIN_REGEX, mPersonalIdNumber.getText().toString())){
                        mPersonalIdNumber.setError("Your SIN must be 9 digits long");
                    }
                }else{
                    if(!Pattern.matches(SIN_REGEX, mPersonalIdNumber.getText().toString())){
                        mPersonalIdNumber.setError("Your SSN must be 9 digits long");
                    }
                }
            }
        });
        dob_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dobMonthString = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dob_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dobDayString = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dob_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dobYearString = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setSpinners();
        setInputedValues();
        setFormValidators();
        return view;
    }

    private void setInputedValues(){
        if(!sharedPreferences.getString("emailAddress","").equals("")){
            mEmailAddress.setText(sharedPreferences.getString("emailAddress",""));
        }
        if(!sharedPreferences.getString("personalIdNumber","").equals("")){
            mPersonalIdNumber.setText(sharedPreferences.getString("personalIdNumber",""));
        }
    }

    private void presetSpinnerValues(ArrayAdapter<Integer> adapter, Spinner spinner,String sharedPrefValue){
        if((!sharedPreferences.getString("dobMonth","").equals(""))){
            int setPosition = adapter.getPosition(Integer.valueOf(sharedPreferences.getString(sharedPrefValue,"")));
            spinner.setSelection(setPosition);
        }
    }

    private void setSpinners(){
        Integer monthValues[] = new Integer[12];
        for(int i = 0; i < 12; i++){
            monthValues[i] = Integer.valueOf(i+1);
        }
        ArrayAdapter<Integer> dobMonthArrayAdapter = new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_item, monthValues);
        dobMonthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        dob_month.setAdapter(dobMonthArrayAdapter);
        presetSpinnerValues(dobMonthArrayAdapter,dob_month,"dobMonth");

        Integer dayValues[] = new Integer[31];
        for(int i = 0; i < 31; i++){
            dayValues[i] = Integer.valueOf(i+1);
        }
        ArrayAdapter<Integer> dobDayArrayAdapter = new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_item, dayValues);
        dobDayArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        dob_day.setAdapter(dobDayArrayAdapter);
        presetSpinnerValues(dobDayArrayAdapter,dob_day,"dobDay");

        Integer yearValues[] = new Integer[89];
        for(int i = 0; i < 89; i++){
            yearValues[i] = Integer.valueOf(1920+i);
        }
        ArrayAdapter<Integer> dobYearArrayAdapter = new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_item, yearValues);
        dobYearArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        dob_year.setAdapter(dobYearArrayAdapter);
        dob_year.setSelection(75);
        presetSpinnerValues(dobYearArrayAdapter,dob_year,"dobYear");
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
                if(mEmailAddress.length() > 0 && !Pattern.matches(EMAIL_REGEX, mEmailAddress.getText().toString())){
                    errorMessage+="Your email address is in an invalid format\n";
                    hasErrors = true;
                }
                if(mPersonalIdNumber.length() == 0){
                    hasErrors = true;
                    if(isCanada){
                        errorMessage+="SIN cannot be blank\n";
                    }else{
                        errorMessage+="SSN cannot be blank\n";
                    }
                }
                if(hasErrors){
                    DialogFragment fragment = InformationDialogFragment.newInstance(errorMessage,"Error");
                    fragment.show(getActivity().getFragmentManager(), "error");
                }else{
                    editSharedPreferences.putString("emailAddress",mEmailAddress.getText().toString());
                    editSharedPreferences.putString("personalIdNumber",mPersonalIdNumber.getText().toString());
                    editSharedPreferences.putString("dobMonth",dobMonthString);
                    editSharedPreferences.putString("dobDay",dobDayString);
                    editSharedPreferences.putString("dobYear",dobYearString);
                    editSharedPreferences.commit();
                    Fragment personalDetailsThirdFragment = new PaymentPersonalDetailThirdFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.fragmentContainer,personalDetailsThirdFragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack("PaymentPersonalDetailThirdFragment");
                    ft.commit();
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
