package com.example.cllobet.ndklogin.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cllobet.ndklogin.DB.UserFunctions;
import com.example.cllobet.ndklogin.R;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import static com.tozny.crypto.android.AesCbcWithIntegrity.encrypt;
import static com.tozny.crypto.android.AesCbcWithIntegrity.generateSalt;
import static com.tozny.crypto.android.AesCbcWithIntegrity.saltString;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private Button btnRegister;
    private TextView textError;
    private EditText inputUser;
    private EditText inputPassword;

    private String user;
    private String password;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUser = (EditText) findViewById(R.id.loginUser);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        textError = (TextView) findViewById(R.id.login_error);

        setTitle("Sign In");
        textError.setText("");

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                user = inputUser.getText().toString();
                password = inputPassword.getText().toString();

                UserFunctions userFunction = new UserFunctions();
                try {
                    String salt = userFunction.getSalt(getApplicationContext(), user);
                    Log.e("[Login] Salt:", salt.toString());
                    AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword("F0rPr4ct1c3Purp0s30nly", salt);
                    Log.e("[Login] Key:", key.toString());

//                        AesCbcWithIntegrity.CipherTextIvMac civ = new AesCbcWithIntegrity.CipherTextIvMac(civString);
//                        String decPass = AesCbcWithIntegrity.decryptString(civ, key);
//                        Log.d("[Login] DecryptedPass:", decPass.toString());
                    if (signInDB(user, password, key.toString()) == 0){ //if (comparePass(decPass, password)) { //decPass.equals(password)
                        textError.setText("");
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("userName", user);
                        editor.commit();
                        // Launch Dashboard Screen
                        Intent dashboard = new Intent(getApplicationContext(), HomeActivity.class);

                        // Close all views before launching Dashboard
                        dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(dashboard);

                        // Close Login Screen
                        finish();
                    } else {
                        textError.setText("Login incorrecto");
                    }

                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                user = inputUser.getText().toString();
                password = inputPassword.getText().toString();
                UserFunctions userFunction = new UserFunctions();
                boolean res = false;
                if (user.equals("") || password.equals("")) {
                    textError.setText("Por favor rellena todos los campos");
                } else {
                    try {
                        String salt = saltString(generateSalt());
                        AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword("F0rPr4ct1c3Purp0s30nly", salt);
                        Log.e("[Register] Salt:", salt.toString());
                        Log.e("[Register] Key:", key.toString());
                        //AesCbcWithIntegrity.CipherTextIvMac civ = encrypt(password, key);
                        //Log.e("[Register] CipheredPass:", civ.toString());
                        //res = userFunction.registerUser(getApplicationContext(), user, civ.toString());
                        if (signInDB(user, password, key.toString()) == 0 || signInDB(user,password,key.toString()) == 3) {
                            textError.setText("Este usuario ya existe");
                        } else {
                            res = addUserDB(user, password, key.toString());
                            if (res) {
                                userFunction.saveSalt(getApplicationContext(), user, salt.toString());
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("userName", user);
                                editor.commit();
                            } else textError.setText("Error al registrar");
                        }
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }

                    if (res) {
                        // Launch Dashboard Screen
                        Intent dashboard = new Intent(getApplicationContext(), HomeActivity.class);

                        // Close all views before launching Dashboard
                        dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(dashboard);

                        // Close Login Screen
                        finish();
                    }
                }
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native boolean comparePass(String password1, String password2);

    public native boolean addUserDB(String userName, String password, String key);

    public native int signInDB(String user, String password, String key);

    @Override
    public void onBackPressed() {
        finish();
    }
}