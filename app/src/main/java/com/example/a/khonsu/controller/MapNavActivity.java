package com.example.a.khonsu.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.example.a.khonsu.R;
import com.example.a.khonsu.SensorService;
import com.example.a.khonsu.util.ZoomLayout;

public class MapNavActivity extends AppCompatActivity {

    private ImageView mPin, mMap;
    private int mapWidth, mapHeight, pinWidth, pinHeight = 0;
    private String uuid;
    private double azimuth;
    public double currentAngle;
    public double preAngle;

    private boolean serviceNotRunning = true;
    private SensorServiceReceiver mSensorReceiverDirection, mSensorReceiverStep, mSensorReceiverAngle;

    public int stepCounter;

    private ZoomLayout mapLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        uuid = intent.getExtras().getString("UUID");

        mMap = (ImageView)findViewById(R.id.map);
        mMap.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMap.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mapWidth = mMap.getWidth();
                mapHeight = mMap.getHeight();
                displayLocation();
            }
        });

        mPin = (ImageView)findViewById(R.id.imagePin);
        mPin.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                pinWidth = mPin.getWidth();
                pinHeight = mPin.getHeight();
                displayLocation();
            }
        });

        mapLayout = (ZoomLayout) findViewById(R.id.map_layout);

        Button startBtn = (Button)findViewById(R.id.start_tracking_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        startService();
        if (!SensorService.sensorAvailable) {
            //terminate the app if sensors are not available
            System.exit(1);
        }
    }

    private void displayLocation() {
        double[] coordinates = fetchData(uuid);
        if ( mapWidth != 0 && mapHeight != 0 && pinWidth != 0 && pinHeight != 0) {
            mPin.setX((float)Math.abs(pinWidth/2-(coordinates[0]*mapWidth)));
            mPin.setY((float)Math.abs(pinHeight/2-(coordinates[1]*mapHeight)));
            mPin.setPivotX((float)(coordinates[0]*mapWidth));
            mPin.setPivotY((float)(coordinates[1]*mapHeight));
            mPin.requestLayout();

            setUpZoom();
        }
    }

    private void setUpZoom() {
        ScaleAnimation sa = new ScaleAnimation(0, 2.5f, 0, 2.5f, mPin.getPivotX(), mPin.getPivotY());
        sa.setDuration(1000);
        sa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mapLayout.applyScaleAndTranslation(mPin.getPivotX(),  mPin.getPivotY(), 2.5f);
                mapLayout.setPivotX(mPin.getPivotX());
                mapLayout.setPivotY(mPin.getPivotY());
                mapLayout.setScale(2.5f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        //sa.setFillAfter(true);
        mapLayout.startAnimation(sa);
    }

    private double[] fetchData(String location) {
        double[] result = new double[2];
        switch (location) {
            case "66e9a4c00a5240138e9eb747540738ea":
            case "b302":
                result[0] = 0.8028;
                result[1] = 0.1119;
                break;
            case "e44e432dffa1401d8da43550940e95de":
            case "b327":
                result[0] = 0.8028;
                result[1] = 0.7308;
                break;
            default:
                result[0] = 0;
                result[1] = 0;
                break;
        }
        return result;
    }

    private void startService() {
        if (serviceNotRunning) {
            startService(new Intent(this, SensorService.class));
            serviceNotRunning = false;

            regsiterBroadCastReceivers();
        }
    }

    /**
     * Creates and registers two intent filters - for direction and steps update
     */
    private void regsiterBroadCastReceivers() {
        IntentFilter directionFilter = new IntentFilter(SensorService.DIRECT_UPDATE);
        mSensorReceiverDirection = new SensorServiceReceiver();
        registerReceiver(mSensorReceiverDirection, directionFilter);
        IntentFilter stepsFilter = new IntentFilter(SensorService.STEP_UPDATE);
        mSensorReceiverStep = new SensorServiceReceiver();
        registerReceiver(mSensorReceiverStep, stepsFilter);
        IntentFilter anglesFilter = new IntentFilter("Azimuth");
        mSensorReceiverAngle = new SensorServiceReceiver();
        registerReceiver(mSensorReceiverAngle, anglesFilter);

    }

    @Override
    public void onStart() {
        //mLocProvider.connectService();
        super.onStart();
    }

    @Override
    public void onStop() {
        //mLocProvider.disconnectService();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorReceiverDirection != null || mSensorReceiverStep != null) {
            try {
                unregisterReceiver(mSensorReceiverDirection);
                unregisterReceiver(mSensorReceiverStep);
                mSensorReceiverDirection = null;
                mSensorReceiverStep = null;
            } catch (Exception e) {
                mSensorReceiverDirection = null;
                mSensorReceiverStep = null;
            }
        }
    }

    private class SensorServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SensorService.STEP_UPDATE)) {
                stepCounter = intent.getIntExtra(SensorService.STEPS, 0);
            } else if (intent.getAction().equals(SensorService.DIRECT_UPDATE)) {
                //ngle = intent.getIntExtra(SensorService.ANGLE, 0);
            } else if (intent.getAction().equals("Azimuth")) {
                Log.i("Map", "Receive broadcast");
                azimuth = intent.getDoubleExtra("Azimuth", 0);
                currentAngle = azimuth;
            }

            RotateAnimation ra = new RotateAnimation(
                    (float) preAngle, (float) currentAngle, mPin.getPivotX(), mPin.getPivotY());
            ra.setDuration(210);
            ra.setFillAfter(true);
            mPin.setAnimation(ra);
            mPin.startAnimation(ra);
            //System.out.println(stepCounter + " " + angle);
            //updateGUI();
            preAngle = currentAngle;
        }

        private void updateGUI() {

        }
    }
}
