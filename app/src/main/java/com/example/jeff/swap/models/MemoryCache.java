package com.example.jeff.swap.models;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

/**
 * Created by jeff on 15-03-16.
 */
public class MemoryCache extends LruCache<String,Bitmap> {

    private static LruCache<String,Bitmap> mLruCache;
    private static MemoryCache mMemoryCache;

    public MemoryCache(int maxSize) {
        super(maxSize);
        mLruCache = new LruCache<String, Bitmap>(maxSize);
    }

    public static MemoryCache getMemoryCache(int cacheSize){
        if (mMemoryCache == null){
            mMemoryCache = new MemoryCache(cacheSize);
        }
        return mMemoryCache;
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mLruCache.get(key);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public int sizeOf(String key, Bitmap bitmap){
        return bitmap.getByteCount()/1024;
    }
}
