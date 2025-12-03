package com.example.cualma;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cualma.adapters.ClassAdapter;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.utils.ScheduleExporter;
// CAMBIO IMPORTANTE: Importamos el botón Extendido
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity implements ClassAdapter.OnClassClickListener {

    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private DatabaseHelper dbHelper;
    // CAMBIO IMPORTANTE: Cambiamos el tipo de variable aquí
    private ExtendedFloatingActionButton fabAdd;

    // Launcher para guardar archivo
    private ActivityResultLauncher<Intent> saveFileLauncher;
    private Bitmap pendingBitmapToSave; // Variable temporal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Horario");
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupFilePicker(); // Configurar el selector de archivos
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        // Ahora el casting será correcto porque la variable es del mismo tipo que el XML
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // Opcional: Hacer que el botón se encoja al hacer scroll para dar efecto elegante
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fabAdd.isExtended()) {
                    fabAdd.shrink();
                } else if (dy < 0 && !fabAdd.isExtended()) {
                    fabAdd.extend();
                }
            }
        });
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditClassActivity.class));
        });
    }

    // Configuración del Selector de Archivos
    private void setupFilePicker() {
        saveFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null && pendingBitmapToSave != null) {
                            ScheduleExporter.saveBitmapToUri(this, pendingBitmapToSave, uri);
                        }
                    } else {
                        Toast.makeText(this, "Guardado cancelado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initiateSaveProcess() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "No hay clases para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Generar Bitmap
        pendingBitmapToSave = ScheduleExporter.captureRecyclerView(recyclerView);

        // 2. Abrir selector de archivos (Storage Access Framework)
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_TITLE, "MiHorario_CualMa.png");
        saveFileLauncher.launch(intent);
    }

    private void loadClasses() {
        List<ClassSchedule> classes = dbHelper.getAllClasses();
        adapter.setClasses(classes);
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
        loadClasses();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Buscar asignatura...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { adapter.getFilter().filter(query); return false; }
            @Override
            public boolean onQueryTextChange(String newText) { adapter.getFilter().filter(newText); return false; }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_calendar) {
            startActivity(new Intent(this, ScheduleCalendarActivity.class));
            return true;
        } else if (id == R.id.action_download) {
            initiateSaveProcess();
            return true;
        } else if (id == android.R.id.home) { // Manejar botón atrás del Toolbar
            finish();
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