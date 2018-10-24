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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
                user = md5(user);
                Log.e("MD5 is: ", user);
                String aux = inputPassword.getText().toString();
                password = xor(aux);

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

    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native boolean comparePass(String password1, String password2);

    public native String xor(String s);

    public native int signInDB(String user, String password, String key);

    @Override
    public void onBackPressed() {
        finish();
    }
}