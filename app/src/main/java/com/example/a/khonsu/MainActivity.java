package com.example.a.khonsu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Launching launchingFragment = new Launching();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, launchingFragment).commit();
    }

}
