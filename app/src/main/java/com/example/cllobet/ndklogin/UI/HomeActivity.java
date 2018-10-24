package com.example.cllobet.ndklogin.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.cllobet.ndklogin.DB.UserFunctions;
import com.example.cllobet.ndklogin.R;

import org.w3c.dom.Text;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class HomeActivity extends AppCompatActivity {
    private UserFunctions uf;
    private TextView un;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        uf = new UserFunctions();
        un = (TextView) findViewById(R.id.sample_text);

        un.setText("Hello " + uf.getUsername(getApplicationContext()) + "!");
    }
    @Override
    public void onBackPressed() {
        uf.logoutUser(getApplicationContext());
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}
