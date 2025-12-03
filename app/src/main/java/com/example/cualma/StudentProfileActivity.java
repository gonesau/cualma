package com.example.cualma;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.database.Student;

public class StudentProfileActivity extends AppCompatActivity {

    private EditText etCarnet, etName, etLastname, etCareer, etBirthDate, etEmail;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mi Perfil");

        dbHelper = new DatabaseHelper(this);

        initViews();
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

    private void loadStudentData() {
        Student student = dbHelper.getStudent();
        if (student != null) {
            etCarnet.setText(student.getCarnet());
            etName.setText(student.getName());
            etLastname.setText(student.getLastname());
            etCareer.setText(student.getCareer());
            etBirthDate.setText(student.getBirthDate());
            etEmail.setText(student.getEmail());
            isEditing = true;
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveStudentData());
    }

    private void saveStudentData() {
        String carnet = etCarnet.getText().toString().trim();
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

        Student student = new Student(carnet, name, lastname, career, birthDate, email);

        long result;
        if (isEditing) {
            result = dbHelper.updateStudent(student);
            if (result > 0) {
                Toast.makeText(this, R.string.data_updated, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            }
        } else {
            result = dbHelper.insertStudent(student);
            if (result > 0) {
                Toast.makeText(this, R.string.data_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}