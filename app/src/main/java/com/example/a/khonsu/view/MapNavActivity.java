package com.example.a.khonsu.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.a.khonsu.DatabaseOpenHelper;
import com.example.a.khonsu.R;
import com.example.a.khonsu.SensorService;
import com.example.a.khonsu.model.Floor;
import com.example.a.khonsu.model.Location;
import com.example.a.khonsu.model.Path;
import com.example.a.khonsu.model.Route;
import com.example.a.khonsu.util.CustomDialog;
import com.example.a.khonsu.util.ZoomLayout;

import java.util.Arrays;
import java.util.List;

/**
 *  Main feature of the app.
 */

public class MapNavActivity extends AppCompatActivity {

    //All variables here are declared protected, to be used in SensorServiceReceiver

    //  VIEW REFERENCES
    protected ImageView mPin, mMap;
    protected ZoomLayout mapLayout;
    protected int mapWidth, mapHeight, pinWidth, pinHeight = 0;

    protected boolean serviceNotRunning = true;
    protected SensorServiceReceiver mSensorReceiver;
    protected DatabaseOpenHelper dbHelper;

    protected Location startLoc;
    protected Route currentRoute;
    protected Path currentPath;
    protected double[] currentXY = new double[2]; // current XY coordination on screen
    protected double[] prevXY = new double[2]; // previous XY coordination on screen
    protected int currentPathIndex, stepsToTake;

    protected Bitmap bmp;
    protected Canvas c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new DatabaseOpenHelper(this);

        mMap = (ImageView)findViewById(R.id.map);
        mPin = (ImageView)findViewById(R.id.imagePin);
        mapLayout = (ZoomLayout) findViewById(R.id.map_layout);

        // Get current users' Location, which is sent from LoadLocation task in HomeFragment
        Intent intent = getIntent();
        startLoc = (Location) intent.getSerializableExtra(HomeFragment.START_LOCATION);
        currentXY[0] = startLoc.getLocationX();
        currentXY[1] = startLoc.getLocationY();

        // Get floor base on floorId of current Location. From this floor, display the floor maps in assets and set the title.
        Floor startFloor = dbHelper.getFloor(startLoc.getFloorId());
        if (startFloor == null) {
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
            int id = getResources().getIdentifier(startFloor.getMapPath(), "drawable", getPackageName());
            Drawable map = getResources().getDrawable(id, null);
            mMap.setBackground(map);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(startFloor.getFloorName());
            }
        }

        /*
         Wait until the Map and Pin has fully draw to get the Width and Height of both. From these values, draw a canvas base on map size, for
         drawing path later. Also, set the coordinates of pin to the current location on map, calculate by the relative coordinate in database,
         map's width and height, pin's width and height.
          */
        mMap.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMap.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mapWidth = mMap.getWidth();
                mapHeight = mMap.getHeight();
                bmp = Bitmap.createBitmap(mapWidth, mapHeight, Bitmap.Config.ARGB_8888);
                c = new Canvas(bmp);
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
            Toast.makeText(this, R.string.sensor_not_detected, Toast.LENGTH_SHORT).show();
            //terminate the app if sensors are not available
            System.exit(1);
        }

        // show instruction at the start of the Activity
        showInstructDialog();
    }

    // Set the coordinates of the Pin on map and zoom to it automatically.
    private void displayLocation() {
        if ( mapWidth != 0 && mapHeight != 0 && pinWidth != 0 && pinHeight != 0) {
            setPinOnMap(startLoc.getLocationX(), startLoc.getLocationY());
            ZoomLayout.setUpZoomAnimation(mapLayout, mPin.getPivotX(), mPin.getPivotY());
        }
    }

    /*
     The Pin is set based on its size and also the maps'. We need to calculate the X and Y to set the center
     of the pin right to the current location. This is done by the formula: Xcenter = X + imageView.width/2, similar
     to Y coordinate. Also set the pivot X and Y of pin for rotate Animation. If pivots is not set, the pin will rotate
     around old pivots when move to new X and Y.
      */
    protected void setPinOnMap(double relativeCoorX, double relativeCoorY) {
        mPin.setPivotX((float)(relativeCoorX*mapWidth));
        mPin.setPivotY((float)(relativeCoorY*mapHeight));
        mPin.setX((float)Math.abs(mPin.getWidth()/2-(relativeCoorX*mapWidth)));
        mPin.setY((float)Math.abs(mPin.getHeight()/2-(relativeCoorY*mapHeight)));
    }

    /*
     Basically get all other Locations in database than the current Location and display them on map. Inside a building, from one location
     there should be at least one possible route to get to another locations. After getting the destinations, display new pins at their coordinates.
     Position of the pins is calculate the same way as current location's pin.
      */
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
                // when user choose a destination, display a path how to get to it
                desPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentRoute = dbHelper.getRoute(startLoc, loc);
                        List<Path> paths = currentRoute.getPaths();
                        drawPaths(paths);

                        currentPathIndex = 0;
                        currentPath = currentRoute.getPaths().get(currentPathIndex);
                    }
                });
                la.addView(desPin, params);
                desPin.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        desPin.setX((float) Math.abs(desPin.getWidth() / 2 - (loc.getLocationX() * mapWidth)));
                        desPin.setY((float) Math.abs(desPin.getHeight() - (loc.getLocationY() * mapHeight)));
                    }
                });
            }
        }
    }

    /*
     Paths is drawn by calculating each start and end points of each path in the route.
     These points then transform onto screen coordinates.
      */
    private void drawPaths(List<Path> paths) {
        mMap.draw(c);
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(this, R.color.grey500));
        paint.setStrokeWidth(10);
        paint.setStrokeJoin(Paint.Join.ROUND);
        for(Path path : paths){
            float startX = (float) (path.getStartX() * mapWidth);
            float endX = (float) (path.getEndX() * mapWidth);
            float startY = (float) (path.getStartY() * mapHeight);
            float endY = (float) (path.getEndY() * mapHeight);
            c.drawLine(startX, startY, endX, endY, paint);
        }
        mMap.setImageBitmap(bmp);
    }

    // Start the sensorService, this is unbind service
    private void startService() {
        if (serviceNotRunning) {
            startService(new Intent(this, SensorService.class));
            serviceNotRunning = false;

            regsiterBroadCastReceivers();
        }
    }

    /*
     Creates and registers two intent filters - for direction and steps update
     */
    private void regsiterBroadCastReceivers() {
        IntentFilter directionFilter = new IntentFilter(SensorService.ANGLE_UPDATE);
        IntentFilter stepsFilter = new IntentFilter(SensorService.STEP_UPDATE);
        mSensorReceiver = new SensorServiceReceiver(this);
        registerReceiver(mSensorReceiver, stepsFilter);
        registerReceiver(mSensorReceiver, directionFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorReceiver != null ) {
            try {
                unregisterReceiver(mSensorReceiver);
                mSensorReceiver = null;
            } catch (Exception e) {
                mSensorReceiver  = null;
            }
        }
        serviceNotRunning = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showInstructDialog();
        return true;
    }

    private void showInstructDialog() {
        CustomDialog dialog = new CustomDialog(this, getString(R.string.dialog_title), getString(R.string.map_dialog_message));
        dialog.show();
    }
}
