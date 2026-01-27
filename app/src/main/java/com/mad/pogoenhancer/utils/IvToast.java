package com.mad.pogoenhancer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;

public class IvToast {

    public static void showToast (Context context, String toasttext, int position, int xoffset, int yoffset,
                           int toastimage, int secondtoastimage) {
        SharedPreferences preferenceManager = PreferenceManager.
                getDefaultSharedPreferences(context);

        String duration = preferenceManager.getString(
                Constants.SHAREDPERFERENCES_KEYS.IV_TOAST_DURATION,
                Constants.DEFAULT_VALUES.IV_TOAST_DURATION
        );

        int durationMillis = 6000;
        if (duration.equals("4s")) {
            durationMillis = 4000;
        } else if (duration.equals("2s")) {
            durationMillis = 2000;
        }

        new CountDownTimer(durationMillis - 2000, 2000)
        {
            public void onTick(long millisUntilFinished) {
                Toast toast = buildToast(context, toasttext, position, xoffset, yoffset, toastimage, secondtoastimage);
                toast.show();
            }
            public void onFinish() {
                Toast toast = buildToast(context, toasttext, position, xoffset, yoffset, toastimage, secondtoastimage);
                toast.show();
            }

        }.start();
    }

    private static Toast buildToast(Context context, String toasttext, int position, int xoffset, int yoffset,
                                   int toastimage, int secondtoastimage) {
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.iv_toast, null);
        TextView textView = (TextView) view.findViewById(R.id.custom_toast_message);
        ImageView imageView = (ImageView)  view.findViewById(R.id.toast_image);
        ViewGroup.LayoutParams layoutParams_Image1 = imageView.getLayoutParams();

        ImageView imageView2 = (ImageView)  view.findViewById(R.id.toast_second_image);
        ViewGroup.LayoutParams layoutParams_Image2 = imageView2.getLayoutParams();

        imageView.setImageDrawable(null);
        layoutParams_Image1.width = 10;

        imageView2.setImageDrawable(null);
        layoutParams_Image2.width = 10;

        if (toastimage > 0) {
            imageView.setImageResource(toastimage);
            layoutParams_Image1.width = 60;
        }

        if (secondtoastimage > 0) {
            imageView2.setImageResource(secondtoastimage);
            layoutParams_Image2.width = 60;
        }

        textView.setText(toasttext);
        toast.setView(view);
        toast.setGravity(position, xoffset, yoffset);
        toast.setDuration(Toast.LENGTH_SHORT);

        return toast;
    }
}
