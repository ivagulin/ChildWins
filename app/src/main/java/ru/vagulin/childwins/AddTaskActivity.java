package ru.vagulin.childwins;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etName, etReward, etSteps, etCurstep;
    private Integer taskId = null; // null = add mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        etName = findViewById(R.id.etName);
        etReward = findViewById(R.id.etReward);
        etCurstep = findViewById(R.id.etCurstep);
        etSteps = findViewById(R.id.etSteps);
        var btnSave = (Button)findViewById(R.id.btnSave);

        // ✅ Check edit mode
        if (getIntent().hasExtra("id")) {
            taskId = getIntent().getIntExtra("id", -1);

            etName.setText(getIntent().getStringExtra("name"));
            etReward.setText(getIntent().getStringExtra("reward"));
            etCurstep.setText(
                    String.valueOf(getIntent().getIntExtra("curstep", 0))
            );
            etSteps.setText(
                    String.valueOf(getIntent().getIntExtra("steps", 100))
            );

            setTitle("Edit Task");
            btnSave.setText("Save");
        } else {
            etCurstep.setText("0");
            etSteps.setText("100");
            setTitle("Add Task");
            btnSave.setText("Add");
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String name = etName.getText().toString().trim();
        String reward = etReward.getText().toString().trim();
        String curstepStr = etCurstep.getText().toString().trim();
        String stepsStr = etSteps.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }

        if (reward.isEmpty()) {
            etReward.setError("Required");
            return;
        }

        int curstep = curstepStr.isEmpty() ? 0 : Integer.parseInt(curstepStr);
        int steps = stepsStr.isEmpty() ? 100 : Integer.parseInt(stepsStr);

        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("child", "maxim");
                json.put("name", name);
                json.put("reward", reward);
                json.put("curstep", curstep);
                json.put("steps", steps);

                boolean isEdit = taskId != null;

                URL url = isEdit
                        ? new URL(MainActivity.URL + "?id=eq." + taskId)
                        : new URL(MainActivity.URL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(isEdit ? "PATCH" : "POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);

                // Only add curstep on create
                if (!isEdit) {
                    json.put("curstep", 0);
                }

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int code = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (code == 200 || code == 201 || code == 204) {
                        Toast.makeText(
                                this,
                                isEdit ? "Task updated" : "Task added",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    } else {
                        Toast.makeText(
                                this,
                                "Error: " + code,
                                Toast.LENGTH_LONG
                        ).show();
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