package com.example.jeff.swap.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jeff.swap.PostImageDownloader;
import com.example.jeff.swap.R;
import com.example.jeff.swap.models.MemoryCache;

/**
 * Created by jeff on 15-03-25.
 */
public class PostShowFragment extends Fragment {

    private TextView mPostTitleView;
    private TextView mPostDescriptionView;
    private TextView mPostCityView;
    private TextView mPostUsernameView;
    private ImageView mPostImageView;
    private String mPostImageUrl;
    private Bitmap mPostBitmap;

    private MemoryCache mCache = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCache = MemoryCache.getMemoryCache(PostImageDownloader.CACHE_SIZE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        String postId = intent.getStringExtra("id");
        String postTitle = intent.getStringExtra("title");
        String postDescription = intent.getStringExtra("description");
        String postCity = intent.getStringExtra("city");
        String postUsername = intent.getStringExtra("username");
        String postImageUrl = intent.getStringExtra("imageUrl");
        View view = inflater.inflate(R.layout.post_show_fragment, container, false);

        mPostTitleView = (TextView) view.findViewById(R.id.post_title);
        mPostDescriptionView = (TextView) view.findViewById(R.id.post_description);
        mPostCityView = (TextView) view.findViewById(R.id.post_city);
        mPostUsernameView = (TextView) view.findViewById(R.id.post_username);
        mPostImageView = (ImageView) view.findViewById(R.id.post_photo);

        if (mCache.getBitmapFromMemCache(postImageUrl) != null){
            mPostBitmap = mCache.getBitmapFromMemCache(postImageUrl);
        }
        mPostTitleView.setText("Title: "+postTitle);
        mPostImageView.setImageBitmap(mPostBitmap);
        mPostDescriptionView.setText("Description: "+postDescription);
        mPostUsernameView.setText("User: "+postUsername);
        mPostCityView.setText("City: "+postCity);
        return view;
    }
}
