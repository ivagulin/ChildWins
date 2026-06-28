package ru.vagulin.childwins;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.Listener {
    public static final String URL =
            "https://ivagulin.dedyn.io/pgrest/tasks";

    private final List<Task> tasks = new ArrayList<>();
    private TaskAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = findViewById(R.id.tasksRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(tasks, this, this);
        rv.setAdapter(adapter);

        if (RoleSelectActivity.isParent(this)) {
            var fabAdd = findViewById(R.id.fabAdd);
            fabAdd.setVisibility(VISIBLE);
            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
                }
            });
        }

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> loadTasks());
        swipeRefresh.setRefreshing(true);

        loadTasks();
    }

    private void loadTasks() {
        new Thread(() -> {
            try {
                URL url = new URL(URL + "?order=id.desc");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }

                JSONArray arr = new JSONArray(sb.toString());

                List<Task> result = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    Task t = new Task();
                    t.id = o.getInt("id");
                    t.name = o.getString("name");
                    t.reward = o.optString("reward", null);
                    t.steps = o.getInt("steps");
                    t.curstep = o.getInt("curstep");
                    result.add(t);
                }

                runOnUiThread(() -> {
                    tasks.clear();
                    tasks.addAll(result);
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                swipeRefresh.setRefreshing(false);
            }
        }).start();
    }

    @Override
    public void onIncrement(Task task) {
        if (task.curstep >= task.steps) return;

        int newStep = task.curstep + 1;
        swipeRefresh.setRefreshing(true);

        new Thread(() -> {
            try {
                URL url = new URL(URL + "?id=eq." + task.id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("curstep", newStep);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                if (conn.getResponseCode() == 204) {
                    runOnUiThread(() -> {
                        loadTasks();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                swipeRefresh.setRefreshing(false);
            }
        }).start();
    }

    @Override
    public void onMenu(View anchor, Task task) {
        if (!RoleSelectActivity.isParent(this)) return;

        PopupMenu menu = new PopupMenu(this, anchor);
        menu.inflate(R.menu.task_menu);

        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_edit) {
                editTask(task);
                return true;
            }

            if (id == R.id.action_delete) {
                onDelete(task);
                return true;
            }

            return false;
        });

        menu.show();
    }

    private void editTask(Task task) {
        Intent i = new Intent(this, AddTaskActivity.class);
        i.putExtra("id", task.id);
        i.putExtra("name", task.name);
        i.putExtra("reward", task.reward);
        i.putExtra("curstep", task.curstep);
        i.putExtra("steps", task.steps);
        startActivity(i);
    }

    private void onDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete task?")
                .setMessage(task.name)
                .setPositiveButton("Delete", (d, w) ->
                        deleteTask(task))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask(Task task) {
        swipeRefresh.setRefreshing(true);
        new Thread(() -> {
            try {
                URL url = new URL(URL + "?id=eq." + task.id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                if (conn.getResponseCode() == 204) {
                    runOnUiThread(() -> {
                        loadTasks();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefresh.setRefreshing(true);
        loadTasks();
    }
}