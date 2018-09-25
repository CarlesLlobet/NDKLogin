package com.example.cllobet.ndklogin.Domain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cllobet.ndklogin.DB.UserFunctions;
import com.example.cllobet.ndklogin.UI.HomeActivity;
import com.example.cllobet.ndklogin.UI.LoginActivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private UserFunctions userFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isDeviceRooted()){
            Toast.makeText(getApplicationContext(), "Rooted Device",
                    Toast.LENGTH_LONG).show();
                            finishAndRemoveTask();
        } else {
            userFunctions = new UserFunctions();
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
    }

    public boolean isDeviceRooted() {
        //Checks if the device uses a Custom or Stock ROM. It could be possible that it had a Stock ROM but still having been rooted, though
        // cat /system/build.prop | grep ro.build.tags
        // ro.build.tags == release-keys?

        //Checks if the OTA certs exist, if not probably a custom ROM has been installed. They could be there and the phone rooted anyway
        //ls -l /etc/security/otacerts.zip

        //Check if Superuser.apk exists

        //check pm list packages for any "chainfire/magisk" one. They are the most notable being SuperSU

        //List activities within com.android.settings. If cyanogenmod.superuser is installed, it is rooted

        //Check following binaries:
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

        //Check if we can touch a file in any of the following directories:
//        /data
//        /
//        /system
//        /system/bin
//        /system/sbin
//        /system/xbin
//        /vendor/bin
//        /sys
//        /sbin
//        /etc
//        /proc
//        /dev

        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.flush();
            su.waitFor();
            Log.e("RootDetection: ", "executing su worked");
            return true;
        }catch(IOException e){
            //Exception thrown, executing su didn't work
        }catch(InterruptedException e){
            //Exception thrown, executing su didn't work
        }

        //Check if we can read a file of /data

        //Check if we can execute "su" or "busybox df" (this lastone can be left on Moto E or Oneplus devices by the stock rom)

        return false;
    }
}
