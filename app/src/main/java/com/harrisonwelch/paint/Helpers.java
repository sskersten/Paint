package com.harrisonwelch.paint;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Holds some basic helper functions that can be used throughout the program.
 * Created by Suzanne on 4/5/2018.
 */

public class Helpers {
    private static Toast toast;
    public static void makeToast(String text, Context context){
        if (toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    //do dp to px converion
    public static float dpToPx(int dpSize, Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
    }

    //do sp to px converion
    public static float spToPx(int spSize, Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, dm);
    }

    //takes in three edit texts and gives back a color based on the values in there
    public static int rgbToHex(EditText er, EditText eg, EditText eb){
        int r, g, b;

        try {
            r = Integer.parseInt(er.getText().toString());
        } catch (NumberFormatException e) {
            r = 0;
        }

        try {
            g = Integer.parseInt(eg.getText().toString());
        } catch (NumberFormatException e) {
            g = 0;
        }

        try {
            b = Integer.parseInt(eb.getText().toString());
        } catch (NumberFormatException e) {
            b = 0;
        }

        return Color.rgb(r,g,b);

    }
}
