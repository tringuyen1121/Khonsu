package com.example.a.khonsu;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;

public class SensorService extends Service implements SensorEventListener {

    /*
    SENSOR REFERENCES
     */
    private SensorManager ssManager;
    private Sensor gravitySensor;
    private Sensor stepDetectSensor;
    private Sensor magnetSensor;
    private Sensor rotationSensor;

    // Accelerometer vector
    private float[] mAccel = new float[3];
    // Rotation sensor vector
    private float[] mRotation = new float[3];
    // Magnetometer vector
    private float[] mMagnet = new float[3];
    // Rotation matrix based on Accelerometer and Magnetometer
    private float[] mAccMagMatrix = new float[9];
    // Orientation angles from accelerometer and magnetometer
    private float[] mRotationMatrixFromVector = new float[9];
    // Orientation angles from accelerometer and magnetometer
    private float[] mOrientation = new float[3];

    /*
    CONSTANTS
     */
    private static final String TAG = "com.exmaple.a.khonsu.SensorService";
    public static final String STEP_UPDATE = TAG + ".action.STEP_UPDATE";
    public static final String DIRECT_UPDATE = TAG + ".action.DIRECTION_UPDATE";
    public static final String STEPS = "STEPS";
    public static final String ANGLE = "ANGLE";

    /*
    OTHER VARIABLES
     */
    public static boolean sensorAvailable = true;
    private boolean hasRotationSensor = true;
    private int mStep = 0;
    private int currentAngle = 0;
    private int prevAngle = 0;
    private boolean isWalking = false;



    public static final String AZIMUTH = "AZIMUTH";
    public static final String PITCH = "PITCH";
    public static final String ROLL = "ROLL";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ssManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        rotationSensor = ssManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        gravitySensor = ssManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepDetectSensor = ssManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        magnetSensor = ssManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Check if sensors are available, if not close application
        if (rotationSensor == null) { hasRotationSensor = false; }
        if (gravitySensor == null || stepDetectSensor == null || magnetSensor == null) {
            sensorAvailable = false;
        }

        initListener();

        return START_NOT_STICKY;
    }

    private void initListener() {
        ssManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        ssManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        ssManager.registerListener(this, stepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);
        ssManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ssManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, mRotation, 0, 3);
                calculateOrientation();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mAccel, 0, 3);
                calculateOrientation();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mMagnet, 0, 3);
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                if (event.values[0] == 1.0) {
                    isWalking = true;
                    mStep++;
                    announceChange(STEP_UPDATE);
                } else {
                    isWalking = false;
                }
                break;
        }
    }

    private void calculateOrientation() {
        // If phone doesn't have Rotation Vector sensor, calculate orientation based on Accelerometer and Magnetometer
        if (SensorManager.getRotationMatrix(mAccMagMatrix, null, mAccel, mMagnet) && !hasRotationSensor) {
            Log.d("AccMagMatrix", Arrays.toString(mAccMagMatrix));
            SensorManager.getOrientation(mAccMagMatrix, mOrientation);
        } else {
            SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, mRotation);
            SensorManager.getOrientation(mRotationMatrixFromVector, mOrientation);
        }

        // Calculate azimuth to detect direction
        double azimuth = Math.toDegrees(mOrientation[0]);
        currentAngle = (int)azimuth;
        if((prevAngle - currentAngle) > 45){
            Log.v("<-", "Turning LEFT");
            announceChange(DIRECT_UPDATE);
            prevAngle = currentAngle;
        } else if ((prevAngle - currentAngle) < -45) {
            Log.v("->", "Turning RIGHT");
            announceChange(DIRECT_UPDATE);
            prevAngle = currentAngle;
        }
    }

    private void announceChange(String type) {
        if (type.equals(STEP_UPDATE)) {
            Intent intent = new Intent(STEP_UPDATE);
            intent.putExtra(STEPS, mStep);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(DIRECT_UPDATE);
            intent.putExtra(ANGLE, currentAngle);
            sendBroadcast(intent);
        }
    }
}
