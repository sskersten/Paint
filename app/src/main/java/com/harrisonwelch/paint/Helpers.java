package com.harrisonwelch.paint;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
}
