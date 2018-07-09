package com.example.cllobet.ndklogin.Domain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.cllobet.ndklogin.DB.DatabaseHandler;
import com.example.cllobet.ndklogin.DB.UserFunctions;
import com.example.cllobet.ndklogin.UI.HomeActivity;
import com.example.cllobet.ndklogin.UI.LoginActivity;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private UserFunctions userFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // mirar si el usuari accedeix

        userFunctions = new UserFunctions();
        if(userFunctions.isUserLoggedIn(getApplicationContext())){
            Intent menu = new Intent(getApplicationContext(), HomeActivity.class);
            menu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(menu);
            // Closing menu
            finish();
        }
        else {
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
