package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.database.Student;
import com.example.cualma.utils.NotificationHelper;
import com.example.cualma.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private CardView cardProfile, cardSchedule;
    private Button btnLogout;
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

        // Inicializar canal de notificaciones para las clases
        NotificationHelper.createNotificationChannel(this);
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        cardProfile = findViewById(R.id.cardProfile);
        cardSchedule = findViewById(R.id.cardSchedule);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadStudentData() {
        // Intentar obtener el estudiante usando el carnet de la sesión
        String carnet = sessionManager.getCarnet();
        // Nota: Idealmente crearías un método dbHelper.getStudentByCarnet(carnet)
        // Por ahora usamos el método genérico existente getStudent()
        Student student = dbHelper.getStudent();

        if (student != null) {
            // Dividir nombre para mostrar solo el primer nombre (más amigable)
            String firstName = student.getName().split(" ")[0];
            tvWelcome.setText("Hola, " + firstName);
        } else {
            tvWelcome.setText("Hola, Estudiante");
        }
    }

    private void setupClickListeners() {
        // Ir a Perfil
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentProfileActivity.class);
            startActivity(intent);
        });

        // Ir a Horario
        cardSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleActivity.class);
            startActivity(intent);
        });

        // Cerrar Sesión
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        sessionManager.logoutUser();
        Intent intent = new Intent(this, MainActivity.class);
        // Flags para limpiar la pila de actividades (evitar que vuelvan atrás)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos por si se editó el perfil y se volvió
        loadStudentData();
    }
}