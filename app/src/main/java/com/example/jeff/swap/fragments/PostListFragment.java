package com.example.jeff.swap.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.JSONParser;
import com.example.jeff.swap.PostImageCacheDownloader;
import com.example.jeff.swap.PostImageDownloader;
import com.example.jeff.swap.PostListAdapter;
import com.example.jeff.swap.R;
import com.example.jeff.swap.activities.PostShowActivity;
import com.example.jeff.swap.activities.SearchActivity;
import com.example.jeff.swap.models.Post;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jeff on 15-03-14.
 */
public class PostListFragment extends Fragment implements PostListAdapter.PostViewListener{
    private RecyclerView mPostsView;
    private List<Post> mPosts = new ArrayList<Post>();
    private PostListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private PostImageDownloader<ImageView> mPostImageDownloaderThread;
    private PostImageCacheDownloader mPostImageCacheDownloaderThread;

    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;

    private static final String CITY_SEARCH = "0";
    private String mSearchTerm;
    private String mNextPageId;

    public static PostListFragment newInstance(String ... params){
        Bundle args = new Bundle();
        for(int i = 0; i < params.length; i++){
            if (params[i] != null){
                args.putString(String.valueOf(i),params[i]);
            }
        }
        PostListFragment postListFragment = new PostListFragment();
        postListFragment.setArguments(args);
        return postListFragment;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mAdapter = new PostListAdapter(activity,mPosts);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null){
            String city = arguments.getString(CITY_SEARCH);
            if(city != null){
                mSearchTerm = city;
                loadNewPosts();
            }
        }else{
            loadNewPosts();
        }
        setHasOptionsMenu(true);
        mPostImageDownloaderThread = new PostImageDownloader<ImageView>(new Handler());
        mPostImageDownloaderThread.setListener(new PostImageDownloader.Listener<ImageView>() {
            public void onPostImageDownloaded(ImageView imageView, Bitmap postImage) {
                if (isVisible()) {
                    imageView.setImageBitmap(postImage);
                }
            }
        });
        mAdapter.setListener(this);
        mPostImageDownloaderThread.start();
        mPostImageDownloaderThread.setPriority(5);
        mPostImageDownloaderThread.getLooper();

