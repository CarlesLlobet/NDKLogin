package com.example.cllobet.ndklogin.DB;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserFunctions {
    public String loginUser(Context context, String user) {
        DatabaseHandler db = new DatabaseHandler(context);
        String res;
        //if ((!user.equals("admin"))||(!password.equals("4dm1n")))
        res = db.SignIn(user);
        if (!res.equals("")) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("userName", user);
            editor.commit();
        }
        //else res = true
        return res;
    }

    public boolean isUserLoggedIn(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains("userName")) return true;
        return false;
    }

    public String getUsername(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains("userName")) return preferences.getString("userName","");
        return "null";
    }

    public String getSalt(Context context, String username) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains(username)) return preferences.getString(username,"");
        return "null";
    }

    public void saveSalt(Context context, String username, String salt){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(username, salt);
        editor.commit();
    }

    public boolean registerUser(Context context, String username, String password) {
        DatabaseHandler db = new DatabaseHandler(context);
        boolean res = db.addUser(username, password);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("userName", username);
        editor.commit();
        return res;
    }

    public void logoutUser(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("userName");
        editor.commit();
    }
}
