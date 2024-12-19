package com.attacker.app.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class CodeToLoad {

    public static final String TAG = "CodeToLoad";
    private static final String Placeholder = "This is a secret.";

    public CodeToLoad(){
        Log.d(TAG, "Instance created.");
    }

    // Code to inject to Insecure Target App via Create Package Context exploit (Uncomment to use)
//    public void initialize(Context appContext){
//
//        // Verify that injection is successful
//        Log.d(TAG, "Injection successful");
//        Toast.makeText(appContext, "Injection from Attacker App is successful (Public Method)", Toast.LENGTH_LONG).show();
//
//        //Add additional logic as needed
//    }

    // Private version of initialize (Uncomment to use)
//    private void initialize(Context appContext){
//
//        // Verify that injection is successful
//        Log.d(TAG, "Injection successful");
//        Toast.makeText(appContext, "Injection from Attacker App is successful (Private Method)", Toast.LENGTH_LONG).show();
//    }

    // Static version of initialize (Uncomment to use)
    public static void initialize(Context appContext){

        // Verify that injection is successful
        Log.d(TAG, "Injection successful");
        Toast.makeText(appContext, "Injection from Attacker App is successful (Static Method)", Toast.LENGTH_LONG).show();
    }

}
