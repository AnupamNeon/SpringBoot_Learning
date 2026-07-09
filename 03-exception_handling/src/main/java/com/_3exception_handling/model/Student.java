package com._3exception_handling.model;

public class Student {
    private Integer student_id;
    private String name;
    private Integer age;
    private String address;
    private String course;

    public Student() {
    }

    public Student(Integer student_id, String name, Integer age, String address, String course) {
        this.student_id = student_id;
        this.name = name;
        this.age = age;
        this.address = address;
        this.course = course;
    }

    public Integer getStudent_id() {
        return student_id;
    }

    public void setStudent_id(Integer student_id) {
        this.student_id = student_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}