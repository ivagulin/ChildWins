package ru.vagulin.childwins;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    public static final String USER_ROLE = "user_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences("app_prefs", MODE_PRIVATE);
        String role = prefs.getString(USER_ROLE, null);

        if (role == null) {
            // First login → configure
            startActivity(new Intent(this, RoleSelectActivity.class));
        } else {
            // Already configured
            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }
}