package com.example.cualma.database;

public class Student {
    private String carnet;
    private String name;
    private String lastname;
    private String career;
    private String birthDate;
    private String email;

    public Student(String carnet, String name, String lastname, String career,
                   String birthDate, String email) {
        this.carnet = carnet;
        this.name = name;
        this.lastname = lastname;
        this.career = career;
        this.birthDate = birthDate;
        this.email = email;
    }

    public String getCarnet() {
        return carnet;
    }

    public void setCarnet(String carnet) {
        this.carnet = carnet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}