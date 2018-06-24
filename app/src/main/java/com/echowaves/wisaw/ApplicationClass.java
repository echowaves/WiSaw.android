package com.echowaves.wisaw;

/**
 * Created by dmitry on 11/26/17.
 */

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
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

        // schedule job

        ComponentName componentName = new ComponentName(this, BackgroundFetch.class);
        JobInfo jobInfo = new JobInfo.Builder(12, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPeriodic(1000 * 60 * 60 * 12) // 12 hours
                .build();

        JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(HOST, "Job scheduled!");
        } else {
            Log.d(HOST, "Job not scheduled");
        }

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
        return true;
//        File file = new File(context.getFilesDir(), "wisaw-viewed-" + photoId.toString());
//        return file.exists();
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
        return;
//        int updates = 0;
//        for (int i=0; i < photosJSON.length(); i++) {
//            try {
//                JSONObject photoJSON = photosJSON.getJSONObject(i);
//                Integer photoId = photoJSON.getInt("id");
//                if(!isPhotoViewed(photoId, context)) {
//                    updates++;
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

        // set badge notification

//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "wisaw notifications", NotificationManager.IMPORTANCE_HIGH);
//
//            // Configure the notification channel.
//            notificationChannel.setDescription("default wisaw notification channel");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//            notificationChannel.enableVibration(true);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//

//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
//
//        notificationBuilder
//                .setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.mipmap.ic_launcher_round)
////                .setTicker("Hearty365")
////                .setPriority(Notification.PRIORITY_MAX)
////                .setContentTitle("Default notification")
//                .setContentText("unseen photos: " + updates)
////                .setContentInfo("Info")
//                ;
//
//        if(updates > 0) {
//            notificationManager.notify(/*notification id*/1, notificationBuilder.build());
//        } else {
//            notificationManager.cancelAll();
//        }
    }



}
