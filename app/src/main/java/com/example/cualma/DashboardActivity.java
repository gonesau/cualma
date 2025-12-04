package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.database.Student;
import com.example.cualma.utils.NotificationHelper;
import com.example.cualma.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private CardView cardProfile, cardSchedule;
    private Button btnExit;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Seguridad: Verificar si hay sesión activa
        if (!sessionManager.isLoggedIn()) {
            logout();
            return;
        }

        initViews();
        loadStudentData();
        setupClickListeners();

        // Inicializar canal de notificaciones
        NotificationHelper.createNotificationChannel(this);

        // IMPORTANTE: Programar todas las notificaciones al iniciar
        NotificationHelper.scheduleAllNotificationsForCurrentUser(this);
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        cardProfile = findViewById(R.id.cardProfile);
        cardSchedule = findViewById(R.id.cardSchedule);
        btnExit = findViewById(R.id.btnExit);
    }

    private void loadStudentData() {
        String carnet = sessionManager.getCarnet();
        Student student = dbHelper.getStudentByCarnet(carnet);

        if (student != null) {
            String firstName = student.getName().split(" ")[0];
            tvWelcome.setText("Hola, " + firstName);
        } else {
            tvWelcome.setText("Hola, Estudiante");
        }
    }

    private void setupClickListeners() {
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentProfileActivity.class);
            startActivity(intent);
        });

        cardSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleActivity.class);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> showExitDialog());
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salir de la aplicación");
        builder.setMessage("¿Qué deseas hacer?");

        builder.setPositiveButton("Cerrar sesión", (dialog, which) -> {
            showLogoutConfirmationDialog();
        });

        builder.setNegativeButton("Solo salir", (dialog, which) -> {
            // Salir sin cerrar sesión
            finishAffinity(); // Cierra todas las actividades
        });

        builder.setNeutralButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar cierre de sesión");
        builder.setMessage("¿Estás seguro que deseas cerrar sesión? Deberás iniciar sesión nuevamente la próxima vez.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
            logout();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logout() {
        // IMPORTANTE: Cancelar todas las notificaciones antes de cerrar sesión
        NotificationHelper.cancelAllNotifications(this);

        sessionManager.logoutUser();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudentData();

        // Reprogramar notificaciones por si hubo cambios en el horario
        NotificationHelper.scheduleAllNotificationsForCurrentUser(this);
    }

    @Override
    public void onBackPressed() {
        // Interceptar el botón de retroceso para mostrar el diálogo de salida
        showExitDialog();
    }
}