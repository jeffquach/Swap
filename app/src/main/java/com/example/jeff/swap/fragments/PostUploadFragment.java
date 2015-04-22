package com.example.jeff.swap.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.GPSBackgroundService;
import com.example.jeff.swap.ProximityIntentReceiver;
import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PaymentActivity;
import com.example.jeff.swap.activities.TermsOfServiceActivity;
import com.example.jeff.swap.models.MockLocationProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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

/**
 * Created by jeff on 15-03-07.
 */
public class PostUploadFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

    private Bitmap bitmapToUpload;
    protected Location mLastLocation;
    protected GoogleApiClient mGoogleApiClient;
    protected Boolean mRequestingLocationUpdates;
    protected LocationRequest mLocationRequest;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private Button mStartGPS;
    private Button mStopGPS;
    private Button mProximityAlert;
    private EditText mockLatitudeEditText;
    private EditText mockLongitudeEditText;
    private Button mockPositionButton;
    private EditText fakeLatitudeEditText;
    private EditText fakeLongitudeEditText;
    private LocationManager locationManager;
    private static final String PROXIMITY_ALERT_INTENT = "com.example.jeff.swap.fragments.ProximityAlert";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String ACTION_LOCATION = "com.example.jeff.swap.fragments.ACTION_LOCATION";

    private ProximityIntentReceiver mProximityReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        MockLocationProvider mockLocationProvider = new MockLocationProvider(LocationManager.NETWORK_PROVIDER,getActivity());
        mockLocationProvider.pushLocation(39.946682, 116.355316);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, getLocationPendingIntent(true));
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
        Log.i("onResume","$$$ onResume called $$$");
        showTermsOfServiceMessage();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("onPause","$$$ onPause called $$$");
        stopLocationUpdates();
//        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
//            startLocationUpdates();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("onDestroyView","$$$ onDestroyView called $$$");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "$$$ onDestroy called $$$");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void addProximityAlert(double latitude, double longitude){
        Intent intent = new Intent(PROXIMITY_ALERT_INTENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0,intent,0);
        locationManager.addProximityAlert(latitude,longitude,1000f,-1,pendingIntent);
        mProximityReceiver = new ProximityIntentReceiver();
        getActivity().registerReceiver(mProximityReceiver,new IntentFilter(PROXIMITY_ALERT_INTENT));
        Toast.makeText(getActivity(),"New proximity alert created! Lat: "+latitude+", longitude: "+longitude,Toast.LENGTH_SHORT).show();
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(getActivity(), 0, broadcast, flags);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        PendingIntent pendingIntent = getLocationPendingIntent(true);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
//        PendingIntent pendingIntent = getLocationPendingIntent(false);
//        Log.i("LOCATION YO!","$$$$$ pendingIntent != null $$$$$: "+(pendingIntent != null));
//        if (pendingIntent != null){
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//            pendingIntent.cancel();
//        }
    }

    @Override
    public void onLocationChanged(Location location) {
        float distance = mLastLocation.distanceTo(location);
        Toast.makeText(getActivity(),"Distance from previous location: "+distance,Toast.LENGTH_LONG).show();
        mLastLocation = location;
        Log.i("LOCATION CHANGED","$$$$$ mLastLocation.getLatitude() $$$$$: "+(mLastLocation.getLatitude()));
        Log.i("LOCATION CHANGED","$$$$$ mLastLocation.getLongitude() $$$$$: "+(mLastLocation.getLongitude()));
        mLatitudeTextView.setText("Latitude: "+(Double.toString(mLastLocation.getLatitude())));
        mLongitudeTextView.setText("Longitude: "+(Double.toString(mLastLocation.getLongitude())));
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i("LAATITUDE","$$$$$$ String.valueOf(mLastLocation.getLatitude()) $$$$$$$: "+(String.valueOf(mLastLocation.getLatitude())));
            Log.i("LONGITUDE","$$$$$ String.valueOf(mLastLocation.getLongitude()) $$$$$: "+(String.valueOf(mLastLocation.getLongitude())));
            Toast.makeText(getActivity(),"Latitude: "+(String.valueOf(mLastLocation.getLatitude()))+"\nLongitude: "+(String.valueOf(mLastLocation.getLongitude())),Toast.LENGTH_LONG).show();
            mLatitudeTextView.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mLastLocation.getLongitude()));
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("STUFF", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("STUFF", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
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
        mLatitudeTextView = (TextView) view.findViewById(R.id.latitude);
        mLongitudeTextView = (TextView) view.findViewById(R.id.longitude);
        mStartGPS = (Button) view.findViewById(R.id.startGPS);
        mStopGPS = (Button) view.findViewById(R.id.stopGPS);
        mockLatitudeEditText = (EditText) view.findViewById(R.id.mockLatitude);
        mockLongitudeEditText = (EditText) view.findViewById(R.id.mockLongitude);
        mProximityAlert = (Button) view.findViewById(R.id.proximityAlert);
        fakeLatitudeEditText = (EditText) view.findViewById(R.id.fakeLatitude);
        fakeLongitudeEditText = (EditText) view.findViewById(R.id.fakeLongitude);
        mockPositionButton = (Button) view.findViewById(R.id.setFakeLocation);

        String username = sharedPreferences.getString("USERNAME","");
        String phoneNumber = sharedPreferences.getString("REGISTRATION_PHONE_NUMBER","");
        Toast.makeText(getActivity(),"USERNAME: "+username+", NUMBER: "+phoneNumber,Toast.LENGTH_LONG).show();

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
//                SharedPreferences.Editor sharedPrefService = getActivity().getSharedPreferences("GPSBackgroundService",0).edit();
//                sharedPrefService.putBoolean("shouldCancel",true).commit();
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
                Toast.makeText(getActivity(), "Tingz be uploaded son!", Toast.LENGTH_LONG).show();

                new UploadTask().execute();
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
        registerBankAccountHelp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DialogFragment fragment = InformationDialogFragment.newInstance("By registering for direct payments you can have funds directly transferred to your bank account whenever someone buys one of your items","Notice");
                fragment.show(getActivity().getFragmentManager(), "error");
            }
        });

        photoContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                photoContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                imageViewHeight = photoContainer.getMeasuredHeight();
                imageViewWidth = photoContainer.getMeasuredWidth();
                Log.i("HEIGHT","$$$ imageViewHeight (LISTENER) is $$$: "+imageViewHeight);
                Log.i("WIDTH","$$$ imageViewWidth (LISTENER) is $$$: "+imageViewWidth);
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
                Toast.makeText(getActivity(),"Country: "+country+" , Country name: "+countryName,Toast.LENGTH_LONG).show();
            }
        });
        buildGoogleApiClient();
        mRequestingLocationUpdates = false;

        mProximityAlert.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                double latitude = Double.parseDouble(mockLatitudeEditText.getText().toString());
                SharedPreferences sharedPreferencesInner = getActivity().getSharedPreferences("GPSBackgroundService",0);
                SharedPreferences.Editor editSharedPref = sharedPreferencesInner.edit();
                editSharedPref.putFloat("desiredDistance",((float)latitude)).commit();
                Toast.makeText(getActivity(),"YALL entered: "+(sharedPreferencesInner.getFloat("desiredDistance",0f)),Toast.LENGTH_LONG).show();
            }
        });
