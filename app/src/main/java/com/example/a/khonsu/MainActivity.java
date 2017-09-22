package com.example.a.khonsu;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.a.khonsu.Util.Filter.LowPassFilter;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /*
    Text View references
     */
    private TextView azimuthText;
    private TextView pitchText;
    private TextView rollText;
    private TextView directionText;

    /*
    SENSOR REFERENCES
     */
    private SensorManager ssManager;
    private Sensor accelSensor;
    private Sensor gyroSensor;
    private Sensor stepDetectSensor;
    private Sensor magnetSensor;

    // Accelerometer vector
    private float[] mAccel = new float[3];
    // Magnetometer vector
    private float[] mMagnet = new float[3];
    // Rotation matrix based on Accelerometer and Magnetometer
    private float[] mAccMagMatrix = new float[9];
    // Orientation angles from accelerometer and magnetometer
    private float[] mAccMagOrientation = new float[3];
    // Gyroscope Vector
    private float[] mGyro = new float[3];
    // Rotation matrix of Gyroscope
    private float[] mGyroMatrix = new float[9];
    // Orientation angles from Gyroscope
    private float[] mGyroOrientation = new float[3];
    // Final orientation from sensor fusion
    private float[] mFusedOrientation = new float[3];

    /*
    CONSTANTS
     */
    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;

    /*
    HELPER VARIABLES
     */
    private float timestampOldCalibrated;
    private boolean initState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelSensor = ssManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = ssManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        stepDetectSensor = ssManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        magnetSensor = ssManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Check if sensors are available, if not close application
        if (accelSensor == null || gyroSensor == null || stepDetectSensor == null || magnetSensor == null) {
            Log.e("Sensor: ", "Sensors not available");
            finish();
        }

        // Init matrices
        mGyroOrientation[0] = 0.0f;
        mGyroOrientation[1] = 0.0f;
        mGyroOrientation[2] = 0.0f;

        // Init gyroMatrix with identity matrix
        mGyroMatrix[0] = 1.0f; mGyroMatrix[1] = 0.0f; mGyroMatrix[2] = 0.0f;
        mGyroMatrix[3] = 0.0f; mGyroMatrix[4] = 1.0f; mGyroMatrix[5] = 0.0f;
        mGyroMatrix[6] = 0.0f; mGyroMatrix[7] = 0.0f; mGyroMatrix[8] = 1.0f;

        initListener();

        azimuthText = (TextView)findViewById(R.id.azimuth_text);
        pitchText = (TextView)findViewById(R.id.pitch_text);
        rollText = (TextView)findViewById(R.id.roll_text);
        directionText = (TextView)findViewById(R.id.direction_text);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ssManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initListener();
    }

    private void initListener() {
        ssManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        ssManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        ssManager.registerListener(this, stepDetectSensor, SensorManager.SENSOR_DELAY_GAME);
        ssManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccel = LowPassFilter.lowPass(sensorEvent.values.clone(), mAccel); //Apply low-pass filter to reduce noise
                calculateAccMagOrientation();
                break;
            case Sensor.TYPE_GYROSCOPE:
                mGyro = LowPassFilter.lowPass(sensorEvent.values.clone(), mGyro);
                calculateGyro(mGyro, sensorEvent.timestamp);
                Log.d("GyroVal", Arrays.toString(mGyro));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnet = LowPassFilter.lowPass(sensorEvent.values.clone(), mMagnet); //Apply low-pass filter to reduce noise
                break;
        }
    }

    // calculate initial orientation from accelerometer and magnetometer
    private void calculateAccMagOrientation() {
        if (SensorManager.getRotationMatrix(mAccMagMatrix, null, mAccel, mMagnet)) {
            Log.d("AccMagMatrix", Arrays.toString(mAccMagMatrix));
            SensorManager.getOrientation(mAccMagMatrix, mAccMagOrientation);
        }
    }

    // Calculate integration of gyroscope data.
    // Write orientation from gyroscope to mGyroOrientation
    private void calculateGyro(float[] gyroscope, long timestamp) {
        // Don't start until the initial orientation has been acquired
        if(mAccMagOrientation == null) {
            return;
        }

        // Init gyroscope based rotation matrix
        if(initState) {
            float[] initialRotationMatrix = getRotationMatrixFromOrientation(mAccMagOrientation);
            mGyroMatrix = matrixMultiplication(mGyroMatrix, initialRotationMatrix);
            initState = false;
        }

        // This timestep's delta rotation to be multiplied by the current
        // Rotation after computing it from the gyro sample data.
        float[] deltaVector = new float[4];
        if (timestampOldCalibrated != 0) {
            final float dT = (timestamp - timestampOldCalibrated) * NS2S;
            getRotationVectorFromGyro(gyroscope, deltaVector, dT / 2.0f);

            // Convert rotation vector into rotation matrix
            float[] deltaMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

            // Apply the new rotation interval on the gyroscope based rotation matrix
            mGyroMatrix = matrixMultiplication(mGyroMatrix, deltaMatrix);

            // get the gyroscope based orientation from the rotation matrix
            SensorManager.getOrientation(mGyroMatrix, mGyroOrientation);
            Log.d("GyroOrientation", Arrays.toString(mGyroOrientation));
            displayData(mAccMagOrientation[0], mAccMagOrientation[1], mAccMagOrientation[2]);
        }

        // measurement done, save current time for next interval
        timestampOldCalibrated = timestamp;
    }

    //Return rotation matrix from axis-angles in orientation vector
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9]; //init rotation matrix of x-axis
        float[] yM = new float[9]; //init rotation matrix of y-axis
        float[] zM = new float[9]; //init rotation matrix of z-axis

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        // rotation matrix is the result from multiplication of 3 matrices in 3 direction
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);

        return resultMatrix;
    }

    // TEMPORARY METHOD: Display data to View
    private void displayData(float azimuth, float pitch, float roll) {
        float azimuthInDegrees = (float)(Math.toDegrees(azimuth)+360)%360;
        float pitchInDegrees = (float)(Math.toDegrees(pitch)+360)%360;
        float rollInDegrees = (float)(Math.toDegrees(roll)+360)%360;

        azimuthText.setText(String.valueOf(azimuthInDegrees));
        pitchText.setText(String.valueOf(pitchInDegrees));
        rollText.setText(String.valueOf(rollInDegrees));

        if ((azimuthInDegrees >= 0 && azimuthInDegrees < 22) || azimuthInDegrees >=338 ) {
            directionText.setText("North");
        } else if (azimuthInDegrees >= 22 && azimuthInDegrees < 68 ) {
            directionText.setText("North-East");
        } else if (azimuthInDegrees >= 68 && azimuthInDegrees < 112 ) {
            directionText.setText("East");
        } else if (azimuthInDegrees >= 112 && azimuthInDegrees < 158 ) {
            directionText.setText("South-East");
        } else if (azimuthInDegrees >= 158 && azimuthInDegrees < 202 ) {
            directionText.setText("South");
        } else if (azimuthInDegrees >= 202 && azimuthInDegrees < 248 ) {
            directionText.setText("South-West");
        } else if (azimuthInDegrees >= 248 && azimuthInDegrees < 292 ) {
            directionText.setText("West");
        } else if (azimuthInDegrees >= 292 && azimuthInDegrees < 338 ) {
            directionText.setText("North-West");
        } else {
            directionText.setText("Error");
        }
    }

    // Calculate a rotation vector from the gyroscope angular speed values
    private void getRotationVectorFromGyro(float[] gyroscope, float[] deltaRotationVector, float timeFactor) {

        // Axis of the rotation sample, not normalized yet.
        float axisX = gyroscope[0];
        float axisY = gyroscope[1];
        float axisZ = gyroscope[2];

        // The angular speed of the sample
        float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

        //// Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON)
        {
            axisX /= omegaMagnitude;
            axisY /= omegaMagnitude;
            axisZ /= omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * axisX;
        deltaRotationVector[1] = sinThetaOverTwo * axisY;
        deltaRotationVector[2] = sinThetaOverTwo * axisZ;
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    // Helper method to multiply two 3x3 matrices
    private float[] matrixMultiplication(float[] a, float[] b) {
        float[] result = new float[9];

        result[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
        result[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
        result[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];

        result[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
        result[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
        result[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];

        result[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
        result[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
        result[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

        return result;
    }
}
