package com.echowaves.wisaw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailedViewActivity extends AppCompatActivity {

    Button cancelButton;
    Button reportAbuseButton;
    ImageButton deleteButton;
    ImageView imageView;

    Context context;


    String UUID;
    String photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        Intent myIntent = getIntent(); // gets the previously created intent
        UUID = myIntent.getStringExtra("uuid"); // will return "FirstKeyValue"
        photoId= myIntent.getStringExtra("photoId"); // will return "SecondKeyValue"

        context = this;
        cancelButton = (Button) findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        reportAbuseButton = (Button) findViewById(R.id.btnReportAbuse);
        deleteButton = (ImageButton)findViewById(R.id.btnDelete);
        imageView = (ImageView)findViewById(R.id.imageView);



        AndroidNetworking.get("https://www.wisaw.com/api/photos/" + photoId)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                        // handle error
                        Log.e("++++++++++++++++++++++ ", error.getErrorBody());

                    }
                });





    }
}
