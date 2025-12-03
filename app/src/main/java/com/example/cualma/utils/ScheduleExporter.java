package com.example.cualma.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;
import android.widget.Toast;
import com.example.cualma.database.ClassSchedule;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleExporter {

    public static void exportScheduleAsImage(Context context, List<ClassSchedule> classes) {
        if (classes.isEmpty()) {
            Toast.makeText(context, "No hay clases para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        int width = 1080;
        int height = 1920;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.parseColor("#2196F3"));
        canvas.drawRect(0, 0, width, 200, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Mi Horario", 50, 120, paint);

        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        int yOffset = 250;
        int cardHeight = 200;
        int margin = 20;

        for (String day : days) {
            boolean hasClasses = false;

            for (ClassSchedule classSchedule : classes) {
                if (classSchedule.getDay().equals(day)) {
                    hasClasses = true;

                    paint.setColor(Color.parseColor("#F5F5F5"));
                    canvas.drawRoundRect(margin, yOffset, width - margin,
                            yOffset + cardHeight, 20, 20, paint);

                    paint.setColor(Color.parseColor("#2196F3"));
                    paint.setTextSize(40);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    canvas.drawText(classSchedule.getClassName(), margin + 30, yOffset + 50, paint);

                    paint.setColor(Color.parseColor("#757575"));
                    paint.setTextSize(30);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    canvas.drawText(day + " | " + classSchedule.getStartTime() + " - " +
                            classSchedule.getEndTime(), margin + 30, yOffset + 90, paint);

                    canvas.drawText("Aula: " + classSchedule.getClassroom(),
                            margin + 30, yOffset + 130, paint);
                    canvas.drawText("Docente: " + classSchedule.getTeacherName(),
                            margin + 30, yOffset + 170, paint);

                    yOffset += cardHeight + margin;
                }
            }
        }

        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "CualMa");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            File file = new File(directory, "Horario_" + timestamp + ".png");

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Toast.makeText(context, "Horario exportado: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al exportar el horario", Toast.LENGTH_SHORT).show();
        }
    }
}