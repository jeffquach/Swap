package com.example.jeff.swap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.jeff.swap.models.MemoryCache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeff on 15-03-15.
 */
public class PostImageDownloader<Token> extends HandlerThread {
    private static final String TAG = "PostImageDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private final static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    public final static int CACHE_SIZE = maxMemory / 8;

    private MemoryCache mCache = null;

    Handler mHandler;
    Map<Token,String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    Handler mResponseHandler;
    Listener<Token> mListener;

    public interface Listener<Token>{
        void onPostImageDownloaded(Token token, Bitmap postImage);
    }

    public void setListener(Listener<Token> listener){
        mListener = listener;
    }

    public PostImageDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
        mCache = MemoryCache.getMemoryCache(CACHE_SIZE);
        Log.i("CACHE SIZE","$$$ CACHE SIZE IS $$$: "+CACHE_SIZE);
    }


    // 'handleMessage(Message)' is called within 'onLooperPrepared()' because 'onLooperPrepared()' is called before the 'Looper' checks the message queue for the 1st time, therefore making it a good spot to implement the 'Handler'
    @Override
    protected void onLooperPrepared(){
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == MESSAGE_DOWNLOAD){
                    @SuppressWarnings("unchecked")
                    Token token = (Token) message.obj;
                    Log.i(TAG, "Got a request for URL: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueImage(Token token,String url){
        Log.i(TAG,"Got an URL: "+url);
        requestMap.put(token,url);
        mHandler.obtainMessage(MESSAGE_DOWNLOAD,token).sendToTarget();
    }

    private void handleRequest(final Token token){
        final Bitmap bitmap;
        try{
            final String url = requestMap.get(token);
            if (url == null) return;
            Log.i(TAG,"$$$ CACHE == null: $$$ "+(mCache == null));
            Log.i(TAG,"$$$ CACHE SIZE BEFORE: $$$ "+(mCache.size()));
            if (mCache.getBitmapFromMemCache(url) != null){
                bitmap = mCache.getBitmapFromMemCache(url);
                Log.i(TAG,"$$$ Bitmap OBTAINED FROM CACHE $$$");
                Log.i(TAG,"$$$ CACHE SIZE: $$$ "+(mCache.size()));
            }else{
                byte[] bitmapBytes = new JSONParser().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
                mCache.addBitmapToMemoryCache(url,bitmap);
                Log.i(TAG,"Bitmap created NOT CACHED");
            }

            // This method is here to send a custom message to your UI thread where you can customize the response and override methods to do things that you want to be done on the UI thread
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url) return;
                    requestMap.remove(token);
                    mListener.onPostImageDownloaded(token,bitmap);
                }
            });

        }catch (IOException ioe){
            Log.e(TAG,"Error downloading image ",ioe);
        }
    }

    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
