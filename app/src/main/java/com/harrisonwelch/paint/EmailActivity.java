package com.harrisonwelch.paint;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EmailActivity extends Activity {
    public final static String KEY_EMAIL = "KEY_EMAIL";
    public final static String KEY_EMAIL_SUBJECT = "KEY_EMAIL_SUBJECT";
    public final static String KEY_EMAIL_MESSAGE = "KEY_EMAIL_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                String email = ((EditText)findViewById(R.id.editText_email)).getText().toString();
                returnIntent.putExtra(KEY_EMAIL, email);
                String subject = ((EditText)findViewById(R.id.editText_subject)).getText().toString();
                returnIntent.putExtra(KEY_EMAIL_SUBJECT, subject);
                String message = ((EditText)findViewById(R.id.editText_message)).getText().toString();
                returnIntent.putExtra(KEY_EMAIL_MESSAGE, message);
                setResult(DrawActivity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}
