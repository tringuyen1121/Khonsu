package com.example.a.khonsu;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MapNavActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        TextView test = (TextView)findViewById(R.id.test);

        String text = getIntent().getExtras().getString("UUID");
        test.setText(text);
    }
}
