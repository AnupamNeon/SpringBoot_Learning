package com._4jpa_hibernate.controller;

import com._4jpa_hibernate.entity.Course;
import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.exception.GlobalExceptionHandler;
import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.service.CourseService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CourseController Web Layer Tests")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    private Course course(int id, String name) {
        Course c = new Course(name);
        c.setId(id);
        return c;
    }

    // GET /courses/{id}
    @Nested
    @DisplayName("GET /courses/{id}")
    class GetById {

        @Test
        @DisplayName("200 + course JSON when course exists")
        void shouldReturnCourse() throws Exception {
            // Arrange
            when(courseService.getCourseById(1)).thenReturn(course(1, "Spring Boot"));

            // Act & Assert
            mockMvc.perform(get("/courses/{id}", 1))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Spring Boot"));

            verify(courseService).getCourseById(1);
            verifyNoMoreInteractions(courseService);
        }

        @Test
        @DisplayName("404 when course does not exist")
        void shouldReturnNotFound() throws Exception {
            when(courseService.getCourseById(99))
                    .thenThrow(new ResourceNotFoundException("Course not found with id: 99"));

            mockMvc.perform(get("/courses/{id}", 99))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Course not found with id: 99"))
                    .andExpect(jsonPath("$.timestamp").isNumber());

            verify(courseService).getCourseById(99);
        }
    }

    // GET /courses
    @Nested
    @DisplayName("GET /courses")
    class GetAll {

        @Test
        @DisplayName("200 + list of courses")
        void shouldReturnAllCourses() throws Exception {
            when(courseService.getAllCourses()).thenReturn(
                    List.of(course(1, "Java"), course(2, "Spring Boot"))
            );

            mockMvc.perform(get("/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Java"))
                    .andExpect(jsonPath("$[1].name").value("Spring Boot"));

            verify(courseService).getAllCourses();
        }
    }

    // POST /courses
    @Nested
    @DisplayName("POST /courses")
    class AddCourse {

        @Test
        @DisplayName("201 + created course")
        void shouldCreateCourse() throws Exception {
            Course request = new Course("Clean Code");
            Course saved = course(10, "Clean Code");

            when(courseService.addCourse(any(Course.class))).thenReturn(saved);

            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.name").value("Clean Code"));

            verify(courseService).addCourse(any(Course.class));
        }

        @Test
        @DisplayName("409 when course name already exists")
        void shouldReturnConflictOnDuplicate() throws Exception {
            Course request = new Course("Java");

            when(courseService.addCourse(any(Course.class)))
                    .thenThrow(new DuplicateResourceException(
                            "Course already exists with name: Java"));

            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message")
                            .value("Course already exists with name: Java"));

            verify(courseService).addCourse(any(Course.class));
        }
    }

    // PUT /courses/{id}
    @Nested
    @DisplayName("PUT /courses/{id}")
    class UpdateCourse {

        @Test
        @DisplayName("200 + updated course")
        void shouldUpdateCourse() throws Exception {
            Course request = new Course("Updated Name");
            Course updated = course(1, "Updated Name");

            when(courseService.updateCourse(eq(1), any(Course.class))).thenReturn(updated);

            mockMvc.perform(put("/courses/{id}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated Name"));

            verify(courseService).updateCourse(eq(1), any(Course.class));
        }

        @Test
        @DisplayName("404 when updating missing course")
        void shouldReturnNotFoundOnUpdate() throws Exception {
            when(courseService.updateCourse(eq(99), any(Course.class)))
                    .thenThrow(new ResourceNotFoundException("Course not found with id: 99"));

            mockMvc.perform(put("/courses/{id}", 99)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new Course("X"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Course not found with id: 99"));
        }
    }

    // DELETE /courses/{id}
    @Nested
    @DisplayName("DELETE /courses/{id}")
    class DeleteCourse {

        @Test
        @DisplayName("200 + success message")
        void shouldDeleteCourse() throws Exception {
            // void method → doNothing (default) or explicit:
            doNothing().when(courseService).deleteCourse(1);

            mockMvc.perform(delete("/courses/{id}", 1))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Course deleted successfully"));

            verify(courseService).deleteCourse(1);
        }

        @Test
        @DisplayName("404 when deleting missing course")
        void shouldReturnNotFoundOnDelete() throws Exception {
            // void method that throws → doThrow
            doThrow(new ResourceNotFoundException("Course not found with id: 99"))
                    .when(courseService).deleteCourse(99);

            mockMvc.perform(delete("/courses/{id}", 99))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Course not found with id: 99"));

            verify(courseService).deleteCourse(99);
        }
    }

    // PUT /courses/{courseId}/teacher/{teacherId}
    @Nested
    @DisplayName("PUT /courses/{courseId}/teacher/{teacherId}")
    class AssignTeacher {

        @Test
        @DisplayName("200 when teacher assigned")
        void shouldAssignTeacher() throws Exception {
            when(courseService.assignTeacher(1, 5)).thenReturn(course(1, "Spring Boot"));

            mockMvc.perform(put("/courses/{courseId}/teacher/{teacherId}", 1, 5))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Spring Boot"));

            verify(courseService).assignTeacher(1, 5);
        }

        @Test
        @DisplayName("404 when course or teacher missing")
        void shouldReturnNotFoundWhenAssignFails() throws Exception {
            when(courseService.assignTeacher(1, 99))
                    .thenThrow(new ResourceNotFoundException("Teacher not found"));

            mockMvc.perform(put("/courses/{courseId}/teacher/{teacherId}", 1, 99))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Teacher not found"));
        }
    }
}