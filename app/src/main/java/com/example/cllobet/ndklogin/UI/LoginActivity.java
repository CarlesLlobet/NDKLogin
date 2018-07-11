package com.example.cllobet.ndklogin.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
                    Log.d("[Login] Salt:", salt.toString());
                    AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword("F0rPr4ct1c3Purp0s30nly", salt);
                    Log.d("[Login] Key:", key.toString());
                    String civString = userFunction.loginUser(getApplicationContext(), user);
                    if(civString.equals("")){
                        textError.setText("Login incorrecto");
                    } else {
                    AesCbcWithIntegrity.CipherTextIvMac civ = new AesCbcWithIntegrity.CipherTextIvMac(civString);
                    String decPass = AesCbcWithIntegrity.decryptString(civ, key);
                    Log.d("[Login] DecryptedPass:", decPass.toString());
                    if (decPass.equals(password)) {
                        textError.setText("");
                        // Launch Dashboard Screen
                        Intent dashboard = new Intent(getApplicationContext(), HomeActivity.class);

                        // Close all views before launching Dashboard
                        dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(dashboard);

                        // Close Login Screen
                        finish();
                    } else {
                        textError.setText("Login incorrecto");
                    } }
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                user = inputUser.getText().toString();
                password = inputPassword.getText().toString();
                UserFunctions userFunction = new UserFunctions();

                try {
                    String salt = saltString(generateSalt());
                    AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword("F0rPr4ct1c3Purp0s30nly", salt);
                    Log.d("[Register] Salt:", salt.toString());
                    Log.d("[Register] Key:", key.toString());
                    AesCbcWithIntegrity.CipherTextIvMac civ = encrypt(password, key);
                    Log.d("[Register] CipheredPass:", civ.toString());
                    userFunction.registerUser(getApplicationContext(), user, civ.toString());
                    userFunction.saveSalt(getApplicationContext(), user, salt.toString());
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Launch Dashboard Screen
                Intent dashboard = new Intent(getApplicationContext(), HomeActivity.class);

                // Close all views before launching Dashboard
                dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(dashboard);

                // Close Login Screen
                finish();
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onBackPressed() {
        finish();
    }
}
