package com.example.jeff.swap.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.JSONParser;
import com.example.jeff.swap.R;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jeff on 15-04-04.
 */
public class PaymentFragment extends Fragment{

    private EditText mCreditCardNumber;
    private EditText mExpiryMonth;
    private EditText mExpiryYear;
    private EditText mCvc;
    private Button mSaveCardButton;
    private Button mPersonalDetailsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private TokenCallback mTokenCallback = new TokenCallback() {
        @Override
        public void onSuccess(Token token) {
            String tokenFromCallback = token.getId();
            new SendPayment().execute(tokenFromCallback);
        }
        @Override
        public void onError(Exception error) {
            handleError(error.getLocalizedMessage());
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_fragment, container, false);
        mCreditCardNumber = (EditText) view.findViewById(R.id.creditCardNumber);
        mExpiryMonth = (EditText) view.findViewById(R.id.expMonth);
        mExpiryYear = (EditText) view.findViewById(R.id.expYear);
        mCvc = (EditText) view.findViewById(R.id.cvc);
        mSaveCardButton = (Button) view.findViewById(R.id.saveCreditCard);
        mSaveCardButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String creditCardNumber = mCreditCardNumber.getText().toString();
                int expiryMonth = Integer.parseInt(mExpiryMonth.getText().toString());
                int expiryYear = Integer.parseInt(mExpiryYear.getText().toString());
                String cvc = mCvc.getText().toString();
                Card card = new Card(creditCardNumber,expiryMonth,expiryYear,cvc);
                boolean validation = card.validateCard();
                if (validation) {
                    new Stripe().createToken(card, BuildConfig.STRIPE_PUBLISHABLE_KEY,mTokenCallback);
                } else if (!card.validateNumber()) {
                    handleError("The card number that you entered is invalid");
                } else if (!card.validateExpiryDate()) {
                    handleError("The expiration date that you entered is invalid");
                } else if (!card.validateCVC()) {
                    handleError("The CVC code that you entered is invalid");
                } else {
                    handleError("The card details that you entered are invalid");
                }
            }
        });
        mPersonalDetailsButton = (Button) view.findViewById(R.id.personalDetails);
        mPersonalDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment personalDetailsFragment = new PaymentPersonalDetailFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainer,personalDetailsFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack("PaymentPersonalDetailFragment");
                ft.commit();
            }
        });
        return view;
    }

    private void handleError(String error) {
        DialogFragment fragment = InformationDialogFragment.newInstance(error,null);
        fragment.show(getActivity().getFragmentManager(), "error");
    }

    private class SendPayment extends AsyncTask<String,Void,JSONObject> {

        @Override
        protected JSONObject doInBackground(String... token) {
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair("token",token[0].toString()));
            JSONObject jsonObject = new JSONParser().getJSONFromUrl(BuildConfig.SERVER_URL+"/stripe/receiveToken",params);
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject){
            try {
                String response = jsonObject.getString("response");
                if (response.equals("Success")){
                    showPaymentTransactionMethod(jsonObject.getString("message"));
                }else if(response.equals("Error")){
                    showPaymentTransactionMethod(jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPaymentTransactionMethod(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
