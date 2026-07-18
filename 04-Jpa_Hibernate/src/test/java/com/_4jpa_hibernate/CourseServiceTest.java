package com._4jpa_hibernate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4jpa_hibernate.entity.Course;
import com._4jpa_hibernate.entity.Teacher;
import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.repository.CourseRepository;
import com._4jpa_hibernate.repository.TeacherRepository;
import com._4jpa_hibernate.service.CourseService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Unit Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course(int id, String name) {
        Course c = new Course(name);
        c.setId(id);
        return c;
    }

    private Teacher teacher(int id, String name, String email, String dept) {
        Teacher t = new Teacher(name, email, dept);
        t.setId(id);
        return t;
    }

    // getCourseById
    @Nested
    @DisplayName("getCourseById")
    class GetCourseById {

        @Test
        @DisplayName("should return course when it exists")
        void shouldReturnCourseWhenFound() {
            // Arrange
            Course existing = course(1, "Java Fundamentals");
            when(courseRepository.findById(1)).thenReturn(Optional.of(existing));

            // Act
            Course result = courseService.getCourseById(1);

            // Assert
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Java Fundamentals");
            verify(courseRepository).findById(1);
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when course does not exist")
        void shouldThrowWhenNotFound() {
            // Arrange
            when(courseRepository.findById(99)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> courseService.getCourseById(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(courseRepository).findById(99);
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }
    }

    // getAllCourses

    @Nested
    @DisplayName("getAllCourses")
    class GetAllCourses {

        @Test
        @DisplayName("should return all courses from repository")
        void shouldReturnAllCourses() {
            // Arrange
            List<Course> courses = List.of(
                    course(1, "Java"),
                    course(2, "Spring Boot")
            );
            when(courseRepository.findAll()).thenReturn(courses);

            // Act
            List<Course> result = courseService.getAllCourses();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Course::getName)
                    .containsExactly("Java", "Spring Boot");

            verify(courseRepository).findAll();
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }
    }

    // addCourse
    @Nested
    @DisplayName("addCourse")
    class AddCourse {

        @Test
        @DisplayName("should save course when name is unique")
        void shouldAddCourseSuccessfully() {
            // Arrange
            Course newCourse = new Course("newCourse");
            when(courseRepository.existsByName("newCourse")).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Course result = courseService.addCourse(newCourse);

            // Assert
            assertThat(result.getName()).isEqualTo("newCourse");

            verify(courseRepository).existsByName("newCourse");
            verify(courseRepository).save(newCourse);
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when course name already exists")
        void shouldThrowWhenNameAlreadyExists() {
            // Arrange
            Course newCourse = new Course("Java Fundamentals");
            when(courseRepository.existsByName("Java Fundamentals")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> courseService.addCourse(newCourse))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Java Fundamentals");

            verify(courseRepository).existsByName("Java Fundamentals");
            verify(courseRepository, never()).save(any());
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should save when name is null (current production behavior)")
        void shouldSaveWhenNameIsNull() {
            // Arrange
            // Current code only checks duplicate when name != null
            Course newCourse = new Course();
            newCourse.setName(null);
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Course result = courseService.addCourse(newCourse);

            // Assert
            assertThat(result.getName()).isNull();
            verify(courseRepository, never()).existsByName(any());
            verify(courseRepository).save(newCourse);
        }
    }

    // updateCourse

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {

        @Test
        @DisplayName("should update existing course fields")
        void shouldUpdateCourseSuccessfully() {
            // Arrange
            Course existing = course(1, "Old Name");
            Course updateRequest = new Course("New Name");
            Teacher newTeacher = teacher(10, "Dr. Ada", "ada@email.com", "CS");
            updateRequest.setTeacher(newTeacher);

            when(courseRepository.findById(1)).thenReturn(Optional.of(existing));
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Course result = courseService.updateCourse(1, updateRequest);

            // Assert
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getTeacher()).isEqualTo(newTeacher);

            verify(courseRepository).findById(1);
            verify(courseRepository).save(existing);
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should throw when updating a non-existent course")
        void shouldThrowWhenCourseNotFound() {
            // Arrange
            when(courseRepository.findById(99)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> courseService.updateCourse(99, new Course("Whatever")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(courseRepository).findById(99);
            verify(courseRepository, never()).save(any());
            verifyNoInteractions(teacherRepository);
        }
    }

    // deleteCourse

    @Nested
    @DisplayName("deleteCourse")
    class DeleteCourse {

        @Test
        @DisplayName("should delete course when it exists")
        void shouldDeleteSuccessfully() {
            // Arrange
            when(courseRepository.existsById(1)).thenReturn(true);

            // Act
            courseService.deleteCourse(1);

            // Assert
            verify(courseRepository).existsById(1);
            verify(courseRepository).deleteById(1);
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when course does not exist")
        void shouldThrowWhenDeletingMissingCourse() {
            // Arrange
            when(courseRepository.existsById(99)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> courseService.deleteCourse(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(courseRepository).existsById(99);
            verify(courseRepository, never()).deleteById(anyInt());
            verifyNoMoreInteractions(courseRepository);
            verifyNoInteractions(teacherRepository);
        }
    }

    // assignTeacher  (most interesting method)

    @Nested
    @DisplayName("assignTeacher")
    class AssignTeacher {

        @Test
        @DisplayName("should assign teacher to course and persist the relationship")
        void shouldAssignTeacherSuccessfully() {
            // Arrange
            Course course = course(1, "Spring Boot");
            Teacher teacher = teacher(5, "Dr. Groot", "groot@email.com", "Computer Science");

            when(courseRepository.findById(1)).thenReturn(Optional.of(course));
            when(teacherRepository.findById(5)).thenReturn(Optional.of(teacher));
            when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Course result = courseService.assignTeacher(1, 5);

            // Assert – relationship set via Teacher.addCourse(...)
            assertThat(result.getTeacher()).isEqualTo(teacher);
            assertThat(teacher.getCourses()).contains(course);

            // Capture what was actually saved
            ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
            verify(courseRepository).findById(1);
            verify(teacherRepository).findById(5);
            verify(courseRepository).save(courseCaptor.capture());
            verifyNoMoreInteractions(courseRepository, teacherRepository);

            Course saved = courseCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(1);
            assertThat(saved.getTeacher()).isEqualTo(teacher);
        }

        @Test
        @DisplayName("should throw when course does not exist")
        void shouldThrowWhenCourseNotFound() {
            // Arrange
            when(courseRepository.findById(99)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> courseService.assignTeacher(99, 5))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(courseRepository).findById(99);
            verify(teacherRepository, never()).findById(anyInt());
            verify(courseRepository, never()).save(any());
            verifyNoMoreInteractions(courseRepository, teacherRepository);
        }

        @Test
        @DisplayName("should throw when teacher does not exist")
        void shouldThrowWhenTeacherNotFound() {
            // Arrange
            Course course = course(1, "Spring Boot");
            when(courseRepository.findById(1)).thenReturn(Optional.of(course));
            when(teacherRepository.findById(99)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> courseService.assignTeacher(1, 99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Teacher not found");

            verify(courseRepository).findById(1);
            verify(teacherRepository).findById(99);
            verify(courseRepository, never()).save(any());
            verifyNoMoreInteractions(courseRepository, teacherRepository);
        }
    }

    // Query-style methods
    @Nested
    @DisplayName("query methods")
    class QueryMethods {

        @Test
        @DisplayName("should return courses by teacher department")
        void shouldGetCoursesByDepartment() {
            // Arrange
            List<Course> courses = List.of(course(1, "Algorithms"));
            when(courseRepository.findByTeacherDepartment("Computer Science"))
                    .thenReturn(courses);

            // Act
            List<Course> result = courseService.getCoursesByDepartment("Computer Science");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Algorithms");
            verify(courseRepository).findByTeacherDepartment("Computer Science");
            verifyNoInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should return courses by student name")
        void shouldGetCoursesByStudentName() {
            // Arrange
            List<Course> courses = List.of(course(2, "Databases"));
            when(courseRepository.findCoursesByStudentName("Alice"))
                    .thenReturn(courses);

            // Act
            List<Course> result = courseService.getCoursesByStudentName("Alice");

            // Assert
            assertThat(result).extracting(Course::getName).containsExactly("Databases");
            verify(courseRepository).findCoursesByStudentName("Alice");
            verifyNoInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should return courses using native name search")
        void shouldGetCourseByNameNative() {
            // Arrange
            List<Course> courses = List.of(course(3, "Networking"));
            when(courseRepository.findByNameNative("Networking")).thenReturn(courses);

            // Act
            List<Course> result = courseService.getCourseByNameNative("Networking");

            // Assert
            assertThat(result).hasSize(1);
            verify(courseRepository).findByNameNative("Networking");
            verifyNoInteractions(teacherRepository);
        }
    }
}