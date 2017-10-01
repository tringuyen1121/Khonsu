package com.example.a.khonsu;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.wikitude.NativeStartupConfiguration;
import com.wikitude.WikitudeSDK;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.InternalRendering;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.rendering.RenderSettings;
import com.wikitude.tracker.ImageTarget;
import com.wikitude.tracker.ImageTracker;
import com.wikitude.tracker.ImageTrackerListener;
import com.wikitude.tracker.TargetCollectionResource;
import com.wikitude.tracker.TargetCollectionResourceLoadingCallback;

public class ARHandler extends Activity implements ImageTrackerListener, InternalRendering {

    private final String WIKITUDE_SDK_KEY = "PdK20Dp1ABfiaizEbbeCk8RfxnI88Mz7Kz7PAurZ3o4CBizgiy5xaXVnHyY3iO2hnHCaCe4DRnOwf+sFcU4Ocl1yOx/" +
            "nSwbmLEXbjEpOwcqAQ4q893/Rd8aHzDhA7hVRLOCeNJjLsWpPgTriUAGIlJ7fvl8vlHNcmIBEWAJ4ChZTYWx0ZWRfX6ku1hrjEM89yYkHtK2fTQYYEu6zSjEu4" +
            "SklG1EI13EAHtkHRmO+7Q7mjPEft42ZcZNd6MR9+aUUTRMPJxsY6T6tMIDIJSdHaiaQIBNUta1Puyey9sEjdEuQmoTzC+X4JF20HIOQXQ0PJAy0k2VzRQiVffl" +
            "y+wnWyk370sHpSEM5ApMZvNqwYd+gBRW8y3FvCGdTTB4MyLB6HANMNi5hrgtnCLgwwR6RM3T0ZKjDUe2f9OuDlf53Yk8SuSLSQ1h+ZFdPPnJz+3/Ytp67hg/un23" +
            "LKTDoqWUpF1xXmN821xnBOjYsy6ATSHddxHD4VmDkzgq2e8qDtGnfkC5Ecv6quzx09KF1k0lCda7Uz99gOjKEnucNsDBjAEFSZ0efI+x1XbsRhelQfT/htAokV6" +
            "CP87VAdh+kA48bmmwX9QD0fSjxCXkUnwHcKeIxIH40QT/UeduMC1OpZjCfL51navA7Nw2oqQrXJwa7kLZQR40XQGQXQWudj5ztjBQ=";

    private WikitudeSDK mWikitudeSDK;
    private TargetCollectionResource mTargetCollectionResource;
    private CustomRenderExtension mRenderExtension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);
        startupConfiguration.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        mTargetCollectionResource = mWikitudeSDK.getTrackerManager().createTargetCollectionResource("file:///android_asset/magazine.wtc",
                new TargetCollectionResourceLoadingCallback() {
                    @Override
                    public void onError(int i, String s) {

                    }

                    @Override
                    public void onFinish() {
                        mWikitudeSDK.getTrackerManager().createImageTracker(mTargetCollectionResource, ARHandler.this, null);
                    }
                });

        setContentView(mWikitudeSDK.setupWikitudeGLSurfaceView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWikitudeSDK.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWikitudeSDK.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWikitudeSDK.clearCache();
        mWikitudeSDK.onDestroy();
    }

    @Override
    public RenderExtension provideRenderExtension() {
        mRenderExtension = new CustomRenderExtension();
        return mRenderExtension;
    }



    @Override
    public void onRenderingApiInstanceCreated(RenderSettings.RenderingAPI renderingAPI) {

    }

    @Override
    public void onTargetsLoaded(ImageTracker imageTracker) {

    }

    @Override
    public void onErrorLoadingTargets(ImageTracker imageTracker, int i, String s) {

    }

    @Override
    public void onImageRecognized(ImageTracker imageTracker, ImageTarget imageTarget) {
        Toast toast = Toast.makeText(this, "Name: " + imageTarget.getName() + " ID: " + imageTarget.getUniqueId(), Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onImageTracked(ImageTracker imageTracker, ImageTarget imageTarget) {
        Toast toast = Toast.makeText(this, "Name: " + imageTarget.getName() + " ID: " + imageTarget.getUniqueId(), Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onImageLost(ImageTracker imageTracker, ImageTarget imageTarget) {
        mRenderExtension.setCurrentlyRecognizedTarget(null);
    }

    @Override
    public void onExtendedTrackingQualityChanged(ImageTracker imageTracker, ImageTarget imageTarget, int i, int i1) {

    }
}
