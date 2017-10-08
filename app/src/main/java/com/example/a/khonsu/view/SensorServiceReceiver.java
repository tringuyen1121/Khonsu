package com.example.a.khonsu.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.example.a.khonsu.SensorService;
import com.example.a.khonsu.model.Path;
import com.example.a.khonsu.util.Constants;

/**
 * Broadcast Receiver receives data sent from Sensor Service
 */

public class SensorServiceReceiver extends BroadcastReceiver {

    // MapActivity references to access its variable, send data and update views.
    private final MapNavActivity mMapNavActivity;

    private double currentAzimuth;
    private double preAzimuth;
    private boolean canUpdateLocation;
    private int takenStep = 0;
    private double incrementX;
    private double incrementY;

    private Toast dirToast;

    public SensorServiceReceiver(MapNavActivity activity) {
        this.mMapNavActivity = activity;
    }

    /*
    Data are received from SensorService. Depend on intent type, perform different methods. If data is from Orientation Sensors
    (Rotation Vector or Accelerometer and Magnetometer), animate the pin and also detect if users facing the right direction or not.
    If data is from Step Detector, start updating users' location.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SensorService.STEP_UPDATE)) {
            boolean isWalking = intent.getBooleanExtra(SensorService.STEPS, false);
            if (isWalking) {
                updateLocation();
            }
        } else if (intent.getAction().equals(SensorService.ANGLE_UPDATE)) {
            currentAzimuth = intent.getDoubleExtra(SensorService.ANGLE, 0);
            animateNavigator();
            isCorrectDirection();
        }
    }

    /*
    If users face the right direction of current Path (for example, the path shows users should head north, then they should face north),
    if the direction of phone is right, then we can calculate new location when users start walking.
     */
    private void isCorrectDirection() {
        if (mMapNavActivity.currentRoute != null) {
            if (currentDirection(currentAzimuth) == mMapNavActivity.currentPath.getDir()) {
                canUpdateLocation = true;
            } else {
                if (dirToast != null) dirToast.cancel();
                dirToast = Toast.makeText(mMapNavActivity, "Oops, wrong direction", Toast.LENGTH_SHORT);
                dirToast.show();
                canUpdateLocation = false;
            }
        }
    }

    // For translate the pin on map
    private void translateAnimPin() {
        /*
        This section is to recalculate the TEMPORARY X and Y for the View to translate to. Because many chances the pin will
        be rotated before it is translated, relative coordinates of the pin is also rotated ("coder thinks so"), therefore
        if we not calculate the new X and Y, the pin will be translated according to how it is rotated (i.e if the pin is rotated
        180 degrees clockwise, then if it is translated downward, it will appear upward on the screen). "I don't know why"!!!!!
        These formulas are conducted based on concept on Translate Matrices, but have been simplified. Mostly using try and fail :)
         */
        double rotatedAngle = Math.toRadians(currentAzimuth);
        float newX = (float) (Math.cos(rotatedAngle)*(mMapNavActivity.currentXY[0] - mMapNavActivity.prevXY[0]) +
                Math.sin(rotatedAngle) * (mMapNavActivity.currentXY[1] - mMapNavActivity.prevXY[1]));
        float newY = (float) (-Math.sin(rotatedAngle)*(mMapNavActivity.currentXY[0] - mMapNavActivity.prevXY[0]) +
                Math.cos(rotatedAngle) * (mMapNavActivity.currentXY[1] - mMapNavActivity.prevXY[1]));

        mMapNavActivity.mPin.animate().translationXBy(newX*mMapNavActivity.mapWidth).setDuration(150).start();
        mMapNavActivity.mPin.animate().translationYBy(newY*mMapNavActivity.mapHeight).setDuration(150).start();
    }

    // Rotate the pin according to current orientation of the phone.
    private void animateNavigator() {
        /*
         Before rotating, set the pin to the RIGHT position, that is where it should appear on screen using translate
         animation (but it doesn't, "I don't know why!!!!!")
         */
        mMapNavActivity.setPinOnMap(mMapNavActivity.currentXY[0], mMapNavActivity.currentXY[1]);

        RotateAnimation ra = new RotateAnimation(
                (float) preAzimuth, (float) currentAzimuth, mMapNavActivity.mPin.getPivotX(), mMapNavActivity.mPin.getPivotY());
        ra.setDuration(150);
        ra.setFillAfter(true);
        mMapNavActivity.mPin.startAnimation(ra);
        preAzimuth = currentAzimuth;
    }

