package com.example.jeff.swap.models;

/**
 * Created by jeff on 15-03-13.
 */
public class Post {
    private String mUsername;
    private String mTitle;
    private String mDescription;
    private String mCity;
    private String mImageUrl;
    private String mId;

    private Post() {
    }

    public String getUsername() {
        return mUsername;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getCity() {
        return mCity;
    }

    public String getImageUrl(){ return mImageUrl; }

    public String getId(){ return mId; }

    public static class Builder {
        private String mUsername;
        private String mTitle;
        private String mDescription;
        private String mCity;
        private String mImageUrl;
        private String mId;

        public Builder id(String id) {
            mId = id;
            return this;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder title(String title) {
            mTitle = title;
            return this;
        }

        public Builder description(String description) {
            mDescription = description;
            return this;
        }

        public Builder city(String city) {
            mCity = city;
            return this;
        }

        public Builder imageUrl(String url){
            mImageUrl = url;
            return this;
        }

        public Post build() {
            Post post = new Post();
            post.mId = mId;
            post.mUsername = mUsername;
            post.mTitle = mTitle;
            post.mDescription = mDescription;
            post.mCity = mCity;
            post.mImageUrl = mImageUrl;
            return post;
        }
    }
}
