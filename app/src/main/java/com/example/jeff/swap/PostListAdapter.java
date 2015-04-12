package com.example.jeff.swap;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jeff.swap.models.Post;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeff on 15-03-24.
 */
public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostViewHolder>{

    private String FLICKR_REGEX = ".*\\b(staticflickr)\\b.*";

    private List<Post> mPosts;
    private PostViewListener mPostViewListener;

    public PostListAdapter(Context context, List<Post> posts) {
        mPosts = posts;
    }

    public interface PostViewListener{
        public void addImageToQueue(ImageView postImageView, String imageUrl);
        public void onPostClick(View view, int position);
    }

    public void setListener(PostViewListener listener){
        mPostViewListener = listener;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_fragment_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder viewHolder, int position) {
        Log.i("POSITION", "$$$ onBindViewHolder POSITION $$$: " + position);
        Post post = mPosts.get(position);
        String pattern = BuildConfig.SERVER_URL+"/image/uploads/(?!undefined)";
        Pattern patternToMatch = Pattern.compile(pattern);
        String imageUrl = post.getImageUrl();
        Matcher regexMatch = patternToMatch.matcher(imageUrl);
        Log.i("onBindViewHolder","$$$$$ post.getImageUrl() $$$$$: "+(post.getImageUrl()));
        Log.i("onBindViewHolder","$$$$$ regexMatch.lookingAt() $$$$$: "+(regexMatch.lookingAt()));
        Log.i("onBindViewHolder","$$$$$ Pattern.matches(FLICKR_REGEX,imageUrl $$$$$: "+(Pattern.matches(FLICKR_REGEX,imageUrl)));
        Log.i("onBindViewHolder","$$$$$ $$$$$ $$$$$");
        if (regexMatch.lookingAt() || (Pattern.matches(FLICKR_REGEX,imageUrl))){
            mPostViewListener.addImageToQueue(viewHolder.mPostImageView, post.getImageUrl());
//                for (int i = position+1; i < getItemCount(); i++){
//                    Post postsAhead = mPosts.get(i);
//                    String imageUrl = postsAhead.getImageUrl();
//                    if (patternToMatch.matcher(imageUrl).lookingAt()){
        //mPostImageCacheDownloaderThread.queueImageCache(post.getId(), post.getImageUrl());
//                    }
//                }
        }else{
            viewHolder.mPostImageView.setImageDrawable(null);
        }
        Log.i("regexMatcher","$$$ regexMatch.lookingAt() $$$: "+(regexMatch.lookingAt()));
        Log.i("onBindViewHolder","$$$ post.getImageUrl() $$$: "+post.getImageUrl());
        viewHolder.setTitle("Title: "+post.getTitle());
        viewHolder.setDescription("Description: "+post.getDescription());
        viewHolder.setCity("City: "+post.getCity());
        viewHolder.setUsername("User: "+post.getUsername());
        viewHolder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mPostImageView;
        private TextView mTitleView;
        private TextView mDescriptionView;
        private TextView mCityView;
        private TextView mUsernameView;

        public PostViewHolder(View itemView) {
            super(itemView);
            mPostImageView = (ImageView) itemView.findViewById(R.id.post_imageView);
            mTitleView = (TextView) itemView.findViewById(R.id.post_title);
            mDescriptionView = (TextView) itemView.findViewById(R.id.post_description);
            mCityView = (TextView) itemView.findViewById(R.id.post_city);
            mUsernameView = (TextView) itemView.findViewById(R.id.post_username);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.i("BLINGKINGS","^^^ FLOSSY SON!&&&&");
            if (mPostViewListener != null){
                mPostViewListener.onPostClick(view,getPosition());
            }
        }
        public void setTitle(String title) {
            if (null == mTitleView) return;
            mTitleView.setText(title);
        }

        public void setDescription(String description) {
            if (null == mDescriptionView) return;
            mDescriptionView.setText(description);
        }

        public void setCity(String city) {
            if (null == mCityView) return;
            mCityView.setText(city);
        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
        }
    }
}
