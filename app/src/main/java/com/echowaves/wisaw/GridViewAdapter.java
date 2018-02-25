package com.echowaves.wisaw;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.androidnetworking.widget.ANImageView;
import com.matrixxun.starry.badgetextview.MaterialBadgeTextView;

import java.util.ArrayList;

/**
 * Created by dmitry on 10/27/17.
 */

public class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<ImageItem> imageItems = new ArrayList<>();

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList imageItems) {
        super(context, layoutResourceId, imageItems);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.imageItems = imageItems;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);
        ANImageView imageView = row.findViewById(R.id.image);
        MaterialBadgeTextView badgeTextView = row.findViewById(R.id.badge);

        ImageItem item = imageItems.get(position);

        imageView.setImageUrl(item.getImageUrl());


        badgeTextView.setTextColor(Color.WHITE);

        if(!ApplicationClass.isPhotoViewed(item.getPhotoId(), context)) {
            badgeTextView.setBackgroundColor(Color.RED);
            if(item.getLikes() == 0) {
                badgeTextView.setHighLightMode();
            } else {
                badgeTextView.setBadgeCount(item.getLikes(), false);
            }
        } else {
            badgeTextView.setBackgroundColor(0xFF249b00);
                badgeTextView.setBadgeCount(item.getLikes(), true);
        }

        return row;
    }

}