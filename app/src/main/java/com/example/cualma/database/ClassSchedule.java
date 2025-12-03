package com.example.cualma.database;

public class ClassSchedule {
    private int id;
    private String classCode;
    private String className;
    private String startTime;
    private String endTime;
    private String classroom;
    private String teacherName;
    private String day;

    public ClassSchedule(int id, String classCode, String className, String startTime,
                         String endTime, String classroom, String teacherName, String day) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroom = classroom;
        this.teacherName = teacherName;
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}