//        mockPositionButton.setOnClickListener(new View.OnClickListener(){
//            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//            @Override
//            public void onClick(View v) {
////                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER,true);
////                Location fakeLocation = new Location(LocationManager.GPS_PROVIDER);
////                fakeLocation.setLatitude(Double.parseDouble(fakeLatitudeEditText.getText().toString()));
////                fakeLocation.setLongitude(Double.parseDouble(fakeLongitudeEditText.getText().toString()));
////                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, fakeLocation);
////                Toast.makeText(getActivity(),"Your new location yo, latitude: "+(fakeLocation.getLatitude())+" , longitude: "+(fakeLocation.getLongitude()),Toast.LENGTH_LONG).show();
//
//                final String providerName = "MyFancyGPSProvider";
//                locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
////Remember to remove your your provider before using it or after.
////In other case it won't be remove till restarting the phone.
//                if (locationManager.getProvider(providerName) != null) {
//                    locationManager.removeTestProvider(providerName);
//                }
//                locationManager.addTestProvider(providerName, true, false, false, false, true, true, true,
//                        Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
//                locationManager.setTestProviderEnabled(providerName, true);
//                Location loc = new Location(providerName);
//                loc.setLongitude(Double.parseDouble(fakeLatitudeEditText.getText().toString()));
//                loc.setTime(System.currentTimeMillis());
//                loc.setLatitude(Double.parseDouble(fakeLongitudeEditText.getText().toString()));
//                loc.setAccuracy(5.555f);
//                loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
//                locationManager.setTestProviderLocation(providerName, loc);
//            }
//        });
//        addProximityAlert(43.661570,-79.469482);
//
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                addProximityAlert(43.661570,-79.469482);
//            }
//        }, 10000);
        return view;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(2f);
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

            DefaultHttpClient httpclient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost(BuildConfig.SERVER_URL+"/post/new"); // server

                MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
                multipartEntity.addTextBody("username",username);
                multipartEntity.addTextBody("phoneNumber",phoneNumber);
                multipartEntity.addTextBody("title",post_title);
                multipartEntity.addTextBody("description",post_description);
                multipartEntity.addTextBody("city",post_city);
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