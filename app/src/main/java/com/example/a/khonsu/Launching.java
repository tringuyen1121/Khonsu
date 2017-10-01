package com.example.a.khonsu;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class Launching extends Fragment {

    //ratio of scaling logo
    private double SCALE_RATIO = 0.5;

    private ImageView logo;
    private Animation zoomIn, zoomOut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_launching, container, false);

        //hide action bar of launching scene
        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.i("Tag", "Runnable running!");
                logo.setAnimation(zoomIn);
                logo.setAnimation(zoomOut);
                logo.startAnimation(zoomIn);
            }
        }, 2000);
    }

    public Launching() {}

    private void scaleLogo() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screen_width = dm.widthPixels;
        logo.getLayoutParams().width = (int) (SCALE_RATIO * screen_width);
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
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
}