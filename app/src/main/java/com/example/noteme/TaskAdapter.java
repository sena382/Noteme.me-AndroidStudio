package com.example.noteme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.noteme.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> originalTasks;
    private List<Task> filteredTasks;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public TaskAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.originalTasks = tasks;
        this.filteredTasks = new ArrayList<>(tasks);
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = filteredTasks.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.dueDateTextView.setText(formatDate(task.getDueDate()));

        long remainingDays = calculateRemainingDays(task.getDueDate());
        if (remainingDays <= 0) {
            holder.durationTextView.setText("Overdue");
            holder.durationTextView.setBackgroundResource(R.drawable.capsule_background_red);
        } else {
            holder.durationTextView.setText(remainingDays + " days remaining");
            holder.durationTextView.setBackgroundResource(R.drawable.capsule_background_green);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredTasks.size();
    }

    public Task getTaskAt(int position) {
        return filteredTasks.get(position);
    }

    public void removeTaskAt(int position) {
        filteredTasks.remove(position);
        notifyItemRemoved(position);
    }

    public void filter(String query) {
        filteredTasks.clear();
        if (query.isEmpty()) {
            filteredTasks.addAll(originalTasks);
        } else {
            for (Task task : originalTasks) {
                if (task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredTasks.add(task);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dueDateTextView, durationTextView;

        public TaskViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dueDateTextView = itemView.findViewById(R.id.dueDateTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
        }
    }

    private String formatDate(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        try {
            return outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    private long calculateRemainingDays(String dueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  // Gunakan format sesuai dengan yang ada di database
        Calendar today = Calendar.getInstance();

        try {
            Calendar due = Calendar.getInstance();
            due.setTime(sdf.parse(dueDate));  // Parsing tanggal dalam format "dd/MM/yyyy"

            // Hitung selisih antara dueDate dan hari ini
            long diffInMillis = due.getTimeInMillis() - today.getTimeInMillis();

            // Jika tanggal sudah lewat, hasilnya negatif
            return TimeUnit.MILLISECONDS.toDays(diffInMillis);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
