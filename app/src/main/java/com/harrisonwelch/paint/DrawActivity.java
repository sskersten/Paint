package com.harrisonwelch.paint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import java.io.FileNotFoundException;

public class DrawActivity extends Activity {
    private final static int REQUEST_PHOTO = 100;

    Bitmap bitmap;
    Bitmap alteredBitmap;

    PictDraw pictDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        pictDraw = findViewById(R.id.pict_draw);

        findViewById(R.id.button_open_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open image from photos or elsewhere
                openImage();
            }
        });

        findViewById(R.id.button_save_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save the edited image to photos or elsewhere
                saveImage();
            }
        });
    }

    private void openImage(){
        // TODO: implement
        Intent choosePictureIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, REQUEST_PHOTO);
    }

    private void saveImage(){
        // TODO: implement
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if ( requestCode == REQUEST_PHOTO ){

                try {
                    Uri dataUri = data.getData();
                    BitmapFactory.Options bfo = new BitmapFactory.Options();
                    bfo.inJustDecodeBounds = true;
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(dataUri), null, bfo);

                    alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                    pictDraw.setNew
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
