package com.example.jeff.swap;

import android.annotation.SuppressLint;
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
 * Created by jeff on 15-03-17.
 */
public class PostImageCacheDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbNailDownloader";
    private static final int MESSAGE_CACHING = 1;
    Map<Token,String> requestMapCache = Collections.synchronizedMap(new HashMap<Token, String>());
    Handler mHandler;
    MemoryCache mMemoryCache;
    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private final int CACHE_SIZE = maxMemory / 8;

    public PostImageCacheDownloader(){
        super(TAG);
        mMemoryCache = MemoryCache.getMemoryCache(CACHE_SIZE);;
    }

    public MemoryCache getMemoryCache(){
        return mMemoryCache;
    }

    public void deleteAllItemsInMemoryCache(){
        mMemoryCache.evictAll();
    }

    public void queueImageCache(Token id, String url){
        requestMapCache.put(id,url);
        mHandler.obtainMessage(MESSAGE_CACHING,id).sendToTarget();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared(){
        // The reason why the cached bitmaps show up in the UI thread is because this method: 'Handler.handleMessage(Message)' exists on the main UI thread
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if (message.what == MESSAGE_CACHING){
                    Token id = (Token) message.obj;
//                    Log.i(TAG," request for URL @ ID: "+requestMapCache.get(id)+" ---> "+id);
                    handleCachingRequest(id);
                }
            }
        };
    }

    private void handleCachingRequest(final Token id){
        try{
            final String url = requestMapCache.get(id);
            if (url == null) return;
            final Bitmap bitmap;
//            if (mMemoryCache.getBitmapFromMemCache(url) != null){
//                bitmap = mMemoryCache.getBitmapFromMemCache(url);
//                Log.i("Cache prefetch test: ","Tingz be found in cache son!");
//            }else{
            if (mMemoryCache.getBitmapFromMemCache(url) == null){
                byte[] bitmapByte = new JSONParser().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
                mMemoryCache.addBitmapToMemoryCache(url,bitmap);
                Log.i("CachePrefetch INSERTION","Putting shiznit in cache son!");
            }
        }catch(IOException e){
            e.printStackTrace();
            Log.e(TAG, "Error loading image: " + e);
        }
    }

    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_CACHING);
        requestMapCache.clear();
    }
}
