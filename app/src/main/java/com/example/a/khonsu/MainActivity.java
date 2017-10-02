package com.example.a.khonsu;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.example.a.khonsu.Util.RuntimePermissions;


public class MainActivity extends RuntimePermissions implements Launching.LaunchingListener {

    private static final int REQUEST_PERMISSIONS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Launching launchingFragment = new Launching();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, launchingFragment).commit();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeActivity()).commit();
    }

    @Override
    public void onFinishFetchData() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            MainActivity.super.requestAppPermissions(new
                            String[]{Manifest.permission.ACCESS_FINE_LOCATION}, R.string.runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeActivity()).commit();
        }
    }
}
