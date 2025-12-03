package com.example.cualma;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleCalendarActivity extends AppCompatActivity {

    private LinearLayout calendarContainer;
    private DatabaseHelper dbHelper;
    private final String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_calendar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Calendario Semanal");

        dbHelper = new DatabaseHelper(this);
        calendarContainer = findViewById(R.id.calendarContainer);

        loadCalendar();
    }

    private void loadCalendar() {
        List<ClassSchedule> classes = dbHelper.getAllClasses();
        Map<String, Integer> dayColors = getDayColors();

        for (String day : days) {
            TextView dayHeader = new TextView(this);
            dayHeader.setText(day);
            dayHeader.setTextSize(20);
            dayHeader.setTextColor(Color.WHITE);
            dayHeader.setBackgroundColor(getResources().getColor(R.color.primary));
            dayHeader.setPadding(32, 24, 32, 24);
            dayHeader.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            headerParams.setMargins(0, 16, 0, 8);
            dayHeader.setLayoutParams(headerParams);
            calendarContainer.addView(dayHeader);

            boolean hasClasses = false;
            for (ClassSchedule classSchedule : classes) {
                if (classSchedule.getDay().equals(day)) {
                    hasClasses = true;
                    CardView card = createClassCard(classSchedule, dayColors.get(day));
                    calendarContainer.addView(card);
                }
            }

            if (!hasClasses) {
                TextView noClasses = new TextView(this);
                noClasses.setText("Sin clases programadas");
                noClasses.setTextSize(14);
                noClasses.setTextColor(Color.GRAY);
                noClasses.setPadding(32, 16, 32, 16);
                noClasses.setGravity(Gravity.CENTER);
                calendarContainer.addView(noClasses);
            }
        }
    }

    private CardView createClassCard(ClassSchedule classSchedule, int backgroundColor) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 8, 16, 8);
        card.setLayoutParams(cardParams);
        card.setCardElevation(4);
        card.setRadius(16);
        card.setCardBackgroundColor(backgroundColor);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 24, 24, 24);

        TextView tvClassName = new TextView(this);
        tvClassName.setText(classSchedule.getClassName());
        tvClassName.setTextSize(18);
        tvClassName.setTextColor(Color.parseColor("#212121"));
        tvClassName.setTypeface(null, android.graphics.Typeface.BOLD);
        cardContent.addView(tvClassName);

        TextView tvClassCode = new TextView(this);
        tvClassCode.setText(classSchedule.getClassCode());
        tvClassCode.setTextSize(14);
        tvClassCode.setTextColor(Color.parseColor("#757575"));
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        codeParams.setMargins(0, 8, 0, 0);
        tvClassCode.setLayoutParams(codeParams);
        cardContent.addView(tvClassCode);

        TextView tvTime = new TextView(this);
        tvTime.setText("⏰ " + classSchedule.getStartTime() + " - " + classSchedule.getEndTime());
        tvTime.setTextSize(14);
        tvTime.setTextColor(Color.parseColor("#212121"));
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timeParams.setMargins(0, 12, 0, 0);
        tvTime.setLayoutParams(timeParams);
        cardContent.addView(tvTime);

        TextView tvClassroom = new TextView(this);
        tvClassroom.setText("📍 Aula: " + classSchedule.getClassroom());
        tvClassroom.setTextSize(14);
        tvClassroom.setTextColor(Color.parseColor("#212121"));
        LinearLayout.LayoutParams classroomParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        classroomParams.setMargins(0, 8, 0, 0);
        tvClassroom.setLayoutParams(classroomParams);
        cardContent.addView(tvClassroom);

        TextView tvTeacher = new TextView(this);
        tvTeacher.setText("👤 " + classSchedule.getTeacherName());
        tvTeacher.setTextSize(14);
        tvTeacher.setTextColor(Color.parseColor("#212121"));
        LinearLayout.LayoutParams teacherParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        teacherParams.setMargins(0, 8, 0, 0);
        tvTeacher.setLayoutParams(teacherParams);
        cardContent.addView(tvTeacher);

        card.addView(cardContent);
        return card;
    }

    private Map<String, Integer> getDayColors() {
        Map<String, Integer> colors = new HashMap<>();
        colors.put("Lunes", Color.parseColor("#E3F2FD"));
        colors.put("Martes", Color.parseColor("#F3E5F5"));
        colors.put("Miércoles", Color.parseColor("#E8F5E9"));
        colors.put("Jueves", Color.parseColor("#FFF3E0"));
        colors.put("Viernes", Color.parseColor("#FCE4EC"));
        colors.put("Sábado", Color.parseColor("#E0F2F1"));
        colors.put("Domingo", Color.parseColor("#FFF9C4"));
        return colors;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}