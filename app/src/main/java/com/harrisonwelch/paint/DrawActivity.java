package com.harrisonwelch.paint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Stack;

import abak.tr.com.boxedverticalseekbar.BoxedVertical;


//Lee


public class DrawActivity extends Activity implements RadioGroup.OnCheckedChangeListener{
    private final static String TAG_DRAW_ACT = "TAG_DRAW_ACT";
    private final static int REQUEST_PHOTO = 100;
    private final static int REQUEST_EMAIL = 101;
    private final static String KEY_SHAPES = "KEY_SHAPES";
    private final static String KEY_SHAPE_POSITIONS = "KEY_SHAPE_POSITIONS";
    private final static String KEY_BITMAP= "KEY_BITMAP";

    private final static String KEY_PICT_DRAW = "pict";

    String file_path = "/sdcard";
    String fileContents = "image.png";
    String fileLocation = null;
    File publicFile = null;
    File dir;
    File file;
    File publicDirectory;
    FileOutputStream fos;
    FileOutputStream publicFos;

    Bitmap bitmap;
    Bitmap alteredBitmap;

    AlertDialog stickerAlert;



    PictDraw pictDraw;

    BoxedVertical brushThickness;
    LinearLayout colorIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

            pictDraw = findViewById(R.id.pict_draw);
        pictDraw.setDrawingCacheEnabled(true);



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

        findViewById(R.id.button_email_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailImage();
//                startEmailActivity();
            }
        });

        findViewById(R.id.button_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( pictDraw != null ){
                    pictDraw.undo();
                }
            }
        });


        stickerAlert = setupStickerDialog();

        final AlertDialog alert = setupColorDialog();
        findViewById(R.id.button_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
            }
        });
        findViewById(R.id.linearLayout_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
            }
        });

        findViewById(R.id.button_new_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pictDraw != null){
                    pictDraw.clear();
                }
            }
        });

//        setupThicknessEditText();
        RadioGroup radioGroup = findViewById(R.id.radioGroup_tools);
        radioGroup.setOnCheckedChangeListener(this);

        findViewById(R.id.radioButton_sticker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictDraw.setCurrentTool(PictDraw.TOOL_STICKER);
                stickerAlert.show();
            }
        });

        findViewById(R.id.radioButton_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictDraw.toggleDoDrawFrame();
            }
        });


        publicDirectory = getPublicFile();

        // setup box text view

        brushThickness = (BoxedVertical) findViewById(R.id.boxedvertical_thickness);

        brushThickness.setOnBoxedPointsChangeListener(new BoxedVertical.OnValuesChangeListener() {
            @Override
            public void onPointsChanged(BoxedVertical boxedVertical, int i) {
                Log.i(TAG_DRAW_ACT, "boxedVertical = " + boxedVertical + " i = " + i);
                pictDraw.setStrokeThickness(i);
            }

            @Override
            public void onStartTrackingTouch(BoxedVertical boxedVertical) {
                Log.i(TAG_DRAW_ACT, "boxedVertical = " + boxedVertical);
            }

            @Override
            public void onStopTrackingTouch(BoxedVertical boxedVertical) {
                Log.i(TAG_DRAW_ACT, "boxedVertical = " + boxedVertical);
            }
        });
        colorIndicator = findViewById(R.id.linearLayout_color);
        colorIndicator.setBackgroundColor(pictDraw.getColor());
        brushThickness.setValue(5);

        if (savedInstanceState != null){
            pictDraw.setShapes((Stack)savedInstanceState.getSerializable(KEY_SHAPES));

            // bitmap
            Bitmap bmp = null;
            String filename = savedInstanceState.getString(KEY_BITMAP, "");

            if (!filename.equals("")) {
                try {
                    FileInputStream fis = this.openFileInput(filename);
                    bmp = BitmapFactory.decodeStream(fis);
                    fis.close();
                    Log.i(TAG_DRAW_ACT, "bmp = " + bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pictDraw.setNewImage(bmp, bmp);
            }
        }
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        Log.i(TAG_DRAW_ACT, "onSaveInstanceState");
//
//        outState.putSerializable(KEY_SHAPES, pictDraw.getShapes());
//        outState.putSerializable(KEY_SHAPE_POSITIONS, pictDraw.getShapePositions());;
//
//        // source: https://stackoverflow.com/questions/11010386/passing-android-bitmap-data-within-activity-using-intent-in-android
//        // user: Zaid Daghestani
//        if (pictDraw.getBitmap() != null){
//            try{
//                Bitmap bmp = pictDraw.getBitmap();
//                String filename = "bitmap.png";
//                FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
//                bmp.compress(Bitmap.CompressFormat.PNG,100,stream);
//
//                stream.close();
//                bmp.recycle();
//
//                outState.putString(KEY_BITMAP, filename);
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        super.onSaveInstanceState(outState);
//    }

    /*

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_SHAPES, pictDraw.getShapes());
        outState.putSerializable(KEY_SHAPE_POSITIONS, pictDraw.getShapePositions());
        if (fileLocation != null && !fileLocation.equals("")) {
            outState.putSerializable(KEY_BITMAP, fileLocation);
        }
    }
    */

    private AlertDialog setupStickerDialog(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View stickerAlert = inflater.inflate(R.layout.sticker_alert, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(DrawActivity.this);

        builder.setView(stickerAlert);
        builder.setTitle("Choose a Sticker");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                RadioGroup r = stickerAlert.findViewById(R.id.radioGroup_sticker);

                int sticker = 1;
                switch (r.getCheckedRadioButtonId()){
                    case R.id.radioButton_star: sticker = PictDraw.STICKER_STAR;
                        break;
                    case R.id.radioButton_leaf: sticker = PictDraw.STICKER_LEAF;
                        break;
                    case R.id.radioButton_lee: sticker = PictDraw.STICKER_LEE;
                        break;
                }

                pictDraw.setSticker(sticker);
            }
        });

        AlertDialog alert = builder.create();
        return alert;
    }

