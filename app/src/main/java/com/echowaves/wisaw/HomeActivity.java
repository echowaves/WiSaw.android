package com.echowaves.wisaw;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.echowaves.wisaw.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActivityHomeBinding bindings = DataBindingUtil.setContentView(this, R.layout.activity_home);


        HomeObject mHome = new HomeObject(0, "Navy blue sandals", 34.50, "Beautiful fitted sandals for work");
        bindings.setHome(mHome);

    }
}
