package com._1studentcrud.service;

import com._1studentcrud.model.Teacher;
import com._1studentcrud.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository tr){
        this.teacherRepository = tr;
    }

    public Teacher getTeacherById(int id) {
        return teacherRepository.getTeacherById(id);
    }

    public List<Teacher> getAllTeacher() {
        return teacherRepository.getAllTeacher();
    }

    public Teacher addTeacher(Teacher teacher) {
        return teacherRepository.addTeacher(teacher);
    }

    public Teacher updateTeacher(Teacher teacher) {
        return teacherRepository.updateTeacher(teacher);
    }

    public String deleteTeacher(int id) {
        return teacherRepository.deleteTeacher(id);
    }
}
