package com.example.cllobet.ndklogin.Domain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cllobet.ndklogin.DB.UserFunctions;
import com.example.cllobet.ndklogin.UI.HomeActivity;
import com.example.cllobet.ndklogin.UI.LoginActivity;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import io.michaelrocks.paranoid.Obfuscate;

import static com.tozny.crypto.android.AesCbcWithIntegrity.generateSalt;
import static com.tozny.crypto.android.AesCbcWithIntegrity.saltString;

@Obfuscate
public class MainActivity extends AppCompatActivity {

    private UserFunctions userFunctions;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isDeviceRooted() || isDeviceRootedNative()) {
            Toast.makeText(getApplicationContext(), "Rooted Device",
                    Toast.LENGTH_LONG).show();
            //finishAndRemoveTask(); //aixo si volem que pari la app
        }
        if (isDebugging()|| isDebuggingNative()) {
            Toast.makeText(getApplicationContext(), "Debugging Detected",
                    Toast.LENGTH_LONG).show();
            //finishAndRemoveTask(); //aixo si volem que pari la app
        }
        userFunctions = new UserFunctions();
        initSaltandUser();
        if (userFunctions.isUserLoggedIn(getApplicationContext())) {
            Intent menu = new Intent(getApplicationContext(), HomeActivity.class);
            menu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(menu);
            // Closing menu
            finish();
        } else {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

        }
    }

    public native boolean isDeviceRootedNative();

    public native boolean isDebuggingNative();

    public boolean isDebugging(){
        int PID = android.os.Process.myPid();
        try {
            Process su = Runtime.getRuntime().exec("gdbserver :3456 --attach " + String.valueOf(PID));
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.flush();
            su.waitFor();
            su.destroy();
            Log.e("Debugging: ", "executing gdbserver worked");
            return false;
        } catch (IOException e) {
            //Exception thrown, executing su didn't work
        } catch (InterruptedException e) {
            //Exception thrown, executing su didn't work
        }
        return true;
    }

    public boolean isDeviceRooted() {
        //Checks if the device uses a Custom or Stock ROM. It could be possible that it had a Stock ROM but still having been rooted, though
        // cat /system/build.prop | grep ro.build.tags
        // ro.build.tags == release-keys?

        //check pm list packages for any "chainfire/magisk" one. They are the most notable being SuperSU
//        try{
//            Process su = Runtime.getRuntime().exec("pm list packages");
//            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
//
//            outputStream.flush();
//            su.waitFor();
//            Log.e("RootDetection: ", "executing pm list packages worked. TODO: Check the result");
//            // if (result == eu.chainfire.supersu/com.topjohnwu.magisk) return true;
//        }catch(IOException e){
//            //Exception thrown, executing su didn't work
//        }catch(InterruptedException e){
//            //Exception thrown, executing su didn't work
//        }

        //List activities within com.android.settings. If cyanogenmod.superuser is installed, it is rooted

        //Check if following binaries exist:
//        /system/su
//        /system/bin/su
//        /sbin/su
//        /system/xbin/su
//        /system/xbin/mu
//        /system/bin/.ext/.su
//        /system/usr/we-need-root/su-backup

        String filename = "su";
        String path = "/system";
        File f = new File(path, filename);
        if (f.exists()) {
            Log.e("RootDetection: ", "Found " + path + "/" + filename + " binary");
            return true;
        }
        path = "/system/bin";
        f = new File(path, filename);
        if (f.exists()) {
            Log.e("RootDetection: ", "Found " + path + "/" + filename + " binary");
            return true;
        }
        path = "/sbin";
        f = new File(path, filename);
        if (f.exists()) {
            Log.e("RootDetection: ", "Found " + path + "/" + filename + " binary");
            return true;
        }
        path = "/system/xbin";
        f = new File(path, filename);
        if (f.exists()) {
            Log.e("RootDetection: ", "Found " + path + "/" + filename + " binary");
            return true;
        }
        filename = "mu";
        f = new File(path, filename);
        if (f.exists()) {
            Log.e("RootDetection: ", "Found " + path + "/" + filename + " binary");
            return true;
        }
        path = "/system/bin/.ext";
        filename = ".su";
        f = new File(path, filename);
        if (f.exists()) {
            Log.e("RootDetection: ", "Found " + path + "/" + filename + " binary");
            return true;
        }
        path = "/system/usr/we-need-root/su-backup";
        filename = ".su";
        f = new File(path, filename);
        if (f.exists()) {
            Log.e("RootDetection: ", "Found " + path + "/" + filename + " binary");
            return true;
        }

        //Check if we can execute "su" or "busybox df" (this lastone can be left on Moto E or Oneplus devices by the stock rom)
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.flush();
            su.waitFor();
            su.destroy();
            Log.e("RootDetection: ", "executing su worked");
            return true;
        } catch (IOException e) {
            //Exception thrown, executing su didn't work
        } catch (InterruptedException e) {
            //Exception thrown, executing su didn't work
        }
        return false;
    }

    public native boolean initPasscode(String user, String key);

    public void initSaltandUser() {
        String user = "e0aaedf7bccb1c80f5730cda9cf159e5";
        UserFunctions userFunction = new UserFunctions();
        try {
            String salt = userFunction.getSalt(getApplicationContext(),user);
            if (salt == "null") {
                salt = saltString(generateSalt());
            }
            AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword("F0rPr4ct1c3Purp0s30nly", salt);
            Log.e("[Register] Salt:", salt.toString());
            Log.e("[Register] Key:", key.toString());
            userFunction.saveSalt(getApplicationContext(), user, salt.toString());

            initPasscode(user,key.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
