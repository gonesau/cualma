package com.example.cualma.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cualma.R;
import com.example.cualma.database.ClassSchedule;
import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private Context context;
    private List<ClassSchedule> classList;
    private OnClassClickListener listener;

    public interface OnClassClickListener {
        void onClassClick(ClassSchedule classSchedule);
        void onClassDelete(ClassSchedule classSchedule);
    }

    public ClassAdapter(Context context, OnClassClickListener listener) {
        this.context = context;
        this.classList = new ArrayList<>();
        this.listener = listener;
    }

    public void setClasses(List<ClassSchedule> classList) {
        this.classList = classList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_class_card, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassSchedule classSchedule = classList.get(position);

        holder.tvClassName.setText(classSchedule.getClassName());
        holder.tvClassCode.setText(classSchedule.getClassCode());
        holder.tvDay.setText(classSchedule.getDay());
        holder.tvTime.setText(classSchedule.getStartTime() + " - " + classSchedule.getEndTime());
        holder.tvClassroom.setText("Aula: " + classSchedule.getClassroom());
        holder.tvTeacher.setText("Docente: " + classSchedule.getTeacherName());

        holder.itemView.setOnClickListener(v -> listener.onClassClick(classSchedule));

        holder.btnDelete.setOnClickListener(v -> listener.onClassDelete(classSchedule));
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvClassCode, tvDay, tvTime, tvClassroom, tvTeacher;
        ImageButton btnDelete;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvClassCode = itemView.findViewById(R.id.tvClassCode);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvClassroom = itemView.findViewById(R.id.tvClassroom);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}