package com.echowaves.wisaw;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import java.util.Calendar;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;


public class DetailedViewFragment extends Fragment {
    private ProgressBar progressBar;

    TextView cancelButton;
    TextView reportAbuseButton;
    TextView deleteButton;
    TextView shareButton;
    TouchImageView imageView;

    Context context;

    public int index = 0;

    private JSONArray photosJSON = null;

    private String uuid;
    private String photoId;


    private FileCache imagesCache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        imagesCache = new FileCache(this.getContext());


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

            Bitmap bitmap = imagesCache.get(photoId);


            if (bitmap == null) {
                bitmap = HomeActivity.fromJsonArray(dataJSON);
//                progressBar.setVisibility(View.INVISIBLE);
            }

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
                                        .setPriority(Priority.HIGH)
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


        shareButton = view.findViewById(R.id.btnShare);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(photoId, getActivity());
            }
        });

        FontAwesome.applyToAllViews(view.getContext(), view.findViewById(R.id.activity_details));

        return view;
    }

    public static void share(String photoId, Activity activity) {
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("photo/" + photoId)
                .setTitle("What I saw today:")
                .setContentDescription("Photo " + photoId + " shared")
                .setContentImageUrl("https://www.wisaw.com/api/photos/" + photoId + "/thumb")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
//                        .setContentMetadata(new ContentMetadata().addCustomMetadata("key1", "value1"));

                ;


        LinkProperties lp = new LinkProperties()
                .setChannel("direct")
                .setFeature("sharing")
                .setCampaign("photo sharing")
//                        .setStage("new user")
//                        .addControlParameter("$desktop_url", "http://example.com/home")
//                        .addControlParameter("custom", "data")
//                        .addControlParameter("custom_random", Long.toString(Calendar.getInstance().getTimeInMillis()));
                .addControlParameter("$photo_id", photoId)

                ;

        ShareSheetStyle ss = new ShareSheetStyle(activity, "Check out", "Check out what I saw today: ")
//                        .setCopyUrlStyle(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
//                        .setMoreOptionStyle(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search), "Show more")
//                        .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
//                        .addPreferredSharingOption(SharingHelper.SHARE_WITH.HANGOUT)
                .setAsFullWidthStyle(true)
//                        .setSharingTitle("Share With")
                ;

        buo.showShareSheet(activity, lp,  ss,  new Branch.BranchLinkShareListener() {
            @Override
            public void onShareLinkDialogLaunched() {
            }
            @Override
            public void onShareLinkDialogDismissed() {
            }
            @Override
            public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
            }
            @Override
            public void onChannelSelected(String channelName) {
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.INVISIBLE);

        if (!getUserVisibleHint()) {
//            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        Bitmap bitmap = imagesCache.get(photoId);


        if (bitmap == null) {
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Bitmap imageData = HomeActivity.fromJsonArray(imageDataArray);


                            imagesCache.put(photoId, imageData);

                            imageView.setImageBitmap(imageData);

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


    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }

    }


    public static class FileCache {
        Context mContext;
        File storageDir;

        FileCache(Context context) {
            mContext = context;
            storageDir = mContext.getFilesDir();
        }

        public void put(String name, Bitmap bitmap) {
            try {
                File pictureFile = new File(storageDir, name);
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                fos.write(bitmap.getRowBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("++++++++++++++", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("++++++++++++++", "Error accessing file: " + e.getMessage());
            }
        }


        public Bitmap get(String name) {
            File pictureFile = new File(storageDir, name);
            if (pictureFile.exists()) {
                return BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
            }
            return null;
        }
    }


}