package com.echowaves.wisaw;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.crashlytics.android.Crashlytics;
import com.eqot.fontawesome.FontAwesome;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class HomeActivity extends AppCompatActivity {
    private ProgressBar progressBar;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridView gridView;
    private GridViewAdapter gridAdapter;

    private Location mLastLocation;

    Context context;

    public static JSONArray photosJSON; //TODO: do not know what to go about public static, will have to figure something out later

    private static final int CAMERA_REQUEST = 1888;
    private static final int PAGER_REQUEST = 1889;


    private String mCurrentPhotoPath;


    String uuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        //Allowing Strict mode policy for Nougat support
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        setContentView(R.layout.activity_home);

        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.bringToFront();

        FontAwesome.applyToAllViews(this, findViewById(R.id.activity_home));


        context = this;

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());

        gridView = (GridView) findViewById(R.id.gridView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                Intent pagerIntent = new Intent(context, PagerActivity.class);
//                pagerIntent.putExtra("photos",photosJSON.toString());
                pagerIntent.putExtra("position", String.valueOf(position));

                startActivityForResult(pagerIntent, PAGER_REQUEST);

            }
        });

        AndroidNetworking.initialize(getApplicationContext());


        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);


        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        uuid = UUID.nameUUIDFromBytes(androidId.getBytes()).toString();
        Log.d("++++++++++++++++++++++", "uuid: " + uuid);

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
                                        Log.d("++++++++++++++++++++++", "obtained new location: " + location.toString());
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



        TextView capture = (TextView) findViewById(R.id.btnCapture);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Dexter.withActivity((Activity) context)
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        /* ... */
                                Log.d("++++++++++++++++++++++", "onPermissionGranted");





                                Dexter.withActivity((Activity) context)
                                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        .withListener(new PermissionListener() {
                                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        /* ... */
                                                Log.d("++++++++++++++++++++++", "onPermissionGranted");



                                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


                                                // Continue only if the File was successfully created

                                                    Log.d("mylog", "Photofile not null");
                                                Uri photoURI = null;
                                                try {
                                                    photoURI = FileProvider.getUriForFile(HomeActivity.this,
                                                            BuildConfig.APPLICATION_ID + ".provider",
                                                                createImageFile());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                                                startActivityForResult(takePictureIntent, CAMERA_REQUEST);



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



        mSwipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                loadImages();
            };
        });
    }

//    private byte[] getByte(String path) {
//        byte[] getBytes = {};
//        try {
//            File file = new File(path);
//            getBytes = new byte[(int) file.length()];
//            InputStream is = new FileInputStream(file);
//            is.read(getBytes);
//            is.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return getBytes;
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(this.mLastLocation == null) {
            return;
        }
        if(requestCode == PAGER_REQUEST) {
            loadImages();
        }

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            MediaScannerConnection.scanFile(context,
                    new String[] { mCurrentPhotoPath }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });


            JSONArray imageJSON = new JSONArray();
            JSONArray coordinatesJSON = new JSONArray();
            JSONObject locationJSON = new JSONObject();
            JSONObject parametersJSON = new JSONObject();
            try {
                parametersJSON.put("uuid", uuid);
                coordinatesJSON.put(this.mLastLocation.getLatitude());
                coordinatesJSON.put(this.mLastLocation.getLongitude());
                locationJSON.put("type", "Point");
                locationJSON.put("coordinates", coordinatesJSON);
                parametersJSON.put("location", locationJSON);




                Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                byte[] bytes = bos.toByteArray();


                for(int ii=0; ii< bytes.length; ii++) {
                    imageJSON.put(ii, bytes[ii]);
                }


                parametersJSON.put("imageData", imageJSON);

            } catch (JSONException e) {
                e.printStackTrace();
            }

//            Log.d("++++++++++++++++++++++", parametersJSON.toString());

            progressBar.setVisibility(View.VISIBLE);
            AndroidNetworking.post("https://www.wisaw.com/api/photos")
                    .addJSONObjectBody(parametersJSON)
                    .setContentType("application/json")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response

                            progressBar.setVisibility(View.INVISIBLE);

                            Log.d("++++++++++++++++++++++", response.toString());

                            loadImages();
                        }
                        @Override
                        public void onError(ANError error) {
                            progressBar.setVisibility(View.INVISIBLE);

                            // handle error
                            Log.e("++++++++++++++++++++++ ", "error: " + error.getErrorCode());
                            if(error.getErrorCode()==401) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Sorry, looks like you are banned from WiSaw.",
                                        Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }

                        }
                    });

        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("++++++++++++++++++++++", "Path: " + mCurrentPhotoPath);
        return image;
    }


    public static Bitmap fromJsonArray(JSONArray array) {
        byte[] bArray = new byte[array.length()];

        for(int j= 0; j<array.length(); j++) {
            try {
                bArray[j] = (byte)array.getInt(j);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//                Log.d("++++++++++++++++++++++", "data: " + bArray);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bArray, 0, bArray.length);
        return bitmap;
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

                Bitmap bitmap = fromJsonArray(dataJSON);

                imageItems.add(new ImageItem(bitmap));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        }
        return imageItems;
    }



    private void loadImages()  {
        if(this.mLastLocation == null) {
            return;
        }

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
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post("https://www.wisaw.com/api/photos/feed")
                .addJSONObjectBody(parametersJSON)
                .setContentType("application/json")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.INVISIBLE);

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
                        progressBar.setVisibility(View.INVISIBLE);

                        // handle error
                        Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                    }
                });
    }


}
