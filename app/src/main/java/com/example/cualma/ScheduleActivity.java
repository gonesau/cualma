package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Importante para la búsqueda
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cualma.adapters.ClassAdapter;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.utils.ScheduleExporter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity implements ClassAdapter.OnClassClickListener {

    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mi Horario");

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        loadClasses();
        setupClickListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadClasses() {
        List<ClassSchedule> classes = dbHelper.getAllClasses();
        adapter.setClasses(classes);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditClassActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onClassClick(ClassSchedule classSchedule) {
        Intent intent = new Intent(this, AddEditClassActivity.class);
        intent.putExtra("class_id", classSchedule.getId());
        startActivity(intent);
    }

    @Override
    public void onClassDelete(ClassSchedule classSchedule) {
        dbHelper.deleteClass(classSchedule.getId());
        loadClasses(); // Recargar la lista después de borrar
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);

        // CONFIGURACIÓN DEL BUSCADOR
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Configurar hint
        searchView.setQueryHint("Buscar asignatura, aula...");

        // Escuchar cambios en el texto
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Filtrar al presionar enter
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtrar mientras se escribe
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_calendar) {
            Intent intent = new Intent(this, ScheduleCalendarActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_download) {
            ScheduleExporter.exportScheduleAsImage(this, dbHelper.getAllClasses());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }
}