        mPostImageCacheDownloaderThread = new PostImageCacheDownloader();
        mPostImageCacheDownloaderThread.start();
        mPostImageCacheDownloaderThread.setPriority(1);
        mPostImageDownloaderThread.getLooper();
    }

    @Override
    public void addImageToQueue(ImageView postImageView, String imageUrl){
        mPostImageDownloaderThread.queueImage(postImageView, imageUrl);
    }

    @Override
    public void onPostClick(View view, int position) {
        Log.i("ITEM-CLICKED","$$$ onPostClick POSITION YO: $$$ --> "+position);
        Post post = mPosts.get(position);
        Intent intent = new Intent(getActivity(),PostShowActivity.class);
        intent.putExtra("id",post.getId());
        intent.putExtra("username",post.getUsername());
        intent.putExtra("title",post.getTitle());
        intent.putExtra("description",post.getDescription());
        intent.putExtra("city",post.getCity());
        intent.putExtra("imageUrl",post.getImageUrl());
        startActivity(intent);
        Log.i("ITEM-CLICKED","$$$ onPostClick post.getImageUrl() $$$ --> "+post.getImageUrl());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_list_fragment,container,false);
        mPostsView = (RecyclerView) view.findViewById(R.id.post_list_recycler_view);
        mPostsView.setHasFixedSize(true);
        mPostsView.setItemViewCacheSize(5);
        mPostsView.setViewCacheExtension(new RecyclerView.ViewCacheExtension() {
            @Override
            public View getViewForPositionAndType(RecyclerView.Recycler recycler, int position, int type) {
                String url;
                Post post = mPosts.get(position);
                if (mPosts.size() > 1) {
                    for (int i = position + 1; i <= position + 10; i++) {
                        Log.i("REG POSITION", "$$$ REG POSITION $$$: " + position);
                        if (i < mPosts.size()) {
                            url = post.getImageUrl();
                            String id = post.getId();
                            if (url != null) {
                                mPostImageCacheDownloaderThread.queueImageCache(id, url);
                            }
                        }
                    }
                }
                return null;
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());
        mPostsView.setLayoutManager(mLayoutManager);
        mPostsView.setAdapter(mAdapter);

        mPostsView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading){
                    if (totalItemCount > previousTotal){
//                        Log.i("SCROLL","$$$ totalItemCount > previousTotal --> totalItemCount $$$: "+totalItemCount);
//                        Log.i("SCROLL","$$$ totalItemCount > previousTotal --> previousTotal $$$: "+previousTotal);
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)){
//                    Log.i("SCROLL","$$$ NOT LOADING visibleItemCount $$$: "+visibleItemCount);
//                    Log.i("SCROLL","$$$ NOT LOADING totalItemCount $$$: "+totalItemCount);
//                    Log.i("SCROLL","$$$ NOT LOADING firstVisibleItem $$$: "+firstVisibleItem);
                    loading = true;
//                    Log.i("SCROLL","$$$ THE END HAS BEEN REACHED! $$$");
//                    Log.i("SCROLL","$$$ mPosts.size() $$$: "+(mPosts.size()));
                    mNextPageId = mPosts.get(mPosts.size()-1).getId();
                    loadNewPosts();
                }
            }
        });

        return view;
    }

    private void loadNewPosts(){
        new LoadPosts().execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                Intent intent = new Intent(getActivity(),SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mPostImageDownloaderThread.quit();
        mPostImageCacheDownloaderThread.quit();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mPostImageDownloaderThread.clearQueue();
        mPostImageCacheDownloaderThread.clearQueue();
    }

//    public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
//
//        private List<Post> mPosts;
//        //private Context mPostAdapterContext;
//
//        public PostAdapter(Context context, List<Post> posts) {
//            //mPostAdapterContext = context;
//            mPosts = posts;
//        }
//
//        @Override
//        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_fragment_item, parent, false);
//            return new PostViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(PostViewHolder viewHolder, int position) {
//            Log.i("POSITION","$$$ onBindViewHolder POSITION $$$: "+position);
//            Post post = mPosts.get(position);
//            String pattern = BuildConfig.SERVER_URL+"/image/uploads/(?!undefined)";
//            Pattern patternToMatch = Pattern.compile(pattern);
//            Matcher regexMatch = patternToMatch.matcher(post.getImageUrl());
//            //if (regexMatch.lookingAt()){
//                mPostImageDownloaderThread.queueImage(viewHolder.mPostImageView, post.getImageUrl());
////                for (int i = position+1; i < getItemCount(); i++){
////                    Post postsAhead = mPosts.get(i);
////                    String imageUrl = postsAhead.getImageUrl();
////                    if (patternToMatch.matcher(imageUrl).lookingAt()){
//                        //mPostImageCacheDownloaderThread.queueImageCache(post.getId(), post.getImageUrl());
////                    }
////                }
////            }else{
////                viewHolder.mPostImageView.setImageDrawable(null);
////            }
//            Log.i("regexMatcher","$$$ regexMatch.lookingAt() $$$: "+(regexMatch.lookingAt()));
//            Log.i("onBindViewHolder","$$$ post.getImageUrl() $$$: "+post.getImageUrl());
//            viewHolder.setTitle("Title: "+post.getTitle());
//            viewHolder.setDescription("Description: "+post.getDescription());
//            viewHolder.setCity("City: "+post.getCity());
//            viewHolder.setUsername("User: "+post.getUsername());
//            viewHolder.setIsRecyclable(false);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mPosts.size();
//        }
//
//        //    @Override
////    public int getItemViewType(int position) {
////        return mPosts.get(position);
////    }
//    }

//    private class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//        private ImageView mPostImageView;
//        private TextView mTitleView;
//        private TextView mDescriptionView;
//        private TextView mCityView;
//        private TextView mUsernameView;
//        //private PostViewHolderClicks mListener;
//
//        public PostViewHolder(View itemView) {
//            super(itemView);
//            mPostImageView = (ImageView) itemView.findViewById(R.id.post_imageView);
//            mTitleView = (TextView) itemView.findViewById(R.id.post_title);
//            mDescriptionView = (TextView) itemView.findViewById(R.id.post_description);
//            mCityView = (TextView) itemView.findViewById(R.id.post_city);
//            mUsernameView = (TextView) itemView.findViewById(R.id.post_username);
//            itemView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View view) {
//            Post post = mPosts.get(getPosition());
//
//            Log.i("ONCLICK","$$$ ONCLICK --> getPosition() $$$: "+(getPosition()));
//            Log.i("ONCLICK","$$$ ONCLICK --> post.getTitle() $$$: "+(post.getTitle()));
//            Log.i("ONCLICK","$$$ ONCLICK --> post.getDescription() $$$: "+(post.getDescription()));
//            Log.i("BLINGKINGS","^^^ FLOSSY SON!&&&&");
//        }
//        public void setTitle(String title) {
//            if (null == mTitleView) return;
//            mTitleView.setText(title);
//        }
//
//        public void setDescription(String description) {
//            if (null == mDescriptionView) return;
//            mDescriptionView.setText(description);
//        }
//
//        public void setCity(String city) {
//            if (null == mCityView) return;
//            mCityView.setText(city);
//        }
//
//        public void setUsername(String username) {
//            if (null == mUsernameView) return;
//            mUsernameView.setText(username);
//        }
//    }

    private String createUrl(){
        String url = BuildConfig.SERVER_URL + "/post/index/";
        if (mSearchTerm != null){
            url += mSearchTerm;
        }else{
            url += "turban";
        }
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        if(mNextPageId != null){
            url += "?";
            params.add(new BasicNameValuePair("object_id",mNextPageId));
        }
        if (!params.isEmpty()){
            String paramString = URLEncodedUtils.format(params,"utf-8");
            url += paramString;
            return url;
        }else{
            return url;
        }
    }

    private class LoadPosts extends AsyncTask<Void,Void,JSONArray>{

        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray;
            jsonArray = jsonParser.getJSONArray(createUrl());
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray){
            if (jsonArray != null){
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("_id");
                        String title = jsonObject.getString("title");
                        String description = jsonObject.getString("description");
                        String city = jsonObject.getString("city");
                        String username = jsonObject.getString("username");
                        String imageUrl = jsonObject.getString("imageUrl");
                        mPosts.add(new Post.Builder().id(id).title(title).description(description).city(city).username(username).imageUrl(imageUrl).build());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mAdapter.notifyDataSetChanged();
                mPostsView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),"YO HATER!", Toast.LENGTH_LONG);
                    }
                });
            }else{
                Toast.makeText(getActivity(),"Nothing found haters, try another search!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
