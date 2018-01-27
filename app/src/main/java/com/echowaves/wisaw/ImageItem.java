package com.echowaves.wisaw;

import com.androidnetworking.widget.ANImageView;

/**
 * Created by dmitry on 10/27/17.
 */

public class ImageItem {
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public ImageItem(String thumbUrl) {
        super();
        this.imageUrl = thumbUrl;
    }

}