//    private void setupThicknessEditText(){
//        final EditText thicknessET = findViewById(R.id.editText_thickness);
//
//        thicknessET.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if (!editable.toString().equals("")){
//                    try {
//                        pictDraw.setStrokeThickness(Integer.parseInt(editable.toString()));
//                    } catch (NumberFormatException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

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
            case R.id.radioButton_sticker:

                break;
            case R.id.radioButton_frame:

                break;
        }
    }


    private AlertDialog setupColorDialog(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View alertView = inflater.inflate(R.layout.color_alert, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(DrawActivity.this);

        final EditText r = alertView.findViewById(R.id.editText_red);
        final EditText g = alertView.findViewById(R.id.editText_green);
        final EditText b = alertView.findViewById(R.id.editText_blue);
        final View colorShow = alertView.findViewById(R.id.colorShow);

        setupEditTexts(r, g, b, colorShow, alertView);
        setupSeekBars(alertView, r, g, b, colorShow);


        builder.setView(alertView);
        builder.setTitle("Choose a color");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                verifyEditText(r);
                verifyEditText(g);
                verifyEditText(b);

                int color = Helpers.rgbToHex(r, g, b);

                // set the stroke on the drawing image
                pictDraw.setColor(color);

                // update the color indicator on screen
                findViewById(R.id.linearLayout_color).setBackgroundColor(color);
            }
        });

        AlertDialog alert = builder.create();
        return alert;
    }

    private void verifyEditText(EditText e){
        if (e.getText().toString().equals("")){
            Helpers.makeToast("Putting 0 for unfilled color slots...", getApplicationContext());
            e.setText("0");
        }
    }

    private void setupEditTexts(final EditText r, final EditText g, final EditText b, final View colorShow, View alertView){
        class TextListener implements TextWatcher {
            EditText e;
            SeekBar s;

            TextListener(EditText e, SeekBar s){
                this.e = e;
                this.s = s;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals("")) {
                    try {
                        if (Integer.parseInt(editable.toString()) > 255) {
                            Helpers.makeToast("Max value is 255.", getApplicationContext());
                            e.setText("255");
                        }
                        if (Integer.parseInt(editable.toString()) < 0) {
                            Helpers.makeToast("Min value is 0.", getApplicationContext());
                            e.setText("0");
                        }
                    } catch (NumberFormatException e2) {
                        e2.printStackTrace();
                    }

                    s.setProgress(Integer.parseInt(e.getText().toString()));
                    e.setSelection(e.getText().length());
                    updateColorShow(colorShow, r, g, b);
                }
            }
        };

        //When the user clicks off the edit text, if they left it blank, force it to 0.
        class focusChangeListener implements View.OnFocusChangeListener {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    EditText e = (EditText) view;
                    if (e.getText().toString().equals("")) {
                        e.setText("0");
                    }
                }
            }
        };

        r.addTextChangedListener(new TextListener(r, (SeekBar) alertView.findViewById(R.id.seekBar_r)));
        r.setOnFocusChangeListener(new focusChangeListener());
        g.addTextChangedListener(new TextListener(g, (SeekBar) alertView.findViewById(R.id.seekBar_g)));
        g.setOnFocusChangeListener(new focusChangeListener());
        b.addTextChangedListener(new TextListener(b, (SeekBar) alertView.findViewById(R.id.seekBar_b)));
        b.setOnFocusChangeListener(new focusChangeListener());
    }

    private void setupSeekBars(View alertView, final EditText r, final EditText g, final EditText b, final View colorShow){
        class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
            EditText e;

            public SeekBarListener(EditText e) {
                this.e = e;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b2) {
                e.setText(Integer.toString(value));
                updateColorShow(colorShow, r, g, b);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        ((SeekBar)alertView.findViewById(R.id.seekBar_r)).setOnSeekBarChangeListener(new SeekBarListener(r));
        ((SeekBar)alertView.findViewById(R.id.seekBar_g)).setOnSeekBarChangeListener(new SeekBarListener(g));
        ((SeekBar)alertView.findViewById(R.id.seekBar_b)).setOnSeekBarChangeListener(new SeekBarListener(b));
    }

    private void updateColorShow(View colorShow, EditText r, EditText g, EditText b){
        colorShow.setBackgroundColor(Helpers.rgbToHex(r, g, b));
    }


    private void openImage() {
        // TODO: implement
        Intent choosePictureIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, REQUEST_PHOTO);
    }

    private void saveImage(){
//        grantUriPermission();
        this.fileLocation = MediaStore.Images.Media.insertImage(getContentResolver(),pictDraw.getDrawingCache(),"title.png","desc123");

        Log.i(TAG_DRAW_ACT, "this.fileLocation = " + this.fileLocation);

        Toast.makeText(getApplicationContext(), "Saved to Gallery", Toast.LENGTH_SHORT).show();
    }

    private File getPublicFile(){
        String albumName = "TEST ALBUM";
        File publicFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if(!publicFile.mkdirs()){
            Log.e(TAG_DRAW_ACT,"Dir not created");
        } else {
            Log.i(TAG_DRAW_ACT,"[YES] Dir created");

        }
        return file;
    }

    @SuppressLint("SetWorldReadable")
    public void emailImage(){
        saveImagePublic();
        if (this.publicFile != null) {
            this.publicFile.setReadable(true, false);
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email Subject");
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hwelch1@my.apsu.edu"});
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello World!");
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(this.publicFile));
            startActivity(Intent.createChooser(emailIntent, "Select app to send this image."));
        }
    }

    public void startEmailActivity(){
        Intent intent = new Intent(getApplicationContext(), EmailActivity.class);
//        startActivityForResult(intent, REQUEST_EMAIL);
        startActivity(intent);
    }

    public void saveImagePublic(){
        File publicDir = getPublicAlbumStorageDir("Emailed Photos");
        String filename = "myfile.png";
        this.publicFile = new File(publicDir,filename);
        try {
            this.publicFos = new FileOutputStream(publicFile);
            Bitmap bitmapFromView = pictDraw.getDrawingCache();
            bitmapFromView.compress(Bitmap.CompressFormat.PNG, 100, this.publicFos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG_DRAW_ACT, "Directory not created");
        }
        return file;
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
            if (requestCode == REQUEST_EMAIL){
//                emailImage();
                Log.i(TAG_DRAW_ACT, "REQUEST_EMAIL");
            }
        }

    }


}
