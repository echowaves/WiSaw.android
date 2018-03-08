package com.echowaves.wisaw;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.fabric.sdk.android.Fabric;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class HomeActivity extends AppCompatActivity {

    private TextView uploadCounterButton;

    private ProgressBar progressBar;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridView gridView;
    private GridViewAdapter gridAdapter;

    private Location mLastLocation;

    Context context;
    private File cacheDir;

    public static JSONArray photosJSON; //TODO: do not know what to go about public static, will have to figure something out later

    private static final int CAMERA_REQUEST = 1888;
    private static final int PAGER_REQUEST = 1889;


    private String mCurrentPhotoPath;


    String uuid;

    @Override
    public void onStart() {
        super.onStart();

        // Branch init
        // listener (within Main Activity's onStart)
        Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {

                if (error == null) {
                    Log.i("BRANCH SDK", referringParams.toString());
                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }


                if (error == null) {
                    // option 3: navigate to page
                    Intent intent = new Intent(HomeActivity.this, SharingActivity.class);
                    try {
                        intent.putExtra("photoId", referringParams.getString("$photo_id"));
                        startActivity(intent);
                    } catch (JSONException e) {
//                        Toast toast = Toast.makeText(getApplicationContext(), "Sorry, looks like something went wrong.",
//                                Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();

                        e.printStackTrace();
                    }

                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }



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
        cacheDir = context.getFilesDir();

        cleanup();


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
                                .config(LocationParams.BEST_EFFORT)
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



        TextView capture = findViewById(R.id.btnCapture);
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
//                                                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


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
            }
        });


        uploadCounterButton = findViewById(R.id.uploadCounterButton);
        final Animation animation = new AlphaAnimation(1, (float)0.1); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        uploadCounterButton.startAnimation(animation);
    }


    private void cleanup() {
        String[] children = cacheDir.list();
        Date today = new Date();

        for (int i = 0; i < children.length; i++)
        {
            File cachedImage = new File(cacheDir, children[i]);

            int diffInDays = (int)( (today.getTime() - cachedImage.lastModified()) /(1000 * 60 * 60 * 24 * 30 ) ); //30 days
            if(diffInDays>=1) {
                    cachedImage.delete();
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        progressBar.setVisibility(View.INVISIBLE);

        if(this.mLastLocation == null) {
            return;
        }

        if(requestCode == PAGER_REQUEST) {
            loadImages();
        }


        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {


//            Log.d("++++++++++++++++++++++", parametersJSON.toString());

            uploadImage();
        }
    }



    private synchronized void uploadImage() {
        updateCounter();

        List<File> imageFiles = getImagesToUpload();
        if(imageFiles.size()==0) {//no files to upload found
            return;
        }

        final File currentFile = imageFiles.get(0);
        String imageFilePath =  currentFile.getPath();

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

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(ApplicationClass.HOST + "/photos")
//                .setTag("uploading")
                .addJSONObjectBody(parametersJSON)
                .setContentType("application/json")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response

                        progressBar.setVisibility(View.INVISIBLE);

                        JSONObject photo = null;
                        Integer photoId = null;
                        String uploadUrl = null;

                        try {
                            photo = response.getJSONObject("photo");
                            photoId = photo.getInt("id");
                            uploadUrl = response.getString("uploadURL");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("++++++++++++++++++++++", "photo " + photoId + " uploaded");

                        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        File finalFile = new File(storageDir.getPath() + "/wisaw-" + photoId + ".jpg");


                        Log.d("++++++++++++++++++++++", "renaming from: " + currentFile.getPath() + " to: " + finalFile.getPath());
//                        currentFile.renameTo(finalFile);

                        copyFileRotated(currentFile, finalFile);

                        MediaScannerConnection.scanFile(context,
                                new String[] { finalFile.getPath() }, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri) {
                                    }
                                });


                        AndroidNetworking
                                .put(uploadUrl)
                                .addFileBody(finalFile)
                                .setContentType("image/jpeg")
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // do anything with response
                                        currentFile.delete();
                                        updateCounter();
                                        loadImages();

                                    }
                                    @Override
                                    public void onError(ANError error) {
                                        // handle error
                                        currentFile.delete();
                                        updateCounter();
                                        loadImages();

                                    }
                                });




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

                            File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            File finalFile = new File(storageDir.getPath() + "/wisaw-" + timeStamp + ".jpg");

                            Log.d("++++++++++++++++++++++", "renaming from: " + currentFile.getPath() + " to: " + finalFile.getPath());
