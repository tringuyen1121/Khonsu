package com.example.a.khonsu.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.a.khonsu.R;

public class HomeFragment extends Fragment {

    Button cameraBtn;
    Button locationBtn;
    EditText locationEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setTitle(R.string.home);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        setHasOptionsMenu(true);

        cameraBtn = v.findViewById(R.id.camera_button);
        setUpCameraBtn();
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraBtn.setEnabled(false); //prevent users accidentally hit the button twice
                ((MainActivity)getActivity()).requestCameraPermission();
            }
        });

        locationEditText = v.findViewById(R.id.location_editText);
        locationEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        locationBtn = v.findViewById(R.id.location_submit_btn);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationEditText.getText() != null) {
                    String searchTerm = locationEditText.getText().toString().trim().toLowerCase();
                    Intent intent = new Intent(getActivity(), MapNavActivity.class);
                    intent.putExtra("UUID", searchTerm);
                    startActivity(intent);
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraBtn.setEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    private void setUpCameraBtn() {
        //scale the button
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int button_size = (int)(dm.widthPixels * 0.3);
        cameraBtn.getLayoutParams().width = button_size;
        cameraBtn.getLayoutParams().height = button_size;
        cameraBtn.requestLayout();
    }
}
