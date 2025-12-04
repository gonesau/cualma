package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cualma.adapters.ClassAdapter;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.utils.NotificationHelper;
import com.example.cualma.utils.SessionManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleCalendarActivity extends AppCompatActivity implements ClassAdapter.OnClassClickListener {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvSelectedDate, tvEmptyState;
    private ClassAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<ClassSchedule> allClasses;
    private String currentCarnet;

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
        sessionManager = new SessionManager(this);

        // IMPORTANTE: Obtener el carnet del usuario actual
        currentCarnet = sessionManager.getCarnet();

        // Verificar sesión activa
        if (currentCarnet == null || !sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();

        // MODIFICADO: Cargar solo las clases del usuario actual
        allClasses = dbHelper.getAllClassesByStudent(currentCarnet);

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
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        String dayString = getDayString(dayOfWeek);

        tvSelectedDate.setText("Clases del " + dayString);

        List<ClassSchedule> filteredList = new ArrayList<>();
        for (ClassSchedule item : allClasses) {
            if (item.getDay().equalsIgnoreCase(dayString)) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.setClasses(filteredList);
        }
    }

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

    @Override
    public void onClassClick(ClassSchedule classSchedule) {
        Intent intent = new Intent(this, AddEditClassActivity.class);
        intent.putExtra("class_id", classSchedule.getId());
        startActivity(intent);
    }

    @Override
    public void onClassDelete(ClassSchedule classSchedule) {
        showDeleteConfirmationDialog(classSchedule);
    }

    private void showDeleteConfirmationDialog(ClassSchedule classSchedule) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Estás seguro que deseas eliminar la clase \"" +
                classSchedule.getClassName() + "\"?\n\n" +
                "Esta acción no se puede deshacer.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Sí, eliminar", (dialog, which) -> {
            // Cancelar la notificación de esta clase
            NotificationHelper.cancelClassNotification(this, classSchedule.getId());

            // MODIFICADO: Eliminar verificando el propietario
            dbHelper.deleteClass(classSchedule.getId(), currentCarnet);

            // Recargar datos del usuario actual
            allClasses = dbHelper.getAllClassesByStudent(currentCarnet);

            // Volver a filtrar para la fecha actual
            Calendar currentDate = Calendar.getInstance();
            currentDate.setTimeInMillis(calendarView.getDate());
            filterClassesByDate(currentDate);

            Toast.makeText(this, "Clase eliminada exitosamente", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
        // MODIFICADO: Recargar solo las clases del usuario actual
        allClasses = dbHelper.getAllClassesByStudent(currentCarnet);
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(calendarView.getDate());
        filterClassesByDate(currentDate);
    }
}