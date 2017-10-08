package com.example.a.khonsu.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.example.a.khonsu.R;
import com.example.a.khonsu.util.RuntimePermissions;


public class MainActivity extends RuntimePermissions implements LaunchingFragment.LaunchingListener {

    private static final int LOCATION_REQUEST_PERMISSIONS = 101;
    private static final int CAMERA_REQUEST_PERMISSIONS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up Listener to replace launchingFragment with HomeFragment after finishing fetching data.
        LaunchingFragment launchingFragment = new LaunchingFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, launchingFragment).commit();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        switch (requestCode) {
            case LOCATION_REQUEST_PERMISSIONS:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case CAMERA_REQUEST_PERMISSIONS:
                Intent intent = new Intent(this, ARFinderActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*
    After finish fetching data, ask users to grant permission of using Location Service. This application relies "heavily"
    on Location to perform its functions. "Although there is not current Location Service use at the moment, but still, for extension :)".
    After permission is granted, replace HomeFragment
     */
    @Override
    public void onFinishFetchData() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            MainActivity.super.requestAppPermissions(new
                            String[]{Manifest.permission.ACCESS_FINE_LOCATION}, R.string.runtime_permissions_txt
                    , LOCATION_REQUEST_PERMISSIONS);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    // When user click cameraBtn on HomeFragment, check permission is call on this Activity.
    public void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            MainActivity.super.requestAppPermissions(new
                            String[]{Manifest.permission.CAMERA}, R.string.runtime_permissions_txt
                    , CAMERA_REQUEST_PERMISSIONS);
        } else {
            Intent intent = new Intent(this, ARFinderActivity.class);
            startActivity(intent);
        }
    }
}
