package com.example.cualma.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.cualma.R;
import com.example.cualma.database.ClassSchedule;
import com.example.cualma.database.DatabaseHelper;
import com.example.cualma.database.Student;
import java.util.Calendar;
import java.util.List;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "class_reminder_channel";
    private static final String CHANNEL_NAME = "Recordatorios de Clase";
    private static final int NOTIFICATION_ADVANCE_MINUTES = 10;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para recordar las clases próximas");
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Programa todas las notificaciones para el estudiante actual
     */
    public static void scheduleAllNotificationsForCurrentUser(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "No hay usuario logueado, no se programan notificaciones");
            return;
        }

        String carnet = sessionManager.getCarnet();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // MODIFICADO: Obtener solo las clases del usuario actual
        List<ClassSchedule> classes = dbHelper.getAllClassesByStudent(carnet);

        Log.d(TAG, "Programando " + classes.size() + " notificaciones para " + carnet);

        for (ClassSchedule classSchedule : classes) {
            scheduleClassNotification(context, classSchedule, carnet);
        }
    }

    /**
     * Programa la notificación para una clase específica
     */
    public static void scheduleClassNotification(Context context, ClassSchedule classSchedule, String carnet) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager no disponible");
            return;
        }

        // Crear intent con todos los datos necesarios
        Intent intent = new Intent(context, ClassReminderReceiver.class);
        intent.putExtra("class_id", classSchedule.getId());
        intent.putExtra("class_name", classSchedule.getClassName());
        intent.putExtra("classroom", classSchedule.getClassroom());
        intent.putExtra("teacher", classSchedule.getTeacherName());
        intent.putExtra("carnet", carnet);
        intent.putExtra("day", classSchedule.getDay());
        intent.putExtra("start_time", classSchedule.getStartTime());

        // ID único para cada notificación (combinación de ID de clase y carnet)
        int notificationId = (carnet + "_" + classSchedule.getId()).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calcular el tiempo de la próxima clase
        Calendar nextClassTime = getNextClassTime(classSchedule);

        if (nextClassTime != null) {
            try {
                // Usar setRepeating para que se repita semanalmente
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        nextClassTime.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY * 7, // Repetir cada semana
                        pendingIntent
                );

                Log.d(TAG, "Notificación programada: " + classSchedule.getClassName() +
                        " para " + nextClassTime.getTime().toString());
            } catch (SecurityException e) {
                Log.e(TAG, "Permiso denegado para programar alarma exacta", e);
            }
        }
    }

    /**
     * Calcula la próxima vez que ocurrirá la clase
     */
    private static Calendar getNextClassTime(ClassSchedule classSchedule) {
        Calendar calendar = Calendar.getInstance();

        int dayOfWeek = getDayOfWeek(classSchedule.getDay());
        if (dayOfWeek == -1) {
            Log.e(TAG, "Día inválido: " + classSchedule.getDay());
            return null;
        }

        // Parsear hora de inicio
        String[] timeParts = classSchedule.getStartTime().split(":");
        if (timeParts.length != 2) {
            Log.e(TAG, "Formato de hora inválido: " + classSchedule.getStartTime());
            return null;
        }

        try {
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Establecer el día y hora de la clase
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Restar 10 minutos para la notificación anticipada
            calendar.add(Calendar.MINUTE, -NOTIFICATION_ADVANCE_MINUTES);

            // Si ya pasó esta semana, programar para la próxima
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }

            return calendar;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error al parsear hora", e);
            return null;
        }
    }

    /**
     * Convierte nombre del día a constante de Calendar
     */
    private static int getDayOfWeek(String day) {
        switch (day.toLowerCase()) {
            case "domingo": return Calendar.SUNDAY;
            case "lunes": return Calendar.MONDAY;
            case "martes": return Calendar.TUESDAY;
            case "miércoles": return Calendar.WEDNESDAY;
            case "jueves": return Calendar.THURSDAY;
            case "viernes": return Calendar.FRIDAY;
            case "sábado": return Calendar.SATURDAY;
            default: return -1;
        }
    }

    /**
     * Cancela todas las notificaciones programadas del usuario actual
     */
    public static void cancelAllNotifications(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String carnet = sessionManager.getCarnet();

        if (carnet == null) {
            Log.d(TAG, "No hay carnet para cancelar notificaciones");
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // MODIFICADO: Obtener solo las clases del usuario actual
        List<ClassSchedule> classes = dbHelper.getAllClassesByStudent(carnet);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        for (ClassSchedule classSchedule : classes) {
            int notificationId = (carnet + "_" + classSchedule.getId()).hashCode();

            Intent intent = new Intent(context, ClassReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }

        Log.d(TAG, "Notificaciones canceladas para " + carnet);
    }

    /**
     * Cancela la notificación de una clase específica
     */
    public static void cancelClassNotification(Context context, int classId) {
        SessionManager sessionManager = new SessionManager(context);
        String carnet = sessionManager.getCarnet();

        if (carnet == null) return;

        int notificationId = (carnet + "_" + classId).hashCode();

        Intent intent = new Intent(context, ClassReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    /**
     * BroadcastReceiver que maneja las notificaciones
     */
    public static class ClassReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Verificar que el usuario sigue logueado
            SessionManager sessionManager = new SessionManager(context);
            if (!sessionManager.isLoggedIn()) {
                Log.d(TAG, "Usuario no logueado, no se muestra notificación");
                return;
            }

            String currentCarnet = sessionManager.getCarnet();
            String notificationCarnet = intent.getStringExtra("carnet");

            // Verificar que la notificación es para el usuario actual
            if (currentCarnet == null || !currentCarnet.equals(notificationCarnet)) {
                Log.d(TAG, "Notificación para otro usuario, ignorando");
                return;
            }

            // Obtener datos del estudiante
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            Student student = dbHelper.getStudentByCarnet(currentCarnet);

            String studentName = student != null ? student.getName().split(" ")[0] : "Estudiante";
            String className = intent.getStringExtra("class_name");
            String classroom = intent.getStringExtra("classroom");
            String teacher = intent.getStringExtra("teacher");

            // Crear la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_schedule)
                    .setContentTitle("¡Hola " + studentName + "!")
                    .setContentText("En 10 minutos empieza tu clase de " + className)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("En 10 minutos empieza tu clase de " + className +
                                    "\n📍 Aula: " + classroom +
                                    "\n👨‍🏫 Docente: " + teacher))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 200, 500})
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "Notificación mostrada: " + className);
            }
        }
    }

    /**
     * BroadcastReceiver para reprogramar notificaciones al reiniciar el dispositivo
     */
    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                Log.d(TAG, "Dispositivo reiniciado, reprogramando notificaciones");
                scheduleAllNotificationsForCurrentUser(context);
            }
        }
    }
}