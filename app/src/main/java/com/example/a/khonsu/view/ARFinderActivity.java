package com.example.a.khonsu.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.craftar.CraftARActivity;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARResult;
import com.craftar.CraftARSDK;
import com.craftar.CraftARSearchResponseHandler;
import com.example.a.khonsu.DatabaseOpenHelper;
import com.example.a.khonsu.R;
import com.example.a.khonsu.model.Location;

import java.util.ArrayList;

public class ARFinderActivity extends CraftARActivity implements CraftARSearchResponseHandler {

    private final static String TAG = "ARFinderActivity";

    CraftAROnDeviceIR mOnDeviceIR;
    CraftARSDK mCraftARSDK;
    View mScanningLayout;
    long startFinderTimeMillis;
    private final static long FINDER_SESSION_TIME_MILLIS= 10000;
    boolean mIsActivityRunning = false;

    private DatabaseOpenHelper dbHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseOpenHelper(this);
    }

    @Override
    public void onPostCreate() {
        setContentView(R.layout.activity_ar_recognition);

        //Obtain an instance of the CraftARSDK (which manages the camera interaction).
        //Note we already called CraftARSDK.init() in the Splash Screen, so we don't have to do it again
        mCraftARSDK = CraftARSDK.Instance();
        mCraftARSDK.startCapture(this);

        //Get the instance to the OnDeviceIR singleton (it has already been initialized in the SplashScreenActivity, and the collectoins are already loaded).
        mOnDeviceIR = CraftAROnDeviceIR.Instance();

        //Tell the SDK that the OnDeviceIR who manage the calls to singleShotSearch() and startFinding().
        //In this case, as we are using on-device-image-recognition, we will tell the SDK that the OnDeviceIR singleton will manage this calls.
        mCraftARSDK.setSearchController(mOnDeviceIR.getSearchController());

        //Tell the SDK that we want to receive the search responses in this class.
        mOnDeviceIR.setCraftARSearchResponseHandler(this);

        mScanningLayout = findViewById(R.id.layout_scanning);

        startFinding();
    }

    @Override
    public void onCameraOpenFailed() {
        Toast.makeText(getApplicationContext(), "Camera error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPreviewStarted(int width, int height) {
        Log.d(TAG, "Preview started with width:"+width+", height:"+height);
    }

    @Override
    public void searchResults(ArrayList<CraftARResult> results,
                              long searchTimeMillis, int requestCode) {
        //Callback with the search results

        if(results.size() > 0){
            //We found something! Show the results
            stopFinding();
            onItemFound(results);
        }else{
            long ellapsedTime = System.currentTimeMillis() - startFinderTimeMillis;
            if(ellapsedTime > FINDER_SESSION_TIME_MILLIS ){
                stopFinding();
                //No object were found during this session
                showNoObjectsDialog();
            }
        }
    }

    private void showNoObjectsDialog(){
        if(!mIsActivityRunning){
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("No objects found");
        dialogBuilder.setMessage("Point to an object of the " + LaunchingFragment.COLLECTION_TOKEN + " collection");
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startFinding();
            }
        });

        dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.show();
    }

    private void onItemFound(ArrayList<CraftARResult> results) {
        if(!mIsActivityRunning){
            return;
        }
        String resultsText="";
        for(CraftARResult result:results){
            String uuid = result.getMatchedImage().getUUID();
            resultsText+= uuid + "\n";
        }
        resultsText = resultsText.substring(0,resultsText.length() - 1); //Eliminate the last \n

        LoadLocation task = new LoadLocation(this);
        task.execute(resultsText);
        finish();
    }

    private void startFinding(){
        mScanningLayout.setVisibility(View.VISIBLE);
        mCraftARSDK.startFinder();
        startFinderTimeMillis= System.currentTimeMillis();
    }

    private void stopFinding(){
        mCraftARSDK.stopFinder();
        mScanningLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    public void searchFailed(CraftARError error, int requestCode) {
        Log.e(TAG, "Search failed(" + error.getErrorCode()+"):"+error.getErrorMessage());
    }

    @Override
    protected void onStop(){
        super.onStop();
        mIsActivityRunning = false;
    }

    @Override
    public void onPause(){
        super.onPause();
        stopFinding();
    }

    @Override
    protected void onStart(){
        super.onStart();
        mIsActivityRunning = true;
    }

    private class LoadLocation extends AsyncTask<String, Void, Location> {

        private Context context;

        public LoadLocation(Context context) {
            this.context = context;
        }

        @Override
        protected Location doInBackground(String... strings) {
            return dbHelper.getStartLocation(strings[0]);
        }

        @Override
        protected void onPostExecute(Location location) {
            if (location == null) {
                Toast.makeText(getApplicationContext(), "Location is not found", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(context, MapNavActivity.class);
                intent.putExtra(HomeFragment.START_LOCATION, location);
                startActivity(intent);
            }
        }
    }
}