package com.example.a.khonsu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a.khonsu.Util.ZoomLayout;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    /*
    Text View references
     */
    private Button startBtn;
    private ImageView mMap;
    private ImageView mPin;
    private TextView coordinatesText;

    private boolean serviceNotRunning = true;
    private SensorServiceReceiver mSensorReceiverDirection;
    private SensorServiceReceiver mSensorReceiverStep;
    private LocationProvider mLocProvider;

    public int stepCounter;
    public int angle;

    private double[] coordinates = new double[2];
    private double[] PHYSICAL_COORDINATES = {60.221448, 24.804610,
                                            60.222040, 24.804610,
                                            60.221448, 24.804597,
                                            60.221435, 24.804597};

    private Class mARHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocProvider = new LocationProvider(this);

        coordinatesText = (TextView) findViewById(R.id.coordinates);

        startBtn = (Button) findViewById(R.id.service_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Intent intent = new Intent(MainActivity.this, mARHandler);
                startActivity(intent);
//                if(mLocProvider.isConnected) {
//                    coordinates = mLocProvider.getCoordinates();
//                }
//                Log.v("Coordinates", Arrays.toString(calculateCoordinatesOnScreen(coordinates)));
//
//                mPin.setX((float) calculateCoordinatesOnScreen(coordinates)[0]);
//                mPin.setY((float) calculateCoordinatesOnScreen(coordinates)[1]);
//                mPin.setImageResource(R.drawable.ic_place_black_24dp);
//
//                startService();
//                if (!SensorService.sensorAvailable) {
//                    //terminate the app if sensors are not available
//                    finish();
//                }
            }
        });

        final ZoomLayout zoomlayout = (ZoomLayout) findViewById(R.id.zoom_layout);
        zoomlayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                zoomlayout.init(MainActivity.this);
                return false;
            }
        });

        mMap = (ImageView) findViewById(R.id.imageView);
        mMap.setImageResource(R.drawable.ic_thirdfloor);

        mPin = (ImageView) findViewById(R.id.imagePin);

        mARHandler = ARHandler.class;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
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

    private void startService() {
        if (serviceNotRunning) {
            startService(new Intent(MainActivity.this, SensorService.class));
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
    }

    @Override
    public void onStart() {
        mLocProvider.connectService();
        super.onStart();
    }

    @Override
    public void onStop() {
        mLocProvider.disconnectService();
        super.onStop();
    }

    private class SensorServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SensorService.STEP_UPDATE)) {
                stepCounter = intent.getIntExtra(SensorService.STEPS, 0);
            } else if (intent.getAction().equals(SensorService.DIRECT_UPDATE)) {
                angle = intent.getIntExtra(SensorService.ANGLE, 0);
            }

            System.out.println(stepCounter + " " + angle);
            updateGUI();
        }

        private void updateGUI() {

        }
    }

    private double getScreenDistance(double x1, double y1, double x2, double y2) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double xDist = Math.pow(Math.abs(x1 - x2) / dm.xdpi, 2);
        double yDist = Math.pow(Math.abs(y1 - y2) / dm.ydpi, 2);
        return Math.sqrt(xDist + yDist);
    }

    private double[] calculateCoordinatesOnScreen(double[] coordinates) {
        double[] result = new double[2];
        Log.v("Measurement", mMap.getWidth() + " " + mMap.getHeight());

        result[0] = ((coordinates[0] - PHYSICAL_COORDINATES[0])/(PHYSICAL_COORDINATES[2] - PHYSICAL_COORDINATES[0]))*mMap.getWidth();
        result[1] = ((coordinates[1] - PHYSICAL_COORDINATES[5])/(PHYSICAL_COORDINATES[1] - PHYSICAL_COORDINATES[5]))*mMap.getHeight();

        return result;
    }

}