//                        currentFile.renameTo(finalFile);

                            copyFileRotated(currentFile, finalFile);

                            MediaScannerConnection.scanFile(context,
                                    new String[] { finalFile.getPath() }, null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String path, Uri uri) {
                                        }
                                    });
                            currentFile.delete();
                            updateCounter();
                            loadImages();
                        }

                    }
                });
    }


    private static void copyFileRotated(File src, File dst) {
        try {
            Bitmap bmp = BitmapFactory.decodeFile(src.getPath());
            Bitmap rotatedBitmap = imageOrientation(bmp, src.getPath());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes);

            FileOutputStream fileOutputStream = new FileOutputStream(dst);
            fileOutputStream.write(bytes.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Bitmap imageOrientation(Bitmap originBitmap, String imageFilePath) {
        try {
            ExifInterface ei = new ExifInterface(imageFilePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(originBitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(originBitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(originBitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = originBitmap;
            }

            return rotatedBitmap;

        } catch(Exception e) {
            e.printStackTrace();
        }
        return originBitmap;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "wisaw-new-" + timeStamp;
//        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = cacheDir;
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.d("++++++++++++++++++++++", "Path: " + mCurrentPhotoPath);
        return imageFile;
    }

    // Prepare some data for gridview
    private ArrayList<ImageItem> getData() {
        Log.d("++++++++++++++++++++++", "getData()");

        final ArrayList<ImageItem> imageItems = new ArrayList<>();


        if(photosJSON!=null) {
        for (int i = 0; i < photosJSON.length(); i++) {
            try {
                imageItems.add(new ImageItem(photosJSON.getJSONObject(i), context));
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
        AndroidNetworking.post(ApplicationClass.HOST + "/photos/feed")
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

//                          gridAdapter  = new GridViewAdapter(context, R.layout.grid_item_layout, getData());
//                            gridView.setAdapter(gridAdapter);
                            ApplicationClass.updateNewPhotosStatus(photosJSON, context);

                            gridAdapter.clear();
                            gridAdapter.addAll(getData());
                            gridAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        uploadImage();
                    }
                    @Override
                    public void onError(ANError error) {
                        progressBar.setVisibility(View.INVISIBLE);

                        // handle error
                        Log.e("++++++++++++++++++++++ ", error.getErrorBody());
                        uploadImage();
                    }
                });
    }


    private void updateCounter() {
        final int tasksCount = getImagesToUpload().size();
        Log.d("!!!!!!!!!!!!!!!!!!!!!!!", "tasksCount - " + tasksCount);
        // Update the UI to indicate the work has been completed
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (tasksCount == 0) {
//                    uploadCounterButton.setVisibility(View.INVISIBLE);
                    uploadCounterButton.setText(String.valueOf(""));
                } else {
//                    uploadCounterButton.setVisibility(View.VISIBLE);
                    uploadCounterButton.setText(String.valueOf(tasksCount));
                }
            }
        });
    }


    private  List<File> getImagesToUpload() {
//        File directory = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File directory = cacheDir;
        File[] files = directory.listFiles();
        List<File> resultList = new ArrayList<File>();
        for (File inFile : files) {
            if(inFile.getName().contains("wisaw-new-")) {
                resultList.add(inFile);
            }
        }
        return resultList;
    }


}
