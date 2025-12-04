package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.utils.NotificationHelper;
import com.example.cualma.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Si ya existe una sesión activa, ir al dashboard
        if (sessionManager.isLoggedIn()) {
            goToDashboard();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudentProfileActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> showLoginDialog());
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Iniciar Sesión");
        builder.setMessage("Por favor, ingresa tu número de carnet:");

        final EditText input = new EditText(this);
        input.setHint("Ej. AA123456");
        input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Entrar", (dialog, which) -> {
            String carnet = input.getText().toString().trim().toUpperCase();
            if (!carnet.isEmpty()) {
                login(carnet);
            } else {
                Toast.makeText(MainActivity.this, "El carnet no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void login(String carnet) {
        if (dbHelper.checkStudentExists(carnet)) {
            // Crear sesión
            sessionManager.createLoginSession(carnet);

            // Programar notificaciones para este usuario
            NotificationHelper.scheduleAllNotificationsForCurrentUser(this);

            Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show();
            goToDashboard();
        } else {
            Toast.makeText(this, "Carnet no registrado. Por favor, completa tu registro.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, StudentProfileActivity.class);
            intent.putExtra("PREFILLED_CARNET", carnet);
            startActivity(intent);
        }
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}