package com.example.a.khonsu;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends Fragment {

    Button cameraBtn;
    Button locationBtn;
    EditText locationEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_bar, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setUpToolbar();

        setHasOptionsMenu(true);

        cameraBtn = v.findViewById(R.id.camera_button);
        //scale the button
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int button_size = (int)(dm.widthPixels * 0.3);
        cameraBtn.getLayoutParams().width = button_size;
        cameraBtn.getLayoutParams().height = button_size;
        cameraBtn.requestLayout();

        locationEditText = v.findViewById(R.id.location_editText);
        locationEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }


    private void setUpToolbar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
       if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);
    }
}
