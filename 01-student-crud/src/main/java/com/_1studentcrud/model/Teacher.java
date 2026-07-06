package com._1studentcrud.model;

public class Teacher {
    private Integer teacher_id;
    private String teacher_name;
    private String email;
    private String department;

    public Teacher(Integer teacher_id, String teacher_name, String email, String department) {
        this.teacher_id = teacher_id;
        this.teacher_name = teacher_name;
        this.email = email;
        this.department = department;
    }

    public Integer getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(Integer teacher_id) {
        this.teacher_id = teacher_id;
    }

    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
