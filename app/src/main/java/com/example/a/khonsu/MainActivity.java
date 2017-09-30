package com.example.a.khonsu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity{

    /*
    Text View references
     */
    private ImageView startBtn;
    private ImageView mMap;

    private boolean serviceNotRunning = true;
    private SensorServiceReceiver mSensorReceiverDirection;
    private SensorServiceReceiver mSensorReceiverStep;

    public int stepCounter;
    public int angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (ImageView) findViewById(R.id.service_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
                if (!SensorService.sensorAvailable) {
                    //terminate the app if sensors are not available
                    finish();
                }
            }
        });

        mMap = (ImageView)findViewById(R.id.imageView);
        mMap.setImageResource(R.drawable.ic_thirdfloor);
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

    private void startService(){
        if(serviceNotRunning){
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

    private class SensorServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SensorService.STEP_UPDATE)) {
                stepCounter = intent.getIntExtra(SensorService.STEPS, 0);
            }
            else if(intent.getAction().equals(SensorService.DIRECT_UPDATE)) {
                angle = intent.getIntExtra(SensorService.ANGLE,0);

            }

            System.out.println(stepCounter + " " + angle);
        }
    }


}
