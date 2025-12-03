package com.example.cualma;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.database.Student;
import com.example.cualma.utils.SessionManager;
import java.util.Calendar;
import java.util.Locale;

public class StudentProfileActivity extends AppCompatActivity {

    private EditText etCarnet, etName, etLastname, etCareer, etBirthDate, etEmail;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        // ... Toolbar setup (igual que antes) ...
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Perfil");

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        initViews();

        // Si venimos redirigidos del login fallido
        if(getIntent().hasExtra("PREFILLED_CARNET")){
            etCarnet.setText(getIntent().getStringExtra("PREFILLED_CARNET"));
        }

        loadStudentData();
        setupClickListeners();
    }

    private void initViews() {
        etCarnet = findViewById(R.id.etCarnet);
        etName = findViewById(R.id.etName);
        etLastname = findViewById(R.id.etLastname);
        etCareer = findViewById(R.id.etCareer);
        etBirthDate = findViewById(R.id.etBirthDate);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupClickListeners() {
        // 1. Calendario para fecha de nacimiento
        etBirthDate.setOnClickListener(v -> showDatePicker());
        etBirthDate.setFocusable(false); // Evitar escribir manualmente

        btnSave.setOnClickListener(v -> saveStudentData());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            etBirthDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveStudentData() {
        String carnet = etCarnet.getText().toString().trim().toUpperCase();
        String name = etName.getText().toString().trim();
        String lastname = etLastname.getText().toString().trim();
        String career = etCareer.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (carnet.isEmpty() || name.isEmpty() || lastname.isEmpty() ||
                career.isEmpty() || birthDate.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validación formato Carnet (2 letras + 6 números)
        if (!carnet.matches("^[A-Z]{2}\\d{6}$")) {
            etCarnet.setError("Formato inválido. Ejemplo: AA123456");
            etCarnet.requestFocus();
            return;
        }

        // 3. Validación Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return;
        }

        Student student = new Student(carnet, name, lastname, career, birthDate, email);
        long result;

        if (isEditing) {
            result = dbHelper.updateStudent(student);
        } else {
            result = dbHelper.insertStudent(student);
        }

        if (result > 0 || result != -1) {
            Toast.makeText(this, isEditing ? "Datos actualizados" : "Registro exitoso", Toast.LENGTH_SHORT).show();
            // Crear sesión automáticamente al guardar/registrar
            sessionManager.createLoginSession(carnet);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStudentData() {
        // Cargar datos solo si hay sesión o si estamos editando
        if (sessionManager.isLoggedIn()) {
            Student student = dbHelper.getStudent(); // Idealmente buscarías por carnet almacenado
            if (student != null) {
                etCarnet.setText(student.getCarnet());
                etCarnet.setEnabled(false); // No editar carnet una vez creado
                etName.setText(student.getName());
                etLastname.setText(student.getLastname());
                etCareer.setText(student.getCareer());
                etBirthDate.setText(student.getBirthDate());
                etEmail.setText(student.getEmail());
                isEditing = true;
                btnSave.setText("Actualizar Datos");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}