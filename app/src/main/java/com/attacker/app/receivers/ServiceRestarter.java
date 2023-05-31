package com.attacker.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.attacker.app.service.AttackerService;

public class ServiceRestarter extends BroadcastReceiver {

    final static String TAG = ServiceRestarter.class.getSimpleName();

    /**
     * Restarts Monitor Service when app is closed manually.
     * @param context Provided by broadcast receiver's onReceive() method
     * @param intent Provided by broadcast receiver's onReceive() method
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Attacker Service has restarted.");
        context.startForegroundService(new Intent(context, AttackerService.class));
    }
}