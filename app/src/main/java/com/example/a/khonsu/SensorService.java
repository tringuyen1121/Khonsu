package com.example.a.khonsu;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.a.khonsu.util.filter.LowPassFilter;

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
    public static final String ANGLE_UPDATE = TAG + ".action.ANGLE_UPDATE";
    public static final String STEPS = "STEPS";
    public static final String ANGLE = "ANGLE";

    /*
    OTHER VARIABLES
     */
    public static boolean sensorAvailable = true;
    private boolean hasRotationSensor = true;
    private boolean mStep = false;
    private double currentAzimuth, preAzimuth;

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
                LowPassFilter.lowPass(event.values.clone(), mRotation);
                calculateOrientation();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                LowPassFilter.lowPass(event.values.clone(), mAccel);
                calculateOrientation();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                LowPassFilter.lowPass(event.values.clone(), mMagnet);
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                mStep = event.values[0] == 1.0;
                announceChange(STEP_UPDATE);
                break;
        }
    }

    private void calculateOrientation() {
        // If phone doesn't have Rotation Vector sensor, calculate orientation based on Accelerometer and Magnetometer
        if (SensorManager.getRotationMatrix(mAccMagMatrix, null, mAccel, mMagnet) && !hasRotationSensor) {
            SensorManager.getOrientation(mAccMagMatrix, mOrientation);
        } else {
            SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, mRotation);
            SensorManager.getOrientation(mRotationMatrixFromVector, mOrientation);
        }

        // Calculate azimuth to detect direction
        currentAzimuth = Math.toDegrees(mOrientation[0]);

        if(Math.abs(currentAzimuth - preAzimuth) >= 2.0) {
            announceChange(ANGLE_UPDATE);
            preAzimuth = currentAzimuth;
        }
    }

    private void announceChange(String type) {
        if (type.equals(STEP_UPDATE)) {
            Intent intent = new Intent(STEP_UPDATE);
            intent.putExtra(STEPS, mStep);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(ANGLE_UPDATE);
            intent.putExtra(ANGLE, currentAzimuth);
            sendBroadcast(intent);
        }
    }
}
