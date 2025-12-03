package com.example.cualma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.database.Student;
import com.example.cualma.utils.NotificationHelper;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private CardView cardProfile, cardSchedule;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        initViews();
        loadStudentData();
        setupClickListeners();

        NotificationHelper.createNotificationChannel(this);
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        cardProfile = findViewById(R.id.cardProfile);
        cardSchedule = findViewById(R.id.cardSchedule);
    }

    private void loadStudentData() {
        Student student = dbHelper.getStudent();
        if (student != null) {
            tvWelcome.setText("Bienvenido, " + student.getName());
        } else {
            tvWelcome.setText("Bienvenido");
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudentData();
    }
}