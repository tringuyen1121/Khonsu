package com.example.a.khonsu;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceCollection;
import com.craftar.CraftAROnDeviceCollectionManager;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARSDK;
import com.craftar.SetOnDeviceCollectionListener;

import java.util.List;


public class Launching extends Fragment implements SetOnDeviceCollectionListener,
        CraftAROnDeviceCollectionManager.AddCollectionListener, CraftAROnDeviceCollectionManager.SyncCollectionListener {

    final private String TAG = getClass().getSimpleName();

    private ImageView logo;
    private Animation zoomIn, zoomOut;
    private ActionBar actionBar;

    private LaunchingListener mListener;

    //Collection token of the stickers for retrieve collection from CraftAR
    public final static String COLLECTION_TOKEN="1f9a4e02857f48a6";

    CraftAROnDeviceIR mCraftAROnDeviceIR;
    CraftAROnDeviceCollectionManager mCollectionManager;

    /**
     *
     * Methods of creating data for AR Service
     */
    @Override
    public void collectionAdded(CraftAROnDeviceCollection collection) {
        Log.e(TAG, "Collection "+ collection.getName()+ " added!");
        loadCollection(collection);
    }

    private void loadCollection(CraftAROnDeviceCollection collection){
        mCraftAROnDeviceIR.setCollection(collection, this);
    }

    @Override
    public void addCollectionFailed(CraftARError error) {
        Log.e(TAG, "AddCollectionFailed(" + error.getErrorCode() + "):" + error.getErrorMessage());
        Toast.makeText(getContext(), "Error adding collection", Toast.LENGTH_SHORT).show();
        switch(error.getErrorCode()){
            case COLLECTION_BUNDLE_SDK_VERSION_IS_OLD:
                //You are trying to add a bundle which version is newer than the SDK version.
                //You should either update the SDK, or download and add a bundle compatible with this SDK version.
                break;
            case COLLECTION_BUNDLE_VERSION_IS_OLD:
                //You are trying to add a bundle which is outdated, since the SDK version is newer than the bundleSDK
                //You should download a bundle compatible with the newer SDK version.
                break;
            default:
                break;
        }
    }

    @Override
    public void addCollectionProgress(float progress) {
        Log.d(TAG, "AddCollectionProgress:" + progress);
    }

    @Override
    public void syncSuccessful(CraftAROnDeviceCollection collection) {
        Log.d(TAG, "Sync succesful for collection "+ collection.getName());
        loadCollection(collection);
    }

    @Override
    public void syncFinishedWithErrors(CraftAROnDeviceCollection craftAROnDeviceCollection, int itemDownloads, int itemErrors) {
        String text = "Sync Finished but  " + itemErrors + " of the " + itemDownloads + " items could not be synchronized";
        Toast.makeText(getContext(), text , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void syncProgress(CraftAROnDeviceCollection collection, float progress) {
        Log.e(TAG, "Sync progress for collection "+ collection.getName() + ":"+ progress);
    }

    @Override
    public void syncFailed(CraftAROnDeviceCollection collection, CraftARError error) {
        String text = "Sync failed for collection "+ collection.getName();
        Toast.makeText(getContext(), text , Toast.LENGTH_SHORT).show();
        Log.e(TAG, text + ":" + error.getErrorMessage());
        loadCollection(collection);
    }

    @Override
    public void setCollectionProgress(double progress) {
        //The images from the collection are loading into memory. You will have to load the collections into memory every time you open the app.
        Log.d(TAG, "SetCollectionProgress:" + progress);
    }

    @Override
    public void collectionReady(List<CraftARError> list) {
        Log.d(TAG, "Collection ready!");
        logo.setAnimation(zoomIn);
        logo.setAnimation(zoomOut);
        logo.startAnimation(zoomIn);
    }

    @Override
    public void setCollectionFailed(CraftARError error) {
        Toast.makeText(getContext(), "setCollection failed! (" + error.getErrorCode()+"):"+ error.getErrorMessage(), Toast.LENGTH_SHORT).show();
        //Error loading the collection into memory. No recognition can be performed unless a collection has been set.
        Log.e("Launching Activity", "SetCollectionFailed (" + error.getErrorCode() + "):" + error.getErrorMessage());
        Toast.makeText(getContext(), "Error loading", Toast.LENGTH_SHORT).show();
    }

    /**
     * Set up the Views and other methods
     */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (LaunchingListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CraftARSDK.Instance().init(getActivity().getApplicationContext());
        //Initialize the Collection Manager
        mCollectionManager = CraftAROnDeviceCollectionManager.Instance();

        //Initialize the Offline IR Module
        mCraftAROnDeviceIR = CraftAROnDeviceIR.Instance();

        //Obtain the collection with token.
        //This will lookup for the collection in the internal storage, and return the collection if it's available.
        CraftAROnDeviceCollection col =  mCollectionManager.get(COLLECTION_TOKEN);

        if(col == null){
            //Collection is not available. Add it from the CraftAR using collection bundle.
            mCollectionManager.addCollection("stickers.zip", this);
        }else{
            //Collection is already available in the device.
            col.sync(this);
        }

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_launching, container, false);

        //hide action bar of launching scene
        if (( actionBar != null)) {
            actionBar.hide();
        }

        logo = v.findViewById(R.id.logo);
        initAnimation();
        //scale up logo to 50% of screen width
        scaleLogo();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public Launching() {}

    private void scaleLogo() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screen_width = dm.widthPixels;
        logo.getLayoutParams().width = (int) (0.5 * screen_width); //scale up logo to 50% width
        Log.v("Welcome", String.valueOf(logo.getLayoutParams().width));
        logo.requestLayout();
    }

    private void initAnimation() {
        zoomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in);
        zoomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_out);

        zoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                logo.startAnimation(zoomOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        zoomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                logo.setVisibility(View.GONE);
                mListener.onFinishFetchData();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public interface LaunchingListener {
        void onFinishFetchData();
    }
}