package com.example.noteme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteme.model.Task;
import com.example.noteme.repository.TaskRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskRepository taskRepository;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fab;
    private SearchView searchViewTasks;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        taskRepository = new TaskRepository(this);
        taskRepository.open();

        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        searchViewTasks = findViewById(R.id.searchViewTasks);
        fab = findViewById(R.id.fab);
        emptyTextView = findViewById(R.id.emptyTextView);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        scheduleTaskWorker();
        loadTasks();

        searchViewTasks.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                taskAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                taskAdapter.filter(newText);
                return true;
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);
            }
        }).attachToRecyclerView(taskRecyclerView);
    }

    private void scheduleTaskWorker() {
        PeriodicWorkRequest taskCheckWorkRequest = new PeriodicWorkRequest.Builder(TaskCheckWorker.class, 24, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TaskCheckWorker",
                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                taskCheckWorkRequest
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        List<Task> tasks = taskRepository.getAllTasks();
        if (tasks.isEmpty()) {
            taskRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            taskRecyclerView.setVisibility(View.VISIBLE);
            taskAdapter = new TaskAdapter(this, tasks);
            taskRecyclerView.setAdapter(taskAdapter);

            taskAdapter.setOnItemClickListener(task -> {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra("TASK_ID", task.getTaskId());
                startActivity(intent);
            });
        }
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTask(position))
                .setNegativeButton("No", (dialog, which) -> taskAdapter.notifyItemChanged(position))
                .create()
                .show();
    }

    private void deleteTask(int position) {
        Task task = taskAdapter.getTaskAt(position);
        taskRepository.deleteTask(task.getTaskId());
        taskAdapter.removeTaskAt(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskRepository.close();
    }
}
