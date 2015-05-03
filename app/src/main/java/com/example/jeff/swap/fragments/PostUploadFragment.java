package com.example.jeff.swap.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.GPSBackgroundService;
import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;
import com.example.jeff.swap.activities.TermsOfServiceActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by jeff on 15-03-07.
 */
public class PostUploadFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private SharedPreferences sharedPreferences;
    private Button takePhotoButton;
    private Button uploadPhotoButton;
    private EditText postTitle;
    private EditText postDescription;
    private EditText postCity;
    private ImageView photoContainer;
    private TextView termsOfService;
    private Button registerBankAccount;
    private ImageButton registerBankAccountHelp;
    private static String mCurrentPhotoPath;
    private static String mImageFileName;
    private static int imageViewHeight;
    private static int imageViewWidth;
    private boolean sendDataWithPicture = false;
    private CheckBox currentLocationCheckbox;
    private CheckBox customLocationCheckbox;
    private LinearLayout postUploadLinearLayout;
    private EditText customAddress;
    private EditText customProvince;
    private EditText customPostalCode;
    private LinearLayout customLocationLinearLayout;

    private Bitmap bitmapToUpload;
    private Button mStartGPS;
    private Button mStopGPS;
    private EditText mockLatitudeEditText;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLocationLatitude = 0;
    private double currentLocationLongitude = 0;
    private boolean useCurrentLocation;
    private static final String ACTION_LOCATION = "com.example.jeff.swap.GPS_SERVICE_ACTION_LOCATION-POST-UPLOAD";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private BroadcastReceiver backgroundLocationReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            currentLocationLatitude = location.getLatitude();
            currentLocationLongitude = location.getLongitude();
            Log.e("BACKGROUND RECEIVER","$$$$ Latitude: $$$$$: "+currentLocationLatitude+", longitude: "+currentLocationLongitude);
            Log.e("BACKGROUND RECEIVER","$$$$$ location.getAccuracy() $$$$$: "+(location.getAccuracy()));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        Log.i("onCreate", "$$$ onCreate called $$$");
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("onResume", "$$$ onResume called $$$");
        showTermsOfServiceMessage();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("onPause", "$$$ onPause called $$$");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("onDestroyView", "$$$ onDestroyView called $$$");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "$$$ onDestroy called $$$");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView","$$$ onCreateView called $$$");
        sharedPreferences = getActivity().getSharedPreferences("Chat",0);
        View view = inflater.inflate(R.layout.post_upload_fragment,container,false);
        takePhotoButton = (Button) view.findViewById(R.id.take_photo);
        uploadPhotoButton = (Button) view.findViewById(R.id.send_post);
        postTitle = (EditText) view.findViewById(R.id.post_title);
        postDescription = (EditText) view.findViewById(R.id.post_description);
        postCity = (EditText) view.findViewById(R.id.post_city);
        photoContainer = (ImageView) view.findViewById(R.id.post_photo);
        registerBankAccount = (Button) view.findViewById(R.id.register_bank_account);
        registerBankAccountHelp = (ImageButton) view.findViewById(R.id.register_bank_account_help);
        termsOfService = (TextView) view.findViewById(R.id.terms_of_service);
        mStartGPS = (Button) view.findViewById(R.id.startGPS);
        mStopGPS = (Button) view.findViewById(R.id.stopGPS);
        mockLatitudeEditText = (EditText) view.findViewById(R.id.mockLatitude);
        currentLocationCheckbox = (CheckBox) view.findViewById(R.id.currentLocation);
        customLocationCheckbox = (CheckBox) view.findViewById(R.id.customLocation);
        postUploadLinearLayout = (LinearLayout) view.findViewById(R.id.postUploadLinearLayout);
        Button customButton = (Button) view.findViewById(R.id.customLocationButton);
        customButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {


            }
        });

        currentLocationCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocationCheckbox.isChecked()) {
                    Toast.makeText(getActivity(), "YE current location yo!", Toast.LENGTH_LONG).show();
                }
                if (customLocationCheckbox.isChecked()) {
                    customLocationCheckbox.setChecked(false);
                    customLocationCheckbox.setEnabled(true);
                }
                if(customAddress != null && customLocationLinearLayout != null){
                    ((LinearLayout)customAddress.getParent()).removeView(customAddress);
                    ((LinearLayout)customLocationLinearLayout.getParent()).removeView(customLocationLinearLayout);
                }
                useCurrentLocation = true;
            }
        });

        customLocationCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customLocationCheckbox.isChecked()){
                    Toast.makeText(getActivity(),"YE CUSTOM location yo!",Toast.LENGTH_LONG).show();
                }
                if (currentLocationCheckbox.isChecked()) {
                    currentLocationCheckbox.setChecked(false);
                }
                customAddress = new EditText(getActivity());
                customAddress.setId(R.id.post_custom_location);
                customAddress.setInputType(InputType.TYPE_CLASS_TEXT);
                customAddress.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                customAddress.setHorizontallyScrolling(false);
                customAddress.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                customAddress.setHint("Enter address");
                postUploadLinearLayout.addView(customAddress, 7);
                customLocationLinearLayout = new LinearLayout(getActivity());
                customLocationLinearLayout.setId(R.id.post_custom_location_linear_layout);
                customLocationLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                customProvince = new EditText(getActivity());
                customProvince.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                customProvince.setHint("Enter state / province");
                customProvince.setInputType(InputType.TYPE_CLASS_TEXT);
                customProvince.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                customProvince.setHorizontallyScrolling(false);
                customPostalCode = new EditText(getActivity());
                customPostalCode.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                customPostalCode.setInputType(InputType.TYPE_CLASS_TEXT);
                customPostalCode.setHint("Enter zip code / postal code");
                customPostalCode.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                customPostalCode.setHorizontallyScrolling(false);
                customLocationLinearLayout.addView(customProvince);
                customLocationLinearLayout.addView(customPostalCode);
                postUploadLinearLayout.addView(customLocationLinearLayout, 8);
                customLocationCheckbox.setEnabled(false);
                useCurrentLocation = false;
            }
        });

        mStartGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(mockLatitudeEditText.getText().toString().equals(""))){
                    SharedPreferences sharedPreferencesInner = getActivity().getSharedPreferences("GPSBackgroundService", 0);
                    SharedPreferences.Editor editSharedPref = sharedPreferencesInner.edit();
                    double latitude = Double.parseDouble(mockLatitudeEditText.getText().toString());
                    editSharedPref.putFloat("desiredDistance", ((float) latitude)).commit();
                }
                Intent intent = new Intent(getActivity(), GPSBackgroundService.class);
                getActivity().startService(intent);
            }
        });
        mStopGPS.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getActivity().stopService(new Intent(getActivity(), GPSBackgroundService.class));
            }
        });

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    Log.i("IN ONCLICK","$$$ mCurrentPhotoPath: $$$"+mCurrentPhotoPath);
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String errorMessage = "";
                boolean hasErrors = false;
                if(postTitle.length() == 0){
                    errorMessage+="Title cannot be blank\n";
                    hasErrors = true;
                }else if(postTitle.length() > 0 && postTitle.length() < 10){
                    errorMessage+="Title must contain at least 10 characters\n";
                    hasErrors = true;
                }
                if(postDescription.length() == 0){
                    errorMessage+="Description cannot be blank\n";
                    hasErrors = true;
                }else if(postDescription.length() > 0 && postDescription.length() < 25){
                    errorMessage+="Title must contain at least 25 characters\n";
                    hasErrors = true;
                }
                if(postCity.length() == 0){
                    errorMessage+="City cannot be blank\n";
                    hasErrors = true;
                }
                if(!currentLocationCheckbox.isChecked() && !customLocationCheckbox.isChecked()){
                    errorMessage+="Location of item cannot be blank\n";
                    hasErrors = true;
                }
                if(!useCurrentLocation && customAddress != null && customPostalCode != null && customProvince != null){
                    if (customAddress.length() == 0){
                        errorMessage+="Address cannot be blank\n";
                        hasErrors = true;
                    }
                    if (customProvince.length() == 0){
                        errorMessage+="Province / state cannot be blank\n";
                        hasErrors = true;
                    }
                    if (customPostalCode.length() == 0){
                        errorMessage+="Postal code / zip code cannot be blank\n";
                        hasErrors = true;
                    }
                }
                if(hasErrors){
                    DialogFragment fragment = InformationDialogFragment.newInstance(errorMessage,"Error");
                    fragment.show(getActivity().getFragmentManager(), "error");
                }else{
                    if (!useCurrentLocation){
                        String address = customAddress.getText().toString();
                        String postalCode = customPostalCode.getText().toString();
                        String province = customProvince.getText().toString();
                        String city = postCity.getText().toString();
                        Geocoder geocoder = new Geocoder(getActivity());
                        try {
                            List<Address> addresses = geocoder.getFromLocationName((address+", "+city+", "+province+", "+postalCode),10);
                            currentLocationLatitude = addresses.get(0).getLatitude();
                            currentLocationLongitude = addresses.get(0).getLongitude();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    new UploadTask().execute();
                }
            }
        });
        if (mCurrentPhotoPath != null){
            setPic();
        }
        registerBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Register you bank account yo!", Toast.LENGTH_SHORT).show();
            }
        });
        registerBankAccountHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = InformationDialogFragment.newInstance("By registering for direct payments you can have funds directly transferred to your bank account whenever someone buys one of your items", "Notice");
                fragment.show(getActivity().getFragmentManager(), "error");
            }
        });

        photoContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                photoContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                imageViewHeight = photoContainer.getMeasuredHeight();
                imageViewWidth = photoContainer.getMeasuredWidth();
                Log.i("HEIGHT", "$$$ imageViewHeight (LISTENER) is $$$: " + imageViewHeight);
                Log.i("WIDTH", "$$$ imageViewWidth (LISTENER) is $$$: " + imageViewWidth);
                return true;
            }
        });

        Button paymentTest = (Button) view.findViewById(R.id.paymentTest);
        paymentTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                startActivity(intent);
            }
        });
        Button showCountry = (Button) view.findViewById(R.id.countryShow);
        showCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country = getActivity().getResources().getConfiguration().locale.getCountry();
                String countryName = getActivity().getResources().getConfiguration().locale.getDisplayCountry();
                Toast.makeText(getActivity(), "Country: " + country + " , Country name: " + countryName, Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }

    private void showTermsOfServiceMessage(){
        SpannableString ss = new SpannableString("By registering your account, you agree to our Terms of Service and the Stripe Connected Account Agreement.");
        ClickableSpan span1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent intent = new Intent(getActivity(),TermsOfServiceActivity.class);
                startActivity(intent);
            }
        };

        ClickableSpan span2 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://stripe.com/connect/terms"));
                startActivity(browserIntent);
            }
        };

        ss.setSpan(span1, 46, 62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(span2, 71, 106, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsOfService.setText(ss);
        termsOfService.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mImageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                mImageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Log.i("IN GALLERYADDPIC","$$$ mCurrentPhotoPath: $$$"+mCurrentPhotoPath);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void setPic(){
        // Get the dimensions of the View
        int targetW = imageViewWidth;
        int targetH = imageViewHeight;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Log.i("IN SETPIC","$$$ mCurrentPhotoPath: $$$"+mCurrentPhotoPath);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.i("photoW","$$$ photoW is AND targetW $$$: "+photoW+" -- "+targetW);
        Log.i("photoH","$$$ photoH is AND targetH $$$: "+photoH+" -- "+targetH);

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        Log.i("scaleFactor","$$$ scaleFactor $$$: "+scaleFactor);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Log.i("scalefactor","$$$ scalefactor is $$$: "+scaleFactor);
        Log.i("SETPIC","$$$ mImageFileName $$$: "+mImageFileName);
        bitmapToUpload = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        photoContainer.setImageBitmap(bitmapToUpload);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //photoContainer.setImageBitmap(imageBitmap);
            setPic();
            sendDataWithPicture = true;
            galleryAddPic();
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_LOCATION), PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, pendingIntent);
        getActivity().registerReceiver(backgroundLocationReceiver, new IntentFilter(ACTION_LOCATION));
        Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, pendingIntent);
                getActivity().unregisterReceiver(backgroundLocationReceiver);
            }
        }, 30000);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class UploadTask extends AsyncTask<Bitmap, Void, Void> {

        protected Void doInBackground(Bitmap... bitmaps) {
//            if (bitmaps[0] == null)
//                return null;
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, options);
            //Bitmap bitmap = bitmaps[0];

            ByteArrayOutputStream byteArrayStream = null;
            InputStream inputStream = null;
            if (sendDataWithPicture){
                byteArrayStream = new ByteArrayOutputStream();
                bitmapToUpload.compress(Bitmap.CompressFormat.PNG, 100, byteArrayStream);
                inputStream = new ByteArrayInputStream(byteArrayStream.toByteArray());
            }

            String username = sharedPreferences.getString("USERNAME","");
            String phoneNumber = sharedPreferences.getString("REGISTRATION_PHONE_NUMBER","");
            String post_title = postTitle.getText().toString();
            String post_description = postDescription.getText().toString();
            String post_city = postCity.getText().toString();
            String latitude = String.valueOf(currentLocationLatitude);
            String longitude = String.valueOf(currentLocationLongitude);

            DefaultHttpClient httpclient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost(BuildConfig.SERVER_URL+"/post/new"); // server

                MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
                multipartEntity.addTextBody("username",username);
                multipartEntity.addTextBody("phoneNumber",phoneNumber);
                multipartEntity.addTextBody("title",post_title);
                multipartEntity.addTextBody("description",post_description);
                multipartEntity.addTextBody("city",post_city);
                if (currentLocationLongitude == 0 || currentLocationLongitude == 0){
                    DialogFragment fragment = InformationDialogFragment.newInstance("Please wait a moment, the location of the device is being computed","Notice");
                    fragment.show(getActivity().getFragmentManager(), "notice");
                }else{
                    multipartEntity.addTextBody("latitude",latitude);
                    multipartEntity.addTextBody("longitude",longitude);
                }
                if(inputStream != null){
                    multipartEntity.addBinaryBody("locationOfImage", inputStream, ContentType.create("image/jpeg"), mImageFileName+".jpg");
                }
                httpPost.setEntity(multipartEntity.build());

                Log.i("DIRK", "request " + httpPost.getRequestLine());
                HttpResponse response = null;
                try {
                    response = httpclient.execute(httpPost);
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    if (response != null)
                        Log.i("DIRK", "response " + response.getStatusLine().toString());
                } finally {

                }
            } finally {

            }
            if (sendDataWithPicture){
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (byteArrayStream != null) {
                    try {
                        byteArrayStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            //Toast.makeText(getActivity(), "Tingz be uploaded son!", Toast.LENGTH_LONG).show();
        }
    }
}