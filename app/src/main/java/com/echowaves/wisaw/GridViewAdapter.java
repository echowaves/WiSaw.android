package com.echowaves.wisaw;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.androidnetworking.widget.ANImageView;

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
        ANImageView imageView = (ANImageView) row.findViewById(R.id.image);


        ImageItem item = imageItems.get(position);

        imageView.setImageUrl(item.getImageUrl());

        return row;
    }

}