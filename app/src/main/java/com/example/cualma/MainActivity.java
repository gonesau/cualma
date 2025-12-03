package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Verificar si ya existe una sesión activa antes de cargar la vista
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            goToDashboard();
            return; // Detiene la ejecución para no cargar el layout de login
        }

        setContentView(R.layout.activity_main);

        // 2. Inicializar base de datos y vistas
        dbHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupClickListeners() {
        // Opción: Ir a pantalla de registro (StudentProfileActivity)
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudentProfileActivity.class);
            startActivity(intent);
        });

        // Opción: Mostrar diálogo para iniciar sesión
        btnLogin.setOnClickListener(v -> showLoginDialog());
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Iniciar Sesión");
        builder.setMessage("Por favor, ingresa tu número de carnet:");

        // Crear el campo de texto programáticamente
        final EditText input = new EditText(this);
        input.setHint("Ej. AA123456");
        // Filtro para convertir a mayúsculas automáticamente (opcional)
        input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // Ajustar márgenes para que se vea bien
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        // Botón "Entrar"
        builder.setPositiveButton("Entrar", (dialog, which) -> {
            String carnet = input.getText().toString().trim().toUpperCase();
            if (!carnet.isEmpty()) {
                login(carnet);
            } else {
                Toast.makeText(MainActivity.this, "El carnet no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón "Cancelar"
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void login(String carnet) {
        // Verificar en la base de datos si el estudiante existe
        if (dbHelper.checkStudentExists(carnet)) {
            // Crear sesión y redirigir
            sessionManager.createLoginSession(carnet);
            Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show();
            goToDashboard();
        } else {
            // Si no existe, avisar y redirigir al registro con el carnet pre-llenado
            Toast.makeText(this, "Carnet no registrado. Por favor, completa tu registro.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, StudentProfileActivity.class);
            intent.putExtra("PREFILLED_CARNET", carnet); // Pasamos el carnet para facilitar el registro
            startActivity(intent);
        }
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Cierra MainActivity para que no se pueda volver atrás con el botón 'Back'
    }
}