package com.echowaves.wisaw;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.widget.ANImageView;
import com.eqot.fontawesome.FontAwesome;

import org.json.JSONException;
import org.json.JSONObject;

public class SharingActivity extends AppCompatActivity {


    private ProgressBar progressBar;

    TextView cancelButton;
    TextView reportAbuseButton;
    TextView deleteButton;
    TextView shareButton;
    ANImageView imageView;

    Activity context;


    private JSONObject photoJson;
    private String uuid;
    private Integer photoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);


        imageView = findViewById(R.id.imageView);

        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.bringToFront();
        progressBar.setVisibility(View.INVISIBLE);

        photoId = new Integer(getIntent().getStringExtra("photoId"));


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
                                    parametersJSON.put("photoId", photoId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ;
                                progressBar.setVisibility(View.VISIBLE);
                                AndroidNetworking.post(ApplicationClass.HOST + "/abusereport")
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
                                                AndroidNetworking.delete(ApplicationClass.HOST + "/photos/" + photoId)
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
                                AndroidNetworking.delete(ApplicationClass.HOST + "/photos/" + photoId)
                                        .setContentType("application/json")
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


        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.cancel("download");
        AndroidNetworking.get(ApplicationClass.HOST + "/photos/" + photoId)
                .setTag("download")
//                    .setExecutor(Executors.newSingleThreadExecutor())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.INVISIBLE);

                        // do anything with response

                        try {
                            photoJson = response.getJSONObject("photo");
                            uuid = photoJson.getString("uuid");


                            String thumbUrl = photoJson.getString("getThumbUrl");
                            String imgUrl = photoJson.getString("getImgUrl");


//            imageView.setDefaultImageResId(R.drawable.default);
//            imageView.setErrorImageResId(R.drawable.error);
                            imageView.setImageUrl(thumbUrl);
                            imageView.setImageUrl(imgUrl);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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


        shareButton = findViewById(R.id.btnShare);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DetailedViewFragment.share(photoJson, context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        FontAwesome.applyToAllViews(this, findViewById(R.id.activity_sharing));

        progressBar.setVisibility(View.INVISIBLE);


        reportAbuseButton.setEnabled(false);
        deleteButton.setEnabled(false);
        shareButton.setEnabled(false);

    }


}
