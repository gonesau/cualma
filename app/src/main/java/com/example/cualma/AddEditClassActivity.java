package com.example.cualma;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.utils.NotificationHelper;
import java.util.Locale;

public class AddEditClassActivity extends AppCompatActivity {

    private EditText etClassCode, etClassName, etStartTime, etEndTime, etClassroom, etTeacher;
    private Spinner spinnerDay;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private int classId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(this);

        classId = getIntent().getIntExtra("class_id", -1);

        if (classId != -1) {
            getSupportActionBar().setTitle("Editar Clase");
        } else {
            getSupportActionBar().setTitle("Agregar Clase");
        }

        initViews();
        setupSpinner();
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
        spinnerDay = findViewById(R.id.spinnerDay);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupSpinner() {
        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(adapter);
    }

    private void setupClickListeners() {
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
        btnSave.setOnClickListener(v -> saveClass());
    }

    private void showTimePicker(EditText editText) {
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

            String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(classSchedule.getDay())) {
                    spinnerDay.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveClass() {
        String classCode = etClassCode.getText().toString().trim();
        String className = etClassName.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String classroom = etClassroom.getText().toString().trim();
        String teacher = etTeacher.getText().toString().trim();
        String day = spinnerDay.getSelectedItem().toString();

        if (classCode.isEmpty() || className.isEmpty() || startTime.isEmpty() ||
                endTime.isEmpty() || classroom.isEmpty() || teacher.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        ClassSchedule classSchedule = new ClassSchedule(
                classId, classCode, className, startTime, endTime, classroom, teacher, day
        );

        long result;
        if (classId != -1) {
            result = dbHelper.updateClass(classSchedule);
            if (result > 0) {
                Toast.makeText(this, R.string.class_updated, Toast.LENGTH_SHORT).show();
                NotificationHelper.scheduleClassNotification(this, classSchedule);
                finish();
            } else {
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            }
        } else {
            result = dbHelper.insertClass(classSchedule);
            if (result > 0) {
                Toast.makeText(this, R.string.class_added, Toast.LENGTH_SHORT).show();
                classSchedule.setId((int) result);
                NotificationHelper.scheduleClassNotification(this, classSchedule);
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