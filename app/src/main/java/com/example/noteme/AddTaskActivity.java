package com.example.noteme;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.noteme.model.Task;
import com.example.noteme.repository.TaskRepository;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, dueDateEditText, extraEditText;
    private Button saveButton;
    private TaskRepository taskRepository;
    private Calendar calendar;
    private int taskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskRepository = new TaskRepository(this);
        taskRepository.open();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dueDateEditText = findViewById(R.id.dueDateEditText);
        extraEditText = findViewById(R.id.extraEditText);
        saveButton = findViewById(R.id.saveButton);

        calendar = Calendar.getInstance();

        Intent intent = getIntent();
        if (intent.hasExtra("TASK_ID")) {
            taskId = intent.getIntExtra("TASK_ID", -1);
            loadTaskData(taskId);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Task");
            }
            saveButton.setText("Update");
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Task");
            }
            saveButton.setText("Save");
        }

        dueDateEditText.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddTaskActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedMonth = selectedMonth + 1;
                        String selectedDate = selectedDay + "/" + selectedMonth + "/" + selectedYear;
                        dueDateEditText.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String dueDate = dueDateEditText.getText().toString().trim();
            String extra = extraEditText.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty()) {
                showSnackbar(v, "Semua field wajib diisi");
                return;
            }

            if (taskId == -1) {
                taskRepository.addTask(title, description, dueDate, extra);
                showSnackbar(v, "Task berhasil ditambahkan");
            } else {
                taskRepository.updateTask(taskId, title, description, dueDate, extra);
                showSnackbar(v, "Task berhasil diperbarui");
            }

            finish();
        });
    }

    private void loadTaskData(int taskId) {
        Task task = taskRepository.getTaskById(taskId);
        if (task != null) {
            titleEditText.setText(task.getTitle());
            descriptionEditText.setText(task.getDescription());
            dueDateEditText.setText(task.getDueDate());
            extraEditText.setText(task.getExtra());
        }
    }

    private void showSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark));
        snackbar.setTextColor(getResources().getColor(android.R.color.white));
        snackbar.setDuration(3000);

        View snackbarView = snackbar.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackbarView.setLayoutParams(params);

        snackbar.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskRepository.close();
    }
}
