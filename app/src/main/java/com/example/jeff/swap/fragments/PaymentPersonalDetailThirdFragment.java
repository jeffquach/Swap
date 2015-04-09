package com.example.jeff.swap.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;

/**
 * Created by jeff on 15-04-08.
 */
public class PaymentPersonalDetailThirdFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editSharedPreferences;

    private EditText mBankAccountNumber;
    private EditText mTransitNumber;
    private EditText mInstitutionNumber;
    private EditText mRoutingNumber;
    private Button mNextButton;
    private final String PERSONAL_ID_NUMBER_REGEX = "\\d*";
    private boolean isCanada = false;
    private TableLayout mBankTableLayout;

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
        View view = inflater.inflate(R.layout.payment_personal_detail_third_fragment, container, false);
        PaymentActivity paymentActivity = (PaymentActivity) getActivity();
        paymentActivity.getSupportActionBar().setTitle("Banking Information");
        mBankTableLayout = (TableLayout) view.findViewById(R.id.bankTableLayout);
        mBankAccountNumber = (EditText) view.findViewById(R.id.bank_account_number);
        mNextButton = (Button) view.findViewById(R.id.nextButton);

        if(sharedPreferences.getString("country","").equals("Canada")){
            isCanada = true;
        }
        if(isCanada){
            Context context = getActivity();
            TableRow tableRow = new TableRow(context);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tableRow.setOrientation(LinearLayout.HORIZONTAL);
            TextView transitNumber = new TextView(context);
            transitNumber.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f));
            transitNumber.setText("Transit Number");
            TextView institutionNumber = new TextView(context);
            institutionNumber.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f));
            institutionNumber.setText("Institution Number");
            tableRow.addView(transitNumber);
            tableRow.addView(institutionNumber);
            mBankTableLayout.addView(tableRow,2);

        }
        return view;
    }

    private void setFormValidators(){
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String errorMessage = "";
                boolean hasErrors = false;
                if(mBankAccountNumber.length() == 0){
                    errorMessage+="Bank account number cannot be blank\n";
                    hasErrors = true;
                }
//                if(mPersonalIdNumber.length() == 0){
//                    hasErrors = true;
//                    if(isCanada){
//                        errorMessage+="SIN cannot be blank\n";
//                    }else{
//                        errorMessage+="SSN cannot be blank\n";
//                    }
//                }
                if(hasErrors){
                    DialogFragment fragment = InformationDialogFragment.newInstance(errorMessage,"Error");
                    fragment.show(getActivity().getFragmentManager(), "error");
                }else{
                    Toast.makeText(getActivity(), "Everything good to go yo!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
