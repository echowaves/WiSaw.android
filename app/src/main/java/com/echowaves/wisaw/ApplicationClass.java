package com.echowaves.wisaw;

/**
 * Created by dmitry on 11/26/17.
 */

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.branch.referral.Branch;
import me.leolin.shortcutbadger.ShortcutBadger;

import static com.echowaves.wisaw.ApplicationClass.updateAppBadge;

public class ApplicationClass  extends Application  {

    public static String HOST = "https://api.wisaw.com";


    @Override
    public void onCreate() {
        super.onCreate();

        // Branch logging for debugging
        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(this);
    }


    public static void photoLiked(Integer photoId, Context context) {
        try {
                File file = new File(context.getFilesDir(), "wisaw-liked-" + photoId.toString());
                file.createNewFile();
            } catch (FileNotFoundException e) {
                Log.d("++++++++++++++", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("++++++++++++++", "Error accessing file: " + e.getMessage());
            }
    }

    public static void photoViewed(Integer photoId, Context context) {
        try {
            File file = new File(context.getFilesDir(), "wisaw-viewed-" + photoId.toString());
            file.createNewFile();
        } catch (FileNotFoundException e) {
            Log.d("++++++++++++++", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("++++++++++++++", "Error accessing file: " + e.getMessage());
        }
    }

    public static boolean isPhotoLiked(Integer photoId, Context context) {
        File file = new File(context.getFilesDir(), "wisaw-liked-" + photoId.toString());
        return file.exists();
    }

    public static boolean isPhotoViewed(Integer photoId, Context context) {
        File file = new File(context.getFilesDir(), "wisaw-viewed-" + photoId.toString());
        return file.exists();
    }


    public static void updateNewPhotosStatus(JSONArray photosJSON, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("wisaw-preferences", Context.MODE_PRIVATE);
        boolean firstRun = sharedPref.getBoolean("firstRun", true);

        if (firstRun) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("firstRun", false);
            editor.commit();
            // mark all photos as read
            for (int i=0; i < photosJSON.length(); i++) {
                try {
                    JSONObject photoJSON = photosJSON.getJSONObject(i);
                    Integer photoId = photoJSON.getInt("id");
                    photoViewed(photoId, context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        updateAppBadge(photosJSON, context);
    }

    public static void updateAppBadge(JSONArray photosJSON, Context context) {
        int updates = 0;
        for (int i=0; i < photosJSON.length(); i++) {
            try {
                JSONObject photoJSON = photosJSON.getJSONObject(i);
                Integer photoId = photoJSON.getInt("id");
                if(!isPhotoViewed(photoId, context)) {
                    updates++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        does not work on all devices
        ShortcutBadger.applyCount(context, updates);

    }

}
