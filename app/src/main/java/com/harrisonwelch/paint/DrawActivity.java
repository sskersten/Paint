package com.harrisonwelch.paint;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class DrawActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        findViewById(R.id.button_open_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open image from photos or elsewhere
            }
        });
    }
}
