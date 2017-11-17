package com.echowaves.wisaw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class DetailedViewFragment extends Fragment {
    private ProgressBar progressBar;

    TextView cancelButton;
    TextView reportAbuseButton;
    TextView deleteButton;
    ImageView imageView;

    Context context;

    public int index = 0;

    private JSONArray photosJSON = null;

    private String uuid;
    private String photoId;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_detailed_view, container, false);


        imageView = view.findViewById(R.id.imageView);

        progressBar = view.findViewById(R.id.progressBar_cyclic);
        progressBar.bringToFront();



//        Intent myIntent = getIntent(); // gets the previously created intent
//        index = Integer.valueOf(myIntent.getStringExtra("position")).intValue(); // will return "SecondKeyValue"

        try {

            photosJSON = HomeActivity.photosJSON;


            JSONObject photoJSON = null;
            photoJSON = photosJSON.getJSONObject(index);
            photoId = photoJSON.getString("id");
            uuid = photoJSON.getString("uuid");


            JSONObject thumbJSON = photosJSON.getJSONObject(index).getJSONObject("thumbNail");
            JSONArray dataJSON = thumbJSON.getJSONArray("data");


            Bitmap bitmap = HomeActivity.fromJsonArray(dataJSON);

            imageView.setImageBitmap(bitmap);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        context = getActivity();
        cancelButton = view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });




        reportAbuseButton = view.findViewById(R.id.btnReportAbuse);
        reportAbuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("The user who posted this photo wlll be baned. Are you sure?")
                        .setNegativeButton("Report", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                JSONObject parametersJSON = new JSONObject();
                                try {
                                    parametersJSON.put("uuid", uuid);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                };
                                progressBar.setVisibility(View.VISIBLE);
                                AndroidNetworking.post("https://www.wisaw.com/api/abusereport")
                                        .addJSONObjectBody(parametersJSON)
                                        .setContentType("application/json")
                                        .setPriority(Priority.MEDIUM)
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressBar.setVisibility(View.INVISIBLE);

                                                // do anything with response



                                                progressBar.setVisibility(View.VISIBLE);
                                                AndroidNetworking.delete("https://www.wisaw.com/api/photos/" + photoId)
                                                        .setContentType("application/json")
                                                        .setPriority(Priority.MEDIUM)
                                                        .build()
                                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                                            @Override
                                                            public void onResponse(JSONObject response) {
                                                                progressBar.setVisibility(View.INVISIBLE);

                                                                // do anything with response
                                                                getActivity().finish();
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



        deleteButton = view.findViewById(R.id.btnDelete);
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
                                        .setPriority(Priority.MEDIUM)
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                // do anything with response
                                                getActivity().finish();
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
        AndroidNetworking.get("https://www.wisaw.com/api/photos/" + photoId)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.INVISIBLE);

                        // do anything with response


                        JSONArray imageDataArray=null;
                        try {
                            JSONObject photoJson = response.getJSONObject("photo");
                            JSONObject imageDataJson = photoJson.getJSONObject("imageData");
                            imageDataArray = imageDataJson.getJSONArray("data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Bitmap imageData = HomeActivity.fromJsonArray(imageDataArray);
                        imageView.setImageBitmap(imageData);

                    }
                    @Override
                    public void onError(ANError error) {
                        progressBar.setVisibility(View.INVISIBLE);

                        // handle error
                        Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                    }
                });




        FontAwesome.applyToAllViews(view.getContext(), view.findViewById(R.id.activity_details));

        return view;
    }



}
