package com.example.cualma;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.utils.NotificationHelper;
import com.example.cualma.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;

public class AddEditClassActivity extends AppCompatActivity {

    private TextInputEditText etClassCode, etClassName, etStartTime, etEndTime, etClassroom, etTeacher;
    private AutoCompleteTextView autoCompleteDay;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int classId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        classId = getIntent().getIntExtra("class_id", -1);

        if (classId != -1) {
            getSupportActionBar().setTitle("Editar Clase");
        } else {
            getSupportActionBar().setTitle("Nueva Clase");
        }

        initViews();
        setupDropdown();
        setupClickListeners();

        if (classId != -1) {
            loadClassData();
        }
    }

    private void initViews() {
        etClassCode = findViewById(R.id.etClassCode);
        etClassName = findViewById(R.id.etClassName);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etClassroom = findViewById(R.id.etClassroom);
        etTeacher = findViewById(R.id.etTeacher);
        autoCompleteDay = findViewById(R.id.autoCompleteDay);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupDropdown() {
        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, days);
        autoCompleteDay.setAdapter(adapter);
        if (autoCompleteDay.getText().toString().isEmpty()) {
            autoCompleteDay.setText(days[0], false);
        }
    }

    private void setupClickListeners() {
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etStartTime.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) showTimePicker(etStartTime);
        });

        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
        etEndTime.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) showTimePicker(etEndTime);
        });

        btnSave.setOnClickListener(v -> saveClass());
    }

    private void showTimePicker(TextInputEditText editText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    editText.setText(time);
                }, 8, 0, true);
        timePickerDialog.show();
    }

    private void loadClassData() {
        ClassSchedule classSchedule = dbHelper.getClass(classId);
        if (classSchedule != null) {
            etClassCode.setText(classSchedule.getClassCode());
            etClassName.setText(classSchedule.getClassName());
            etStartTime.setText(classSchedule.getStartTime());
            etEndTime.setText(classSchedule.getEndTime());
            etClassroom.setText(classSchedule.getClassroom());
            etTeacher.setText(classSchedule.getTeacherName());
            autoCompleteDay.setText(classSchedule.getDay(), false);
        }
    }

    private void saveClass() {
        String classCode = etClassCode.getText().toString().trim();
        String className = etClassName.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String classroom = etClassroom.getText().toString().trim();
        String teacher = etTeacher.getText().toString().trim();
        String day = autoCompleteDay.getText().toString();

        if (classCode.isEmpty() || className.isEmpty() || startTime.isEmpty() ||
                endTime.isEmpty() || classroom.isEmpty() || teacher.isEmpty() || day.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        ClassSchedule classSchedule = new ClassSchedule(
                classId, classCode, className, startTime, endTime, classroom, teacher, day
        );

        String carnet = sessionManager.getCarnet();
        long result;

        if (classId != -1) {
            result = dbHelper.updateClass(classSchedule);
            if (result > 0) {
                Toast.makeText(this, "Clase actualizada", Toast.LENGTH_SHORT).show();

                // Reprogramar notificación para esta clase
                NotificationHelper.scheduleClassNotification(this, classSchedule, carnet);
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            result = dbHelper.insertClass(classSchedule);
            if (result > 0) {
                Toast.makeText(this, "Clase agregada", Toast.LENGTH_SHORT).show();

                // Configurar el ID de la clase recién creada
                classSchedule.setId((int) result);

                // Programar notificación para esta nueva clase
                NotificationHelper.scheduleClassNotification(this, classSchedule, carnet);
                finish();
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}