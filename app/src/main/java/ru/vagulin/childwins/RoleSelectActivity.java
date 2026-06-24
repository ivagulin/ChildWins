package ru.vagulin.childwins;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectActivity extends AppCompatActivity {

    public static final String PREFS = "app_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        findViewById(R.id.btnParent).setOnClickListener(v ->
                selectRole("parent"));

        findViewById(R.id.btnChild).setOnClickListener(v ->
                selectRole("child"));
    }

    private void selectRole(String role) {
        SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        prefs.edit()
                .putString(SplashActivity.USER_ROLE, role)
                .apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public static boolean isParent(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        return "parent".equals(
                prefs.getString(SplashActivity.USER_ROLE, "parent")
        );
    }
}