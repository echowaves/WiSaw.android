package com.echowaves.wisaw;

import android.graphics.Bitmap;

/**
 * Created by dmitry on 10/27/17.
 */

public class ImageItem {
    private Bitmap image;


    public ImageItem(Bitmap image) {
        super();
        this.image = image;

    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }


}