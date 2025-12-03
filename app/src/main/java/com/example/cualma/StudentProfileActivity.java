package com.example.cualma;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
// Usamos TextInputEditText para mejor compatibilidad con el diseño nuevo
import com.google.android.material.textfield.TextInputEditText;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.database.Student;
import com.example.cualma.utils.SessionManager;
import java.util.Calendar;
import java.util.Locale;

public class StudentProfileActivity extends AppCompatActivity {

    // Cambiamos a TextInputEditText para que coincida con el XML
    private TextInputEditText etCarnet, etName, etLastname, etCareer, etBirthDate, etEmail;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Habilitar flecha de regreso en toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        initViews();

        if(getIntent().hasExtra("PREFILLED_CARNET")){
            etCarnet.setText(getIntent().getStringExtra("PREFILLED_CARNET"));
        }

        loadStudentData();
        setupClickListeners();
    }

    private void initViews() {
        // Los IDs son los mismos que en el XML
        etCarnet = findViewById(R.id.etCarnet);
        etName = findViewById(R.id.etName);
        etLastname = findViewById(R.id.etLastname);
        etCareer = findViewById(R.id.etCareer);
        etBirthDate = findViewById(R.id.etBirthDate);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupClickListeners() {
        etBirthDate.setOnClickListener(v -> showDatePicker());
        // Importante: asegurar que el click funcione sobre todo el input
        etBirthDate.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) showDatePicker();
        });

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
        // Validar nulos para evitar crash
        if(etCarnet.getText() == null || etName.getText() == null) return;

        String carnet = etCarnet.getText().toString().trim().toUpperCase();
        String name = etName.getText().toString().trim();
        String lastname = etLastname.getText().toString().trim();
        String career = etCareer.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (carnet.isEmpty() || name.isEmpty() || lastname.isEmpty() ||
                career.isEmpty() || birthDate.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!carnet.matches("^[A-Z]{2}\\d{6}$")) {
            etCarnet.setError("Formato inválido (Ej: AA123456)");
            etCarnet.requestFocus();
            return;
        }

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
            sessionManager.createLoginSession(carnet);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar en base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStudentData() {
        if (sessionManager.isLoggedIn()) {
            Student student = dbHelper.getStudent();
            if (student != null) {
                etCarnet.setText(student.getCarnet());
                etCarnet.setEnabled(false); // No editar carnet
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manejar flecha atrás del Toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}