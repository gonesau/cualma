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
    private static final int DATABASE_VERSION = 1;

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

        String createScheduleTable = "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CLASS_CODE + " TEXT, " +
                COL_CLASS_NAME + " TEXT, " +
                COL_START_TIME + " TEXT, " +
                COL_END_TIME + " TEXT, " +
                COL_CLASSROOM + " TEXT, " +
                COL_TEACHER + " TEXT, " +
                COL_DAY + " TEXT)";

        db.execSQL(createStudentTable);
        db.execSQL(createScheduleTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        onCreate(db);
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

    // Schedule Methods
    public long insertClass(ClassSchedule classSchedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CLASS_CODE, classSchedule.getClassCode());
        values.put(COL_CLASS_NAME, classSchedule.getClassName());
        values.put(COL_START_TIME, classSchedule.getStartTime());
        values.put(COL_END_TIME, classSchedule.getEndTime());
        values.put(COL_CLASSROOM, classSchedule.getClassroom());
        values.put(COL_TEACHER, classSchedule.getTeacherName());
        values.put(COL_DAY, classSchedule.getDay());
        return db.insert(TABLE_SCHEDULE, null, values);
    }

    public List<ClassSchedule> getAllClasses() {
        List<ClassSchedule> classList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCHEDULE, null, null, null, null, null,
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

    public boolean checkStudentExists(String carnet) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT, new String[]{COL_CARNET},
                COL_CARNET + "=?", new String[]{carnet}, null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

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

    public int updateClass(ClassSchedule classSchedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CLASS_CODE, classSchedule.getClassCode());
        values.put(COL_CLASS_NAME, classSchedule.getClassName());
        values.put(COL_START_TIME, classSchedule.getStartTime());
        values.put(COL_END_TIME, classSchedule.getEndTime());
        values.put(COL_CLASSROOM, classSchedule.getClassroom());
        values.put(COL_TEACHER, classSchedule.getTeacherName());
        values.put(COL_DAY, classSchedule.getDay());
        return db.update(TABLE_SCHEDULE, values, COL_ID + " = ?",
                new String[]{String.valueOf(classSchedule.getId())});
    }

    public void deleteClass(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, COL_ID + " = ?", new String[]{String.valueOf(id)});
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


}