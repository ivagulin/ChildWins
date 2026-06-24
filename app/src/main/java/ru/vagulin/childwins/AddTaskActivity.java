package ru.vagulin.childwins;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etName, etReward, etSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        etName = findViewById(R.id.etName);
        etReward = findViewById(R.id.etReward);
        etSteps = findViewById(R.id.etSteps);

        findViewById(R.id.btnSave).setOnClickListener(v -> addTask());
    }

    private void addTask() {
        String name = etName.getText().toString().trim();
        String reward = etReward.getText().toString().trim();
        String stepsStr = etSteps.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }

        int steps = stepsStr.isEmpty() ? 0 : Integer.parseInt(stepsStr);

        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("reward", reward.isEmpty() ? JSONObject.NULL : reward);
                json.put("steps", steps);
                json.put("curstep", 0);

                URL url = new URL(MainActivity.URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=representation");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int code = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (code == 201 || code == 200) {
                        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + code, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}