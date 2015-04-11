package com.example.jeff.swap.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private TableRow mDynamicTableRow;
    private LinearLayout mDynamicLinearLayout;
    private TextView mTransitNumberTextView;
    private TextView mInstitutionNumberTextView;
    private EditText mTransitNumberEditText;
    private EditText mInstitutionNumberEditText;
    private TextView mBankAccountHelp;
    private Button mNextButton;
    private final String PERSONAL_ID_NUMBER_REGEX = "\\d*";
    private boolean isCanada = false;

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
        frameLayout.addView(inflater.inflate(R.layout._payment_personal_detail_third_partial, tableLayout, true));
        paymentActivity.getSupportActionBar().setTitle("Banking Information");
        mBankAccountNumber = (EditText) view.findViewById(R.id.bank_account_number);
        mTransitNumberTextView = (TextView) view.findViewById(R.id.transitNumberTextView);
        mTransitNumberEditText = (EditText) view.findViewById(R.id.transitNumberEditText);
        mBankAccountHelp = (TextView) view.findViewById(R.id.bankAccountHelp);
        mNextButton = (Button) view.findViewById(R.id.sendButton);

        if(sharedPreferences.getString("country","").equals("Canada")){
            isCanada = true;
        }

        if(isCanada){
            mTransitNumberTextView.setText("Transit number");
            mTransitNumberEditText.setHint("Transit number");
            Context context = (PaymentActivity) getActivity();
            TableRow tableRow = (TableRow) view.findViewById(R.id.bankNumbersTableRow);
            TextView institutionNumber = new TextView(context);
            institutionNumber.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f));
            institutionNumber.setText("Institution Number");
            tableRow.addView(institutionNumber);
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.bankNumbersLinearLayout);
            mInstitutionNumberEditText = new EditText(context);
            mInstitutionNumberEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT,1.0f));
            mInstitutionNumberEditText.setHint("Institution number");
            mInstitutionNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            mInstitutionNumberEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            linearLayout.addView(mInstitutionNumberEditText);
        }else{
            mTransitNumberTextView.setText("Routing number");
            mTransitNumberEditText.setHint("Routing number");
        }
        setFormValidators();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCanada){
            bankAccountHelpLink("Need help finding your transit number or institution number?", "https://www.tdcanadatrust.com/lending/images/cheque.jpg",60);
        }else{
            bankAccountHelpLink("Need help finding your routing number?", "http://static.nationwide.com/static/Bank_Check.gif?r=42",38);
        }
    }

    private void bankAccountHelpLink(String helpMessage, final String helpUrl, int endInt){
        SpannableString spannableString = new SpannableString(helpMessage);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl));
                startActivity(browserIntent);
            }
        };
        spannableString.setSpan(clickableSpan,0,endInt,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBankAccountHelp.setText(spannableString);
        mBankAccountHelp.setMovementMethod(LinkMovementMethod.getInstance());
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
                if(mTransitNumberEditText.length() == 0){
                    hasErrors = true;
                    if (isCanada){
                        errorMessage+="Transit number cannot be blank\n";
                    }else{
                        errorMessage+="Routing number cannot be blank\n";
                    }
                }
                if(isCanada && mInstitutionNumberEditText.length() == 0){
                    errorMessage+="Institution number cannot be blank\n";
                    hasErrors = true;
                }
                if(hasErrors){
                    editSharedPreferences.putString("bankAccountNumber",mBankAccountNumber.getText().toString());
                    editSharedPreferences.putString("transitNumber",mTransitNumberEditText.getText().toString());
                    if(isCanada){
                        editSharedPreferences.putString("institutionNumber",mInstitutionNumberEditText.getText().toString());
                    }
                    editSharedPreferences.commit();
                    DialogFragment fragment = InformationDialogFragment.newInstance(errorMessage,"Error");
                    fragment.show(getActivity().getFragmentManager(), "error");
                }else{
                    Toast.makeText(getActivity(), "Everything good to go yo!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
