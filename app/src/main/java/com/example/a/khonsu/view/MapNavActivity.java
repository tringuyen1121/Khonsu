package com.example.a.khonsu.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.a.khonsu.DatabaseOpenHelper;
import com.example.a.khonsu.R;
import com.example.a.khonsu.SensorService;
import com.example.a.khonsu.model.Floor;
import com.example.a.khonsu.model.Location;
import com.example.a.khonsu.util.ZoomLayout;

import java.util.List;

public class MapNavActivity extends AppCompatActivity {

    private ImageView mPin, mMap;
    private int mapWidth, mapHeight, pinWidth, pinHeight = 0;
    private double azimuth;
    public double currentAngle;
    public double preAngle;

    private boolean serviceNotRunning = true;
    private SensorServiceReceiver mSensorReceiverDirection, mSensorReceiverStep, mSensorReceiverAngle;
    private DatabaseOpenHelper dbHelper;

    public int stepCounter;

    private ZoomLayout mapLayout;
    private Location startLoc;
    private Floor startfloor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new DatabaseOpenHelper(this);

        mMap = (ImageView)findViewById(R.id.map);
        mPin = (ImageView)findViewById(R.id.imagePin);
        mapLayout = (ZoomLayout) findViewById(R.id.map_layout);

        Intent intent = getIntent();
        startLoc = (Location) intent.getSerializableExtra(HomeFragment.START_LOCATION);

        startfloor = dbHelper.getFloor(startLoc.getFloorId());
        if (startfloor == null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Error in fetching floor map. Try to update database again.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            finish();
        } else {
            int id = getResources().getIdentifier(startfloor.getMapPath(), "drawable", getPackageName());
            Drawable map = getResources().getDrawable(id, null);
            mMap.setBackground(map);
        }

        mMap.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMap.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mapWidth = mMap.getWidth();
                mapHeight = mMap.getHeight();
                displayLocation();
            }
        });

        mPin.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                pinWidth = mPin.getWidth();
                pinHeight = mPin.getHeight();
                displayLocation();
            }
        });

        Button startBtn = (Button)findViewById(R.id.start_tracking_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayPossibleDestination();
            }
        });

        startService();
        if (!SensorService.sensorAvailable) {
            //terminate the app if sensors are not available
            System.exit(1);
        }
    }

    private void displayLocation() {
        if ( mapWidth != 0 && mapHeight != 0 && pinWidth != 0 && pinHeight != 0) {
            mPin.setX((float)Math.abs(pinWidth/2-(startLoc.getLocationX()*mapWidth)));
            mPin.setY((float)Math.abs(pinHeight/2-(startLoc.getLocationY()*mapHeight)));
            mPin.setPivotX((float)(startLoc.getLocationX()*mapWidth));
            mPin.setPivotY((float)(startLoc.getLocationY()*mapHeight));
            mPin.requestLayout();

            setUpZoom();
        }
    }

    private void displayPossibleDestination() {
        List<Location> destinations = dbHelper.getAllLocation();
        RelativeLayout la = (RelativeLayout)findViewById(R.id.map_layout_child);
        for (final Location loc: destinations) {
            if (loc.getLocationId().intValue() != startLoc.getLocationId().intValue()) {
                final ImageView desPin = new ImageView(this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.pin_size), ViewGroup.LayoutParams.WRAP_CONTENT);
                desPin.setImageResource(R.drawable.ic_app_icon);
                desPin.setScaleType(ImageView.ScaleType.CENTER_CROP);
                desPin.setAdjustViewBounds(true);
                desPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                la.addView(desPin, params);

                desPin.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        desPin.setX((float) Math.abs(desPin.getWidth() / 2 - (loc.getLocationX() * mapWidth)));
                        desPin.setY((float) Math.abs(desPin.getHeight() - (loc.getLocationY() * mapHeight)));
                        desPin.requestLayout();
                    }
                });
            }
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
        mapLayout.startAnimation(sa);
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
        serviceNotRunning = true;
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
