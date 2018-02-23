package com.echowaves.wisaw;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dmitry on 10/27/17.
 */

public class ImageItem {
    private Context context;
    private String imageUrl;
    private Integer likes;
    private Integer photoId;
    private boolean isLiked = false;
    private boolean isViewed = false;


    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getLikes() {
        return likes;
    }

    public Integer getPhotoId() {
        return photoId;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public ImageItem(JSONObject photoJSON, Context context) {
        this.context = context;
        try {
            this.imageUrl = photoJSON.getString("getThumbUrl");
            this.likes = photoJSON.getInt("likes");
            this.photoId = photoJSON.getInt("id");
            this.isLiked = ApplicationClass.isPhotoLiked(photoId, context);
            this.isViewed = ApplicationClass.isPhotoViewed(photoId, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}