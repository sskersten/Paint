package com.harrisonwelch.paint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import java.io.FileNotFoundException;

public class DrawActivity extends Activity implements RadioGroup.OnCheckedChangeListener{
    private final static String TAG_DRAW_ACT = "TAG_DRAW_ACT";
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


        RadioGroup radioGroup = findViewById(R.id.radioGroup_tools);
        radioGroup.setOnCheckedChangeListener(this);
    }

    //update the tool code in PictDraw based on what gets checked here.
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        PictDraw pictDraw = findViewById(R.id.pict_draw);

        //figure out what tool is checked and set the appropriate value in our tool list.
        switch(checkedId){
            case R.id.radioButton_brush:
                pictDraw.setCurrentTool(PictDraw.TOOL_BRUSH);
                break;
            case R.id.radioButton_line:
                pictDraw.setCurrentTool(PictDraw.TOOL_LINE);
                break;
            case R.id.radioButton_rectangle:
                pictDraw.setCurrentTool(PictDraw.TOOL_RECTANGLE);
                break;
        }
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
        Bitmap bitmapFromView = pictDraw.getBitmap();
//        bitmapFromView.compress(Bitmap.CompressFormat.PNG, 95, )
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmapFromView, "image123", "description123");

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
                    bfo.inJustDecodeBounds = false;
                    bfo.inMutable = true;
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(dataUri), null, bfo);

                    Log.i(TAG_DRAW_ACT, "bitmap.getWidth() = " + bitmap.getWidth());
                    Log.i(TAG_DRAW_ACT, "bitmap.getHeight() = " + bitmap.getHeight());

                    alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

                    Log.i(TAG_DRAW_ACT, "alteredBitmap.getWidth() = " + alteredBitmap.getWidth());
                    Log.i(TAG_DRAW_ACT, "alteredBitmap.getHeight() = " + alteredBitmap.getHeight());

                    pictDraw.setNewImage(alteredBitmap, bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

    }


}
