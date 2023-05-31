package com.attacker.app.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.attacker.app.BuildConfig;
import com.attacker.app.MainActivity;

public class BootupReceiver extends BroadcastReceiver {
    final String TAG = "Boot up Receiver";

    /**
     * Initializes Secure App on device reboot (Does not work on emulators)
     * @param context Android Launcher's context
     * @param intent Unused parameter
     */
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        if (BuildConfig.START_APP_ON_BOOT){
            Log.d(TAG, "Boot up has completed, starting Attacker App.");

            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}