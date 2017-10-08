package com.example.a.khonsu.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.a.khonsu.R;


public class CustomDialog extends Dialog {

    private String title, message;

    public CustomDialog(Context context, String titleToShow, String messageToShow) {
        super(context);

        this.title = titleToShow;
        this.message = messageToShow;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_dialog);

        TextView title = findViewById(R.id.dialog_title);
        title.setText(this.title);

        TextView message = findViewById(R.id.dialog_message);
        message.setText(this.message);

        Button okBtn = findViewById(R.id.dialogButtonOK);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
