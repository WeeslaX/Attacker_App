package com.attacker.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.attacker.app.BuildConfig;
import com.attacker.app.R;
import com.attacker.app.receivers.ServiceRestarter;
import com.attacker.app.utils.Utils;

public class AttackerService extends Service {

    final static String TAG = AttackerService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Attacker Service has started.");
        // Persistent Notification: For service persistence
        createPersistentChannel();

        if(BuildConfig.SANITY_CHECK_ATTACKER_SERVICE){
            Utils.showToastEveryFiveSeconds(this,"Attacker Service is still running");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceRestarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createPersistentChannel(){
        //Persistent Service
        String notificationId = getString(R.string.persistent_channel_id);
        CharSequence channelName = getString(R.string.persistent_notification_name);
        String channelDescription = getString(R.string.persistent_desc);

        NotificationChannel channel = new NotificationChannel(notificationId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setDescription(channelDescription);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Attacker Service is running.")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}
