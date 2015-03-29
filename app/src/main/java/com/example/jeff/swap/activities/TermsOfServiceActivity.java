package com.example.jeff.swap.activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.JSONParser;
import com.example.jeff.swap.R;

/**
 * Created by jeff on 15-03-29.
 */
public class TermsOfServiceActivity extends ActionBarActivity {
    private FrameLayout frameLayout;
    private ScrollView scrollView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LoadTermsOfService().execute();
        setContentView(R.layout.activity_fragment);
        frameLayout = (FrameLayout) findViewById(R.id.fragmentContainer);
        scrollView = new ScrollView(this);
        textView = new TextView(this);
    }

    private class LoadTermsOfService extends AsyncTask<Void, Void, String>{

        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(TermsOfServiceActivity.this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jsonParser = new JSONParser();
            String result = jsonParser.getStringUsingHttp(BuildConfig.SERVER_URL+"/post/service");
            return result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            textView.setText(Html.fromHtml(result));
            scrollView.addView(textView);
            frameLayout.addView(scrollView);
        }
    }
}
