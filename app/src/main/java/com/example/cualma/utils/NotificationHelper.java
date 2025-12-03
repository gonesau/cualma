package com.example.cualma.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.cualma.R;
import com.example.cualma.database.ClassSchedule;
import java.util.Calendar;

public class NotificationHelper {

    private static final String CHANNEL_ID = "class_reminder_channel";
    private static final String CHANNEL_NAME = "Recordatorios de Clase";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para recordar las clases próximas");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void scheduleClassNotification(Context context, ClassSchedule classSchedule) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ClassReminderReceiver.class);
        intent.putExtra("class_name", classSchedule.getClassName());
        intent.putExtra("classroom", classSchedule.getClassroom());
        intent.putExtra("teacher", classSchedule.getTeacherName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                classSchedule.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = getNextClassTime(classSchedule);

        if (calendar != null && alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
            );
        }
    }

    private static Calendar getNextClassTime(ClassSchedule classSchedule) {
        Calendar calendar = Calendar.getInstance();

        int dayOfWeek = getDayOfWeek(classSchedule.getDay());
        if (dayOfWeek == -1) return null;

        String[] timeParts = classSchedule.getStartTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute - 10);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        return calendar;
    }

    private static int getDayOfWeek(String day) {
        switch (day) {
            case "Domingo": return Calendar.SUNDAY;
            case "Lunes": return Calendar.MONDAY;
            case "Martes": return Calendar.TUESDAY;
            case "Miércoles": return Calendar.WEDNESDAY;
            case "Jueves": return Calendar.THURSDAY;
            case "Viernes": return Calendar.FRIDAY;
            case "Sábado": return Calendar.SATURDAY;
            default: return -1;
        }
    }

    public static class ClassReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String className = intent.getStringExtra("class_name");
            String classroom = intent.getStringExtra("classroom");
            String teacher = intent.getStringExtra("teacher");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_schedule)
                    .setContentTitle("Recordatorio de Clase")
                    .setContentText(className + " comienza en 10 minutos")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(className + " comienza en 10 minutos\n" +
                                    "Aula: " + classroom + "\n" +
                                    "Docente: " + teacher))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}