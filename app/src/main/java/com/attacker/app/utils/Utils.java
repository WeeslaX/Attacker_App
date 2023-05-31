package com.attacker.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Utils {
    private static Handler handler;
    private static Runnable runnable;

    public static void showToastEveryFiveSeconds(Context context, final String message) {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                showToast(context, message);
                handler.postDelayed(this, 5000); // Repeat every 5 seconds
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
