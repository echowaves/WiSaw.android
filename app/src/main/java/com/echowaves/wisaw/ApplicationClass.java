package com.echowaves.wisaw;

/**
 * Created by dmitry on 11/26/17.
 */

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.branch.referral.Branch;

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


}
