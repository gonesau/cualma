package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cualma.adapters.ClassAdapter;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleCalendarActivity extends AppCompatActivity implements ClassAdapter.OnClassClickListener {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvSelectedDate, tvEmptyState;
    private ClassAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<ClassSchedule> allClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_calendar);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupRecyclerView();

        // Cargar todas las clases una vez para filtrar localmente
        allClasses = dbHelper.getAllClasses();

        // Configurar el listener del calendario
        setupCalendarListener();

        // Filtrar automáticamente para el día de hoy al abrir
        filterClassesByDate(Calendar.getInstance());
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerViewByDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Reutilizamos el adaptador existente
        adapter = new ClassAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupCalendarListener() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            filterClassesByDate(selectedDate);
        });
    }

    private void filterClassesByDate(Calendar date) {
        // 1. Obtener el día de la semana (Ej: Calendar.MONDAY)
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        String dayString = getDayString(dayOfWeek);

        // Actualizar título
        tvSelectedDate.setText("Clases del " + dayString);

        // 2. Filtrar la lista
        List<ClassSchedule> filteredList = new ArrayList<>();
        for (ClassSchedule item : allClasses) {
            // Comparamos ignorando mayúsculas/minúsculas
            if (item.getDay().equalsIgnoreCase(dayString)) {
                filteredList.add(item);
            }
        }

        // 3. Actualizar UI
        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.setClasses(filteredList);
        }
    }

    // Método auxiliar para traducir Calendar.DAY_OF_WEEK a tus Strings de DB
    private String getDayString(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY: return "Lunes";
            case Calendar.TUESDAY: return "Martes";
            case Calendar.WEDNESDAY: return "Miércoles";
            case Calendar.THURSDAY: return "Jueves";
            case Calendar.FRIDAY: return "Viernes";
            case Calendar.SATURDAY: return "Sábado";
            case Calendar.SUNDAY: return "Domingo";
            default: return "";
        }
    }

    // Implementación de la interfaz del adaptador (puedes decidir si permitir editar desde aquí)
    @Override
    public void onClassClick(ClassSchedule classSchedule) {
        // Opción: Abrir detalles para editar
        Intent intent = new Intent(this, AddEditClassActivity.class);
        intent.putExtra("class_id", classSchedule.getId());
        startActivity(intent);
    }

    @Override
    public void onClassDelete(ClassSchedule classSchedule) {
        // Opción: Permitir borrar desde el calendario
        dbHelper.deleteClass(classSchedule.getId());
        // Recargar datos y volver a filtrar
        allClasses = dbHelper.getAllClasses();
        // Truco: Forzar re-filtrado obteniendo la fecha actual del calendarView
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(calendarView.getDate());
        filterClassesByDate(currentDate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos por si se editó algo en otra pantalla
        allClasses = dbHelper.getAllClasses();
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(calendarView.getDate());
        filterClassesByDate(currentDate);
    }
}