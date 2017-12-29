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
    @Override
    public void onCreate() {
        super.onCreate();

        // Branch logging for debugging
        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(this);
    }

    public static class FileCache {
        Context mContext;
        File storageDir;

        FileCache(Context context) {
            mContext = context;
            storageDir = mContext.getFilesDir();
        }

        public void put(Integer id, Bitmap bitmap) {
            try {
                File pictureFile = new File(storageDir, id.toString());
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//                fos.write(bitmap.getRowBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("++++++++++++++", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("++++++++++++++", "Error accessing file: " + e.getMessage());
            }
        }


        public Bitmap get(Integer id) {
            File pictureFile = new File(storageDir, id.toString());
            if (pictureFile.exists()) {
                return BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
            }
            return null;
        }
    }

}
