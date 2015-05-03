package com.example.jeff.swap.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
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

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.JSONParser;
import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jeff on 15-04-08.
 */
public class PaymentPersonalDetailThirdFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editSharedPreferences;
    private String defaultCurrency = "usd";
    private String countryCode = "US";

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
    private final String ROUTING_NUMBER_REGEX = "\\d{9}";
    private boolean isCanada = false;
    private PaymentActivity paymentActivity;

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
        paymentActivity = (PaymentActivity) getActivity();
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
            defaultCurrency = "cad";
            countryCode = "CA";
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
            mTransitNumberEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!Pattern.matches(ROUTING_NUMBER_REGEX, mTransitNumberEditText.getText().toString())){
                        mTransitNumberEditText.setError("Your routing number must have 9 digits");
                    }
                }
            });
        }
        setFormValidators();
        paymentActivity.setOnBackPressedListener(new BaseBackPressedListener(paymentActivity));
        preseedSharedPrefData();
        return view;
    }

    private void preseedSharedPrefData(){
        if(!sharedPreferences.getString("bankAccountNumber","").equals("")){
            mBankAccountNumber.setText(sharedPreferences.getString("bankAccountNumber",""));
        }
        if(!sharedPreferences.getString("transitNumber","").equals("")){
            mTransitNumberEditText.setText(sharedPreferences.getString("transitNumber",""));
        }
        if((!sharedPreferences.getString("institutionNumber","").equals("")) && isCanada){
            mInstitutionNumberEditText.setText(sharedPreferences.getString("institutionNumber",""));
        }
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
                    DialogFragment fragment = InformationDialogFragment.newInstance(errorMessage,"Error");
                    fragment.show(getActivity().getFragmentManager(), "error");
                }else{
                    new SendPersonalInfo().execute();
                }
            }
        });
    }

    public class BaseBackPressedListener {
        private final Activity activity;

        public BaseBackPressedListener(Activity activity) {
            this.activity = activity;
        }

        public void doBack() {
            editSharedPreferences.putString("bankAccountNumber",mBankAccountNumber.getText().toString());
            editSharedPreferences.putString("transitNumber",mTransitNumberEditText.getText().toString());
            if(isCanada){
                editSharedPreferences.putString("institutionNumber",mInstitutionNumberEditText.getText().toString());
            }
            editSharedPreferences.commit();
        }
    }

    private class SendPersonalInfo extends AsyncTask<Void,Void,JSONObject>{

        @Override
        protected JSONObject doInBackground(Void... stuff) {
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair("country",countryCode));
            params.add(new BasicNameValuePair("transitNumber",mTransitNumberEditText.getText().toString()));
            if(isCanada){
                params.add(new BasicNameValuePair("institutionNumber",mInstitutionNumberEditText.getText().toString()));
            }
            params.add(new BasicNameValuePair("bankAccountNumber",mBankAccountNumber.getText().toString()));
            params.add(new BasicNameValuePair("currency",defaultCurrency));
            params.add(new BasicNameValuePair("email",sharedPreferences.getString("emailAddress","")));
            params.add(new BasicNameValuePair("address",sharedPreferences.getString("address","")));
            params.add(new BasicNameValuePair("city",sharedPreferences.getString("city","")));
            params.add(new BasicNameValuePair("state",sharedPreferences.getString("province","")));
            params.add(new BasicNameValuePair("postalCode",sharedPreferences.getString("postalCode","")));
            params.add(new BasicNameValuePair("firstName",sharedPreferences.getString("firstName","")));
            params.add(new BasicNameValuePair("lastName",sharedPreferences.getString("lastName","")));
            params.add(new BasicNameValuePair("birthdayDay",sharedPreferences.getString("dobDay","")));
            params.add(new BasicNameValuePair("birthdayMonth",sharedPreferences.getString("dobMonth","")));
            params.add(new BasicNameValuePair("birthdayYear",sharedPreferences.getString("dobYear","")));
            params.add(new BasicNameValuePair("personalIdNumber",sharedPreferences.getString("personalIdNumber","")));
            JSONObject jsonObject = new JSONParser().getJSONFromUrl(BuildConfig.SERVER_URL+"/stripe/newAccount",params);
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                String response = jsonObject.getString("response");
                if (response.equals("Success")){
                    showResponseMessage(jsonObject.getString("message"));
                }else if(response.equals("Error")){
                    showResponseMessage(jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showResponseMessage(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
