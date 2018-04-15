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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawActivity extends Activity implements RadioGroup.OnCheckedChangeListener{
    private final static String TAG_DRAW_ACT = "TAG_DRAW_ACT";
    private final static int REQUEST_PHOTO = 100;

    String filename = "LOOK_HERE";
    String fileContents = "image.png";
    File dir;
    File file;
    FileOutputStream fos;

    Bitmap bitmap;
    Bitmap alteredBitmap;

    PictDraw pictDraw;

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
            }
        });


        final AlertDialog alert = setupColorDialog();
        findViewById(R.id.button_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
            }
        });

        setupThicknessEditText();
        RadioGroup radioGroup = findViewById(R.id.radioGroup_tools);
        radioGroup.setOnCheckedChangeListener(this);
    }

    private void setupThicknessEditText(){
        final EditText thicknessET = findViewById(R.id.editText_thickness);

        thicknessET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals("")){
                    try {
                        pictDraw.setStrokeThickness(Integer.parseInt(editable.toString()));
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }
        });
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
                pictDraw.setColor(color);
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


    private void openImage(){
        // TODO: implement
        Intent choosePictureIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, REQUEST_PHOTO);
    }

    private void saveImage(){
        // TODO: implement
        Bitmap bitmapFromView = pictDraw.getDrawingCache();
//        bitmapFromView.compress(Bitmap.CompressFormat.PNG, 95, )
        this.dir = getApplicationContext().getDir(filename, Context.MODE_PRIVATE);
        this.file = new File(dir, fileContents);
        try{
            this.fos = new FileOutputStream(this.file);
            bitmapFromView.compress(Bitmap.CompressFormat.PNG, 100, this.fos);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } finally {
            try {
                if ( fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
//        MediaStore.Images.Media.insertImage(getContentResolver(), bitmapFromView, "image123", "description123");

        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

    }

    @SuppressLint("SetWorldReadable")
    public void emailImage(){
        saveImage();
        if (this.file != null) {
            this.file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(this.file));
            intent.setType("image/png");
            startActivity(intent);
        }

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