    /*
    Update new Location of users and set the pin on map. At the moment, Paths in database have only 1 of 8 directions.
    The new location is calculated based on the direction of path.
     */
    private void updateLocation() {
        // Check if users face the right direction first.
        if (canUpdateLocation) {
            // if current Path is the first Path, we need to calculate data beforehand (or NullPointerException).
            if (mMapNavActivity.currentPathIndex == 0) {
                prepareData();
            }
            //
            /*
            takenStep smaller than stepsToTake means that users have not finished current Path. If so update the pin
            on map to the new location ON current Path.
             */
            if (takenStep < mMapNavActivity.stepsToTake) {
                // Store currentXY values before changing for translate animation
                mMapNavActivity.prevXY[0] = mMapNavActivity.currentXY[0];
                mMapNavActivity.prevXY[1] = mMapNavActivity.currentXY[1];

                // Depend on the direction, new location are calculate in this way
                switch (mMapNavActivity.currentPath.getDir()) {
                    case NORTH:
                        mMapNavActivity.currentXY[1] -= incrementY;
                        break;
                    case EAST:
                        mMapNavActivity.currentXY[0] += incrementX;
                        break;
                    case SOUTH:
                        mMapNavActivity.currentXY[1] += incrementY;
                        break;
                    case WEST:
                        mMapNavActivity.currentXY[0] -= incrementX;
                        break;
                    case NORTH_EAST:
                        mMapNavActivity.currentXY[0] += incrementX;
                        mMapNavActivity.currentXY[1] -= incrementY;
                        break;
                    case NORTH_WEST:
                        mMapNavActivity.currentXY[0] -= incrementX;
                        mMapNavActivity.currentXY[1] -= incrementY;
                        break;
                    case SOUTH_EAST:
                        mMapNavActivity.currentXY[0] += incrementX;
                        mMapNavActivity.currentXY[1] += incrementY;
                        break;
                    case SOUTH_WEST:
                        mMapNavActivity.currentXY[0] -= incrementX;
                        mMapNavActivity.currentXY[1] += incrementY;
                        break;
                }
                // Users has finish a step, increment takenStep and also animate the pin
                translateAnimPin();
                takenStep++;

                // if takenStep is equal to stepsToTake, users has completed the current path, change to next path
                if (takenStep == mMapNavActivity.stepsToTake) {
                    mMapNavActivity.currentPathIndex ++;

                    /*
                     If path is the last path of current Route, stop tracking by unregister this receiver, also make
                     a small text to announce user has reached destination. If not get data of new path and reset takenSteps
                     to start tracking again
                      */
                    if (mMapNavActivity.currentPathIndex == mMapNavActivity.currentRoute.getPaths().size()) {
                        Toast.makeText(mMapNavActivity, "Hurray you have reached destination!", Toast.LENGTH_SHORT).show();
                        takenStep = 0;
                        mMapNavActivity.stepsToTake = 0;
                        mMapNavActivity.unregisterReceiver(this);
                    } else {
                        takenStep = 0;
                        mMapNavActivity.currentPath = mMapNavActivity.currentRoute
                                .getPaths()
                                .get(mMapNavActivity.currentPathIndex);
                        prepareData();
                    }
                }
            }
        }
    }

    /*
    A path has distance, and based on AVERAGE_STEP_LENGTH, which is a constant (collected from testing), it will be into small sections
    called stepsToTake. It implies (and expects) that users will take that amount of steps to complete the path. A length of a section is
    called incrementX and incrementY, determine the new position after each step.
     */
    private void prepareData() {
        mMapNavActivity.stepsToTake = (int)((mMapNavActivity.currentPath.getDistance() / Constants.STEP.AVERAGE_STEP_LENGTH) + 0.5); //round to nearest Integer
        incrementX = Math.abs(mMapNavActivity.currentPath.getStartX()- mMapNavActivity.currentPath.getEndX()) / mMapNavActivity.stepsToTake;
        incrementY = Math.abs(mMapNavActivity.currentPath.getStartY() - mMapNavActivity.currentPath.getEndY()) / mMapNavActivity.stepsToTake;
    }

    // Return the current direction of users based on Azimuth
    private Path.DIRECTION currentDirection(double azimuth) {
        azimuth = (azimuth + 360) % 360;
        if (azimuth > 337 || azimuth <= 23) {
            return Path.DIRECTION.NORTH;
        } else if (azimuth <= 68) {
            return Path.DIRECTION.NORTH_EAST;
        } else if (azimuth <= 113) {
            return Path.DIRECTION.EAST;
        } else if (azimuth <= 157) {
            return Path.DIRECTION.SOUTH_EAST;
        } else if (azimuth <= 203) {
            return Path.DIRECTION.SOUTH;
        } else if (azimuth <= 247) {
            return Path.DIRECTION.SOUTH_WEST;
        } else if (azimuth <= 293) {
            return Path.DIRECTION.WEST;
        } else if (azimuth <= 337) {
            return Path.DIRECTION.NORTH_WEST;
        } else {
            return Path.DIRECTION.UNDEFINED;
        }
    }
}

