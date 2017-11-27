package com.echowaves.wisaw;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.eqot.fontawesome.FontAwesome;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

public class SharingActivity extends AppCompatActivity {


    private ProgressBar progressBar;

    TextView cancelButton;
    TextView reportAbuseButton;
    TextView deleteButton;
    TextView shareButton;
    TouchImageView imageView;

    Activity context;


//    private JSONArray photosJSON = null;

    private String uuid;
    private String photoId;


    private DetailedViewFragment.FileCache imagesCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);

        imagesCache = new DetailedViewFragment.FileCache(this);


        imageView = findViewById(R.id.imageView);

        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.bringToFront();


        photoId = getIntent().getStringExtra("photoId");


        context = this;

        cancelButton = findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        reportAbuseButton = findViewById(R.id.btnReportAbuse);
        reportAbuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("The user who posted this photo will be banned. Are you sure?")
                        .setNegativeButton("Report", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                JSONObject parametersJSON = new JSONObject();
                                try {
                                    parametersJSON.put("uuid", uuid);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ;
                                progressBar.setVisibility(View.VISIBLE);
                                AndroidNetworking.post("https://www.wisaw.com/api/abusereport")
                                        .addJSONObjectBody(parametersJSON)
                                        .setContentType("application/json")
                                        .setPriority(Priority.HIGH)
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressBar.setVisibility(View.INVISIBLE);

                                                // do anything with response


                                                progressBar.setVisibility(View.VISIBLE);
                                                AndroidNetworking.delete("https://www.wisaw.com/api/photos/" + photoId)
                                                        .setContentType("application/json")
                                                        .setPriority(Priority.HIGH)
                                                        .build()
                                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                                            @Override
                                                            public void onResponse(JSONObject response) {
                                                                progressBar.setVisibility(View.INVISIBLE);

                                                                // do anything with response
                                                                finish();
                                                            }

                                                            @Override
                                                            public void onError(ANError error) {
                                                                progressBar.setVisibility(View.INVISIBLE);

                                                                // handle error
                                                                Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                                                            }
                                                        });


                                            }

                                            @Override
                                            public void onError(ANError error) {
                                                progressBar.setVisibility(View.INVISIBLE);

                                                // handle error
                                                Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                                            }
                                        });


                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();


            }
        });


        deleteButton = findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("This photo will be obliterated from the cloud. Are you sure?")
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressBar.setVisibility(View.VISIBLE);
                                AndroidNetworking.delete("https://www.wisaw.com/api/photos/" + photoId)
                                        .setContentType("application/json")
                                        .setPriority(Priority.HIGH)
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                // do anything with response
                                                finish();
                                            }

                                            @Override
                                            public void onError(ANError error) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                // handle error
                                                Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                                            }
                                        });

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();


            }
        });


        shareButton = findViewById(R.id.btnShare);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailedViewFragment.share(photoId, context);
            }
        });

        FontAwesome.applyToAllViews(this, findViewById(R.id.activity_sharing));

        progressBar.setVisibility(View.INVISIBLE);



//        Bitmap bitmap = imagesCache.get(photoId);


//        if (bitmap == null) {
            progressBar.setVisibility(View.VISIBLE);
            AndroidNetworking.cancel("download");
            AndroidNetworking.get("https://www.wisaw.com/api/photos/" + photoId)
                    .setPriority(Priority.HIGH)
                    .setTag("download")
//                    .setExecutor(Executors.newSingleThreadExecutor())
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressBar.setVisibility(View.INVISIBLE);

                            // do anything with response


                            JSONArray imageDataArray = null;
                            try {
                                JSONObject photoJson = response.getJSONObject("photo");
                                JSONObject imageDataJson = photoJson.getJSONObject("imageData");
                                imageDataArray = imageDataJson.getJSONArray("data");
                                uuid = photoJson.getString("uuid");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Bitmap imageData = HomeActivity.fromJsonArray(imageDataArray);


                            imagesCache.put(photoId, imageData);


                            imageView.setImageBitmap(imageData);
                            imageView.setZoom(1);

                            reportAbuseButton.setEnabled(true);
                            deleteButton.setEnabled(true);
                            shareButton.setEnabled(true);


                        }

                        @Override
                        public void onError(ANError error) {
                            progressBar.setVisibility(View.INVISIBLE);


                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("Looks like this short lived photo has expired.")
                                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                }
                                            }).show();

                                            // handle error
                            Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                        }
                    });
//        }


        reportAbuseButton.setEnabled(false);
        deleteButton.setEnabled(false);
        shareButton.setEnabled(false);

    }



}
