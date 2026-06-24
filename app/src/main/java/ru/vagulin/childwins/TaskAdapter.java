package ru.vagulin.childwins;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final Listener listener;
    private final Context context;

    public interface Listener {
        void onIncrement(Task task);
        void onDelete(Task task);
    }

    public TaskAdapter(List<Task> tasks, Listener listener, Context context) {
        this.tasks = tasks;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder h, int pos) {
        Task t = tasks.get(pos);

        h.name.setText(t.name);
        h.reward.setText("Награда: " + (t.reward != null ? t.reward : "-"));
        h.steps.setText(t.curstep + " / " + t.steps);

        h.progress.setMax(t.steps);
        h.progress.setProgress(t.curstep);

        if (RoleSelectActivity.isParent(this.context)) {
            h.itemView.setOnClickListener(v -> listener.onIncrement(t));
            h.itemView.setOnLongClickListener(v -> {
                listener.onDelete(t);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView name, reward, steps;
        ProgressBar progress;

        TaskViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.taskName);
            reward = v.findViewById(R.id.taskReward);
            steps = v.findViewById(R.id.taskSteps);
            progress = v.findViewById(R.id.taskProgress);
        }
    }
}