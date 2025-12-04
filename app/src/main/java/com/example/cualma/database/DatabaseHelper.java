package com.example.cualma.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cualma.db";
    private static final int DATABASE_VERSION = 2; // Incrementado por cambio en schema

    // Student Table
    private static final String TABLE_STUDENT = "student";
    private static final String COL_CARNET = "carnet";
    private static final String COL_NAME = "name";
    private static final String COL_LASTNAME = "lastname";
    private static final String COL_CAREER = "career";
    private static final String COL_BIRTH_DATE = "birth_date";
    private static final String COL_EMAIL = "email";

    // Schedule Table
    private static final String TABLE_SCHEDULE = "schedule";
    private static final String COL_ID = "id";
    private static final String COL_CLASS_CODE = "class_code";
    private static final String COL_CLASS_NAME = "class_name";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_END_TIME = "end_time";
    private static final String COL_CLASSROOM = "classroom";
    private static final String COL_TEACHER = "teacher_name";
    private static final String COL_DAY = "day";
    private static final String COL_STUDENT_CARNET = "student_carnet"; // NUEVO: Para asociar clases con estudiantes

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createStudentTable = "CREATE TABLE " + TABLE_STUDENT + " (" +
                COL_CARNET + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_LASTNAME + " TEXT, " +
                COL_CAREER + " TEXT, " +
                COL_BIRTH_DATE + " TEXT, " +
                COL_EMAIL + " TEXT)";

        // MODIFICADO: Agregada columna student_carnet con foreign key
        String createScheduleTable = "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CLASS_CODE + " TEXT, " +
                COL_CLASS_NAME + " TEXT, " +
                COL_START_TIME + " TEXT, " +
                COL_END_TIME + " TEXT, " +
                COL_CLASSROOM + " TEXT, " +
                COL_TEACHER + " TEXT, " +
                COL_DAY + " TEXT, " +
                COL_STUDENT_CARNET + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_STUDENT_CARNET + ") REFERENCES " + TABLE_STUDENT + "(" + COL_CARNET + ") ON DELETE CASCADE)";

        db.execSQL(createStudentTable);
        db.execSQL(createScheduleTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Agregar columna student_carnet a la tabla existente
            db.execSQL("ALTER TABLE " + TABLE_SCHEDULE + " ADD COLUMN " + COL_STUDENT_CARNET + " TEXT");

            // Si hay datos existentes, podríamos necesitar asignarlos a un usuario específico
            // Por ahora, las clases sin carnet asociado no se mostrarán
        }
    }

    // Student Methods
    public long insertStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CARNET, student.getCarnet());
        values.put(COL_NAME, student.getName());
        values.put(COL_LASTNAME, student.getLastname());
        values.put(COL_CAREER, student.getCareer());
        values.put(COL_BIRTH_DATE, student.getBirthDate());
        values.put(COL_EMAIL, student.getEmail());
        return db.insert(TABLE_STUDENT, null, values);
    }

    public Student getStudent() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT, null, null, null, null, null, null);

        Student student = null;
        if (cursor.moveToFirst()) {
            student = new Student(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CARNET)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LASTNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAREER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTH_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
            );
        }
        cursor.close();
        return student;
    }

    public int updateStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, student.getName());
        values.put(COL_LASTNAME, student.getLastname());
        values.put(COL_CAREER, student.getCareer());
        values.put(COL_BIRTH_DATE, student.getBirthDate());
        values.put(COL_EMAIL, student.getEmail());
        return db.update(TABLE_STUDENT, values, COL_CARNET + " = ?",
                new String[]{student.getCarnet()});
    }

    public boolean checkStudentExists(String carnet) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT, new String[]{COL_CARNET},
                COL_CARNET + "=?", new String[]{carnet}, null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public Student getStudentByCarnet(String carnet) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT, null,
                COL_CARNET + " = ?",
                new String[]{carnet},
                null, null, null);

        Student student = null;
        if (cursor.moveToFirst()) {
            student = new Student(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CARNET)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LASTNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAREER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTH_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
            );
        }
        cursor.close();
        return student;
    }

    // Schedule Methods - MODIFICADOS para incluir student_carnet

    /**
     * Inserta una clase asociada a un estudiante específico
     */
    public long insertClass(ClassSchedule classSchedule, String studentCarnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CLASS_CODE, classSchedule.getClassCode());
        values.put(COL_CLASS_NAME, classSchedule.getClassName());
        values.put(COL_START_TIME, classSchedule.getStartTime());
        values.put(COL_END_TIME, classSchedule.getEndTime());
        values.put(COL_CLASSROOM, classSchedule.getClassroom());
        values.put(COL_TEACHER, classSchedule.getTeacherName());
        values.put(COL_DAY, classSchedule.getDay());
        values.put(COL_STUDENT_CARNET, studentCarnet); // NUEVO
        return db.insert(TABLE_SCHEDULE, null, values);
    }

    /**
     * Obtiene todas las clases de un estudiante específico
     */
    public List<ClassSchedule> getAllClassesByStudent(String studentCarnet) {
        List<ClassSchedule> classList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // MODIFICADO: Filtramos por student_carnet
        Cursor cursor = db.query(TABLE_SCHEDULE, null,
                COL_STUDENT_CARNET + " = ?",
                new String[]{studentCarnet},
                null, null,
                COL_DAY + ", " + COL_START_TIME);

        if (cursor.moveToFirst()) {
            do {
                ClassSchedule classSchedule = new ClassSchedule(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASS_CODE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASS_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_END_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASSROOM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TEACHER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DAY))
                );
                classList.add(classSchedule);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classList;
    }

    /**
     * Método antiguo mantenido por compatibilidad - ahora devuelve lista vacía
     * @deprecated Usar getAllClassesByStudent(String studentCarnet) en su lugar
     */
    @Deprecated
    public List<ClassSchedule> getAllClasses() {
        return new ArrayList<>(); // Retorna lista vacía para evitar errores
    }

    /**
     * Obtiene una clase específica por ID (verifica que pertenezca al estudiante)
     */
    public ClassSchedule getClass(int id, String studentCarnet) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCHEDULE, null,
                COL_ID + " = ? AND " + COL_STUDENT_CARNET + " = ?",
                new String[]{String.valueOf(id), studentCarnet},
                null, null, null);

        ClassSchedule classSchedule = null;
        if (cursor.moveToFirst()) {
            classSchedule = new ClassSchedule(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASS_CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASS_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_END_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASSROOM)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TEACHER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DAY))
            );
        }
        cursor.close();
        return classSchedule;
    }

    /**
     * Método antiguo mantenido por compatibilidad
     * @deprecated Usar getClass(int id, String studentCarnet) en su lugar
     */
    @Deprecated
    public ClassSchedule getClass(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCHEDULE, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        ClassSchedule classSchedule = null;
        if (cursor.moveToFirst()) {
            classSchedule = new ClassSchedule(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASS_CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASS_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_END_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CLASSROOM)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TEACHER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DAY))
            );
        }
        cursor.close();
        return classSchedule;
    }

    /**
     * Actualiza una clase (verifica que pertenezca al estudiante)
     */
    public int updateClass(ClassSchedule classSchedule, String studentCarnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CLASS_CODE, classSchedule.getClassCode());
        values.put(COL_CLASS_NAME, classSchedule.getClassName());
        values.put(COL_START_TIME, classSchedule.getStartTime());
        values.put(COL_END_TIME, classSchedule.getEndTime());
        values.put(COL_CLASSROOM, classSchedule.getClassroom());
        values.put(COL_TEACHER, classSchedule.getTeacherName());
        values.put(COL_DAY, classSchedule.getDay());

        // MODIFICADO: Verificamos que la clase pertenezca al estudiante
        return db.update(TABLE_SCHEDULE, values,
                COL_ID + " = ? AND " + COL_STUDENT_CARNET + " = ?",
                new String[]{String.valueOf(classSchedule.getId()), studentCarnet});
    }

    /**
     * Elimina una clase (verifica que pertenezca al estudiante)
     */
    public void deleteClass(int id, String studentCarnet) {
        SQLiteDatabase db = this.getWritableDatabase();
        // MODIFICADO: Verificamos que la clase pertenezca al estudiante
        db.delete(TABLE_SCHEDULE,
                COL_ID + " = ? AND " + COL_STUDENT_CARNET + " = ?",
                new String[]{String.valueOf(id), studentCarnet});
    }

    /**
     * Método antiguo mantenido por compatibilidad
     * @deprecated Usar deleteClass(int id, String studentCarnet) en su lugar
     */
    @Deprecated
    public void deleteClass(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }
}