package com.echowaves.wisaw;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.eqot.fontawesome.FontAwesome;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    TextView likeButton;
    TextView badgeCounter;

    TouchImageView imageView;

    Context context;

    public int index = 0;

    private JSONArray photosJSON = null;
    private JSONObject photoJSON = null;
    private String uuid;
    private Integer photoId;
    private Integer likes;
    private String thumbUrl;
    private String imgUrl;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_detailed_view, container, false);

        context = getActivity();

        imageView = view.findViewById(R.id.imageView);

        progressBar = view.findViewById(R.id.progressBar_cyclic);
        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);


        try {
            photosJSON = HomeActivity.photosJSON;

            photoJSON = photosJSON.getJSONObject(index);
            photoId = photoJSON.getInt("id");
            uuid = photoJSON.getString("uuid");
            likes = photoJSON.getInt("likes");
            thumbUrl = photoJSON.getString("getThumbUrl");
            imgUrl = photoJSON.getString("getImgUrl");


        } catch (JSONException e) {
            e.printStackTrace();
        }



        progressBar.setVisibility(View.VISIBLE);

//            System.out.println("Downloadubg thumb: " + thumbUrl);
        AndroidNetworking.get(thumbUrl)
                .build()
                .getAsBitmap(new BitmapRequestListener() {
                    @Override
                    public void onResponse(Bitmap thumbUrlbitmap) {
                        // do anything with bitmap
                        imageView.setImageBitmap(thumbUrlbitmap);
//                            System.out.println("Downloadubg img: " + imgUrl);
                        AndroidNetworking.get(imgUrl)
                                .build()
                                .getAsBitmap(new BitmapRequestListener() {
                                    @Override
                                    public void onResponse(Bitmap imgUrlbitmap) {
                                        // do anything with bitmap
                                        progressBar.setVisibility(View.INVISIBLE);
                                        imageView.setImageBitmap(imgUrlbitmap);
//                                            imageView.setZoom(1f);
                                    }
                                    @Override
                                    public void onError(ANError error) {
                                        // handle error
                                        System.out.println("Download image error: " + imgUrl + error);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });

                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        System.out.println("Download image error: " + thumbUrl + error);


                        AndroidNetworking.get(imgUrl)
                                .build()
                                .getAsBitmap(new BitmapRequestListener() {
                                    @Override
                                    public void onResponse(Bitmap imgUrlbitmap) {
                                        // do anything with bitmap
                                        imageView.setImageBitmap(imgUrlbitmap);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                    @Override
                                    public void onError(ANError error) {
                                        // handle error
                                        System.out.println("Download image error: " + imgUrl + error);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });


                    }
                });




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
                                    parametersJSON.put("photoId", photoId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ;
                                progressBar.setVisibility(View.VISIBLE);
                                AndroidNetworking.post(ApplicationClass.HOST + "/abusereport")
                                        .addJSONObjectBody(parametersJSON)
                                        .setContentType("application/json")
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                progressBar.setVisibility(View.INVISIBLE);

                                                // do anything with response


                                                progressBar.setVisibility(View.VISIBLE);
                                                AndroidNetworking.delete(ApplicationClass.HOST + "/photos/" + photoId)
                                                        .setContentType("application/json")
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
                                AndroidNetworking.delete(ApplicationClass.HOST + "/photos/" + photoId)
                                        .setContentType("application/json")
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
                try {
                    share(photoJSON, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        likeButton = view.findViewById(R.id.btnLike);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    like(photoJSON, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        badgeCounter = view.findViewById(R.id.badgeCounter);

        badgeCounter.setText(likes.toString());

        if(ApplicationClass.isPhotoLiked(photoId, context)) {
            likeButton.setEnabled(false);
        } else {
            likeButton.setEnabled(true);
        }


        FontAwesome.applyToAllViews(view.getContext(), view.findViewById(R.id.activity_details));

        return view;
    }

    public static void like(JSONObject photoJSON, final Activity activity) throws JSONException {
        final TextView badgeCounter = activity.findViewById(R.id.badgeCounter);
        final TextView likeButton = activity.findViewById(R.id.btnLike);

        try {

            final Integer photoId = photoJSON.getInt("id");
            likeButton.setEnabled(false);

            AndroidNetworking.put(ApplicationClass.HOST + "/photos/" + photoId + "/like")
                    .setContentType("application/json")
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                            ApplicationClass.photoLiked(photoId, activity);
                            badgeCounter.setText( String.valueOf(Integer.parseInt(badgeCounter.getText().toString()) +1) );
                        }

                        @Override
                        public void onError(ANError error) {

                            // handle error
                            Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public static void share(JSONObject photoJSON, Activity activity) throws JSONException {
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("photo/" + photoJSON.getInt("id"))
                .setTitle("What I saw today:")
                .setContentDescription("Photo " + photoJSON.getInt("id") + " shared")
                .setContentImageUrl(photoJSON.getString("getThumbUrl"))
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
                .addControlParameter("$photo_id", String.valueOf(photoJSON.getInt("id")))

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
//
        if (!getUserVisibleHint()) {
//            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

//            if(imageView.isInitialized()) {
//                return;
//            }
        ApplicationClass.photoViewed(photoId, context);
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


}