package com.example.noteme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.noteme.model.Task;
import com.example.noteme.repository.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaskCheckWorker extends Worker {

    private static final String CHANNEL_ID = "task_channel";
    private static final int NOTIFICATION_ID = 1;

    public TaskCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        TaskRepository taskRepository = new TaskRepository(getApplicationContext());
        taskRepository.open();

        List<Task> tasks = taskRepository.getAllTasks();
        for (Task task : tasks) {
            long remainingDays = calculateRemainingDays(task.getDueDate());

            if (remainingDays == 1) {
                showNotification(task.getTitle());
            }
        }

        taskRepository.close();
        return Result.success();
    }

    private long calculateRemainingDays(String dueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar today = Calendar.getInstance();

        try {
            Calendar due = Calendar.getInstance();
            due.setTime(sdf.parse(dueDate));

            long diffInMillis = due.getTimeInMillis() - today.getTimeInMillis();
            return TimeUnit.MILLISECONDS.toDays(diffInMillis);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void showNotification(String taskTitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText("Task \"" + taskTitle + "\" is due tomorrow!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notification";
            String description = "Channel for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
