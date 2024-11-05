package com.example.noteme.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.noteme.helper.DatabaseHelper;
import com.example.noteme.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public TaskRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addTask(String title, String description, String dueDate, String extra) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, title);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_DUE_DATE, dueDate);
        values.put(DatabaseHelper.COLUMN_EXTRA, extra);

        database.insert(DatabaseHelper.TABLE_TASKS, null, values);
    }

    public void updateTask(int taskId, String title, String description, String dueDate, String extra) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, title);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_DUE_DATE, dueDate);
        values.put(DatabaseHelper.COLUMN_EXTRA, extra);

        database.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public Task getTaskById(int taskId) {
        Task task = null;
        Cursor cursor = database.query(DatabaseHelper.TABLE_TASKS,
                new String[]{DatabaseHelper.COLUMN_TASK_ID, DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_DESCRIPTION, DatabaseHelper.COLUMN_DUE_DATE, DatabaseHelper.COLUMN_EXTRA},
                DatabaseHelper.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int taskIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TASK_ID);
            int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
            int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION);
            int dueDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DUE_DATE);
            int extraIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXTRA);

            if (taskIdIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 && extraIndex != -1) {
                int id = cursor.getInt(taskIdIndex);
                String title = cursor.getString(titleIndex);
                String description = cursor.getString(descriptionIndex);
                String dueDate = cursor.getString(dueDateIndex);
                String extra = cursor.getString(extraIndex);

                task = new Task(id, title, description, dueDate, extra);
            }
            cursor.close();
        }

        return task;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_TASKS,
                new String[]{DatabaseHelper.COLUMN_TASK_ID, DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_DESCRIPTION, DatabaseHelper.COLUMN_DUE_DATE, DatabaseHelper.COLUMN_EXTRA},
                null, null, null, null, DatabaseHelper.COLUMN_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            int taskIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TASK_ID);
            int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
            int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION);
            int dueDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DUE_DATE);
            int extraIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EXTRA);

            if (taskIdIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 && extraIndex != -1) {
                do {
                    int taskId = cursor.getInt(taskIdIndex);
                    String title = cursor.getString(titleIndex);
                    String description = cursor.getString(descriptionIndex);
                    String dueDate = cursor.getString(dueDateIndex);
                    String extra = cursor.getString(extraIndex);

                    Task task = new Task(taskId, title, description, dueDate, extra);
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return tasks;
    }

    public void deleteTask(int taskId) {
        database.delete(DatabaseHelper.TABLE_TASKS, DatabaseHelper.COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }
}
