package com.example.cualma.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cualma.R;
import com.example.cualma.database.ClassSchedule;
import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> implements Filterable {

    private Context context;
    private List<ClassSchedule> originalList; // Mantiene la lista completa original
    private List<ClassSchedule> filteredList; // Mantiene la lista que se está mostrando (filtrada)
    private OnClassClickListener listener;

    public interface OnClassClickListener {
        void onClassClick(ClassSchedule classSchedule);
        void onClassDelete(ClassSchedule classSchedule);
    }

    public ClassAdapter(Context context, OnClassClickListener listener) {
        this.context = context;
        this.originalList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.listener = listener;
    }

    public void setClasses(List<ClassSchedule> classList) {
        this.originalList = classList;
        this.filteredList = new ArrayList<>(classList); // Al inicio, la filtrada es igual a la original
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
        // Usamos filteredList en lugar de classList
        ClassSchedule classSchedule = filteredList.get(position);

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
        return filteredList.size(); // Tamaño de la lista filtrada
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                List<ClassSchedule> filtered;

                if (charString.isEmpty()) {
                    filtered = originalList;
                } else {
                    List<ClassSchedule> tempList = new ArrayList<>();
                    for (ClassSchedule row : originalList) {
                        // Lógica de búsqueda: Nombre, Código o Día
                        if (row.getClassName().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getClassCode().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getDay().toLowerCase().contains(charString.toLowerCase())) {
                            tempList.add(row);
                        }
                    }
                    filtered = tempList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<ClassSchedule>) results.values;
                notifyDataSetChanged();
            }
        };
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