package com.echowaves.wisaw;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;


public class HomeActivity extends AppCompatActivity {

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    private Location mLastLocation;

    Context context;
    private static final int LOCATION_PERMISSION_ID = 1001;

    private JSONArray photosJSON;


    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;

        gridView = (GridView) findViewById(R.id.gridView);

        AndroidNetworking.initialize(getApplicationContext());


        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);



        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        /* ... */
                        Log.d("++++++++++++++++++++++", "onPermissionGranted");


//                        long mLocTrackingInterval = 1000 * 100; // 100 sec
//                        float trackingDistance = 5000;
//                        LocationAccuracy trackingAccuracy = LocationAccuracy.LOWEST;
//
//                        LocationParams.Builder builder = new LocationParams.Builder()
//                                .setAccuracy(trackingAccuracy)
//                                .setDistance(trackingDistance)
//                                .setInterval(mLocTrackingInterval);


                        SmartLocation
                                .with(context)
                                .location()
                                .continuous()
//                                .config(builder.build())
                                .start(new OnLocationUpdatedListener() {
                                    @Override
                                    public void onLocationUpdated(Location location) {
                                        Log.d("++++++++++++++++++++++", "obtined new location: " + location.toString());
                                        mLastLocation = location;
                                        loadImages();
                                    }

                                });



                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                        /* ... */
                        Log.d("++++++++++++++++++++++", "onPermissionDenied");

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                        Log.d("++++++++++++++++++++++", "onPermissionRationaleShouldBeShown");

                    }

                }).check();



        ImageButton capture = (ImageButton) findViewById(R.id.btnCapture);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Dexter.withActivity((Activity) context)
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        /* ... */
                                Log.d("++++++++++++++++++++++", "onPermissionGranted");

                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                                startActivityForResult(cameraIntent, CAMERA_REQUEST);



                            }
                            @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                        /* ... */
                                Log.d("++++++++++++++++++++++", "onPermissionDenied");

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                                Log.d("++++++++++++++++++++++", "onPermissionRationaleShouldBeShown");

                            }

                        }).check();






            }
        });


//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//                Bitmap mphoto = (Bitmap) data.getExtras().get("data");
//
//            }
//        }

    }


    // Prepare some data for gridview
    private ArrayList<ImageItem> getData() {
        Log.d("++++++++++++++++++++++", "getData()");

        final ArrayList<ImageItem> imageItems = new ArrayList<>();


        if(photosJSON!=null) {
        for (int i = 0; i < photosJSON.length(); i++) {

            try {
                JSONObject thumbJSON = photosJSON.getJSONObject(i).getJSONObject("thumbNail");
                JSONArray dataJSON = thumbJSON.getJSONArray("data");

                byte[] bArray = new byte[dataJSON.length()];


                for(int j= 0; j<dataJSON.length(); j++) {
                    bArray[j] = (byte)dataJSON.getInt(j);
                }

//                Log.d("++++++++++++++++++++++", "data: " + bArray);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bArray, 0, bArray.length);
                imageItems.add(new ImageItem(bitmap));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        }
        return imageItems;
    }



    private void loadImages()  {


        JSONArray coordinatesJSON = new JSONArray();
        JSONObject locationJSON = new JSONObject();
        JSONObject parametersJSON = new JSONObject();
        try {
            coordinatesJSON.put(this.mLastLocation.getLatitude());
            coordinatesJSON.put(this.mLastLocation.getLongitude());


            locationJSON.put("type", "Point");
            locationJSON.put("coordinates", coordinatesJSON);


            parametersJSON.put("location", locationJSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }



        Log.d("++++++++++++++++++++++", "loading images via API");

        AndroidNetworking.post("https://www.wisaw.com/api/photos/feed")
                .addJSONObjectBody(parametersJSON)
                .setContentType("application/json")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response


                        try {
                            photosJSON = response.getJSONArray("photos");

//                            Log.d("++++++++++++++++++++++", photosJSON.toString());

                            gridAdapter = new GridViewAdapter(context, R.layout.grid_item_layout, getData());
                            gridView.setAdapter(gridAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                    }
                });
    }


}
