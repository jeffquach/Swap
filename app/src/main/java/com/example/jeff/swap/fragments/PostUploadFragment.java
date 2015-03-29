package com.example.jeff.swap.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.app.DialogFragment;
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
import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.TermsOfServiceActivity;

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
public class PostUploadFragment extends Fragment {

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

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                DialogFragment fragment = InformationDialogFragment.newInstance("By registering for direct payments you can have funds directly transferred to your bank account whenever someone buys one of your items");
                fragment.show(getActivity().getFragmentManager(), "error");
            }
        });
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
                // do another thing
                Toast.makeText(getActivity(),"Stripe's Terms of Service yo!",Toast.LENGTH_SHORT).show();
            }
        };

        ss.setSpan(span1, 46, 62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(span2, 71, 106, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsOfService.setText(ss);
        termsOfService.setMovementMethod(LinkMovementMethod.getInstance());

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
        return view;
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
                multipartEntity.addBinaryBody("locationOfImage", inputStream, ContentType.create("image/jpeg"), mImageFileName+".jpg");
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