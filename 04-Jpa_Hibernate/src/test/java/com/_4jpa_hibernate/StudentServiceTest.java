package com._4jpa_hibernate;

import com._4jpa_hibernate.entity.Course;
import com._4jpa_hibernate.entity.Student;
import com._4jpa_hibernate.entity.StudentProfile;
import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.repository.CourseRepository;
import com._4jpa_hibernate.repository.StudentRepository;
import com._4jpa_hibernate.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private StudentService studentService;

    // Test data helpers
    private Student student(String name, String email) {
        Student s = new Student(name, email, 20, "Campus Road");
        s.setId(1);
        return s;
    }

    private Course course(String name) {
        Course c = new Course(name);
        c.setId(10);
        return c;
    }

    private StudentProfile profile(String phone, String bio) {
        return new StudentProfile(phone, bio);
    }

    // getStudentByEmail
    @Nested
    @DisplayName("getStudentByEmail")
    class GetStudentByEmail {

        @Test
        @DisplayName("should return student when email exists")
        void shouldReturnStudentWhenFound() {
            // Arrange
            Student existing = student("Alice", "alice@uni.edu");
            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existing));

            // Act
            Student result = studentService.getStudentByEmail("alice@uni.edu");

            // Assert
            assertThat(result.getEmail()).isEqualTo("alice@uni.edu");
            assertThat(result.getName()).isEqualTo("Alice");
            verify(studentRepository).findByEmail("alice@uni.edu");
            verifyNoMoreInteractions(studentRepository);
            verifyNoInteractions(courseRepository);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when student does not exist")
        void shouldThrowWhenNotFound() {
            when(studentRepository.findByEmail("missing@uni.edu"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.getStudentByEmail("missing@uni.edu"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("missing@uni.edu");

            verify(studentRepository).findByEmail("missing@uni.edu");
            verifyNoInteractions(courseRepository);
        }
    }
    
    // addStudent
    @Nested
    @DisplayName("addStudent")
    class AddStudent {

        @Test
        @DisplayName("should save student when email is unique")
        void shouldAddStudentSuccessfully() {
            // Arrange
            Student newStudent = student("Bob", "bob@uni.edu");
            when(studentRepository.existsByEmail("bob@uni.edu")).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Student result = studentService.addStudent(newStudent);

            // Assert
            assertThat(result.getEmail()).isEqualTo("bob@uni.edu");
            assertThat(result.getName()).isEqualTo("Bob");

            verify(studentRepository).existsByEmail("bob@uni.edu");
            verify(studentRepository).save(newStudent);
            verifyNoMoreInteractions(studentRepository);
            verifyNoInteractions(courseRepository);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email already exists")
        void shouldThrowWhenEmailExists() {
            Student newStudent = student("Bob", "bob@uni.edu");
            when(studentRepository.existsByEmail("bob@uni.edu")).thenReturn(true);

            assertThatThrownBy(() -> studentService.addStudent(newStudent))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("bob@uni.edu");

            verify(studentRepository).existsByEmail("bob@uni.edu");
            verify(studentRepository, never()).save(any());
            verifyNoInteractions(courseRepository);
        }
    }

    // updateStudent
    @Nested
    @DisplayName("updateStudent")
    class UpdateStudent {

        @Test
        @DisplayName("should update fields when email stays the same")
        void shouldUpdateWhenEmailUnchanged() {
            // Arrange
            Student existing = student("Alice", "alice@uni.edu");
            Student request = new Student("Alice Updated", "alice@uni.edu", 22, "New Address");

            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existing));
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Student result = studentService.updateStudent("alice@uni.edu", request);

            // Assert
            assertThat(result.getName()).isEqualTo("Alice Updated");
            assertThat(result.getEmail()).isEqualTo("alice@uni.edu");
            assertThat(result.getAge()).isEqualTo(22);
            assertThat(result.getAddress()).isEqualTo("New Address");

            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(studentRepository, never()).existsByEmail(anyString());
            verify(studentRepository).save(existing);
            verifyNoInteractions(courseRepository);
        }

        @Test
        @DisplayName("should allow email change when new email is unique")
        void shouldUpdateEmailWhenUnique() {
            // Arrange
            Student existing = student("Alice", "alice@uni.edu");
            Student request = new Student("Alice", "alice.new@uni.edu", 20, "Campus Road");

            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existing));
            when(studentRepository.existsByEmail("alice.new@uni.edu")).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Student result = studentService.updateStudent("alice@uni.edu", request);

            // Assert
            assertThat(result.getEmail()).isEqualTo("alice.new@uni.edu");

            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(studentRepository).existsByEmail("alice.new@uni.edu");
            verify(studentRepository).save(existing);
            verifyNoInteractions(courseRepository);
        }

        @Test
        @DisplayName("should throw when changing to an already used email")
        void shouldThrowWhenNewEmailAlreadyExists() {
            // Arrange
            Student existing = student("Alice", "alice@uni.edu");
            Student request = new Student("Alice", "bob@uni.edu", 20, "Campus Road");

            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existing));
            when(studentRepository.existsByEmail("bob@uni.edu")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> studentService.updateStudent("alice@uni.edu", request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("bob@uni.edu");

            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(studentRepository).existsByEmail("bob@uni.edu");
            verify(studentRepository, never()).save(any());
            verifyNoInteractions(courseRepository);
        }

        @Test
        @DisplayName("should throw when student to update does not exist")
        void shouldThrowWhenStudentNotFound() {
            when(studentRepository.findByEmail("missing@uni.edu"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    studentService.updateStudent("missing@uni.edu", student("X", "x@uni.edu")))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(studentRepository).findByEmail("missing@uni.edu");
            verify(studentRepository, never()).save(any());
            verifyNoInteractions(courseRepository);
        }
    }

    // deleteStudent
    @Nested
    @DisplayName("deleteStudent")
    class DeleteStudent {

        @Test
        @DisplayName("should delete student when found")
        void shouldDeleteSuccessfully() {
            Student existing = student("Alice", "alice@uni.edu");
            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existing));

            studentService.deleteStudent("alice@uni.edu");

            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(studentRepository).delete(existing);
            verifyNoMoreInteractions(studentRepository);
            verifyNoInteractions(courseRepository);
        }

        @Test
        @DisplayName("should throw when deleting non-existent student")
        void shouldThrowWhenNotFound() {
            when(studentRepository.findByEmail("missing@uni.edu"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.deleteStudent("missing@uni.edu"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(studentRepository).findByEmail("missing@uni.edu");
            verify(studentRepository, never()).delete(any());
            verifyNoInteractions(courseRepository);
        }
    }

    // assignProfile (One-to-One)
    @Nested
    @DisplayName("assignProfile")
    class AssignProfile {

        @Test
        @DisplayName("should assign profile and wire both sides of the relationship")
        void shouldAssignProfileSuccessfully() {
            // Arrange
            Student existing = student("Alice", "alice@uni.edu");
            StudentProfile newProfile = profile("555-0100", "Loves Spring Boot");

            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existing));
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Student result = studentService.assignProfile("alice@uni.edu", newProfile);

            // Assert
            assertThat(result.getProfile()).isEqualTo(newProfile);
            assertThat(newProfile.getStudent()).isEqualTo(existing);

            ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(studentRepository).save(captor.capture());
            verifyNoInteractions(courseRepository);

            Student saved = captor.getValue();
            assertThat(saved.getProfile().getPhoneNumber()).isEqualTo("555-0100");
            assertThat(saved.getProfile().getBio()).isEqualTo("Loves Spring Boot");
            assertThat(saved.getProfile().getStudent()).isEqualTo(existing);
        }

        @Test
        @DisplayName("should throw when student does not exist")
        void shouldThrowWhenStudentNotFound() {
            when(studentRepository.findByEmail("missing@uni.edu"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    studentService.assignProfile("missing@uni.edu", profile("1", "bio")))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(studentRepository).findByEmail("missing@uni.edu");
            verify(studentRepository, never()).save(any());
            verifyNoInteractions(courseRepository);
        }
    }

    // enrollCourse (Many-to-Many)
    @Nested
    @DisplayName("enrollCourse")
    class EnrollCourse {

        @Test
        @DisplayName("should enroll student into course and maintain bidirectional link")
        void shouldEnrollSuccessfully() {
            // Arrange
            Student existingStudent = student("Alice", "alice@uni.edu");
            Course existingCourse = course("Spring Boot");

            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existingStudent));
            when(courseRepository.findByName("Spring Boot"))
                    .thenReturn(Optional.of(existingCourse));
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Student result = studentService.enrollCourse("alice@uni.edu", "Spring Boot");

            // Assert – both sides of Many-to-Many
            assertThat(result.getCourses()).contains(existingCourse);
            assertThat(existingCourse.getStudents()).contains(existingStudent);

            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(courseRepository).findByName("Spring Boot");
            verify(studentRepository).save(existingStudent);
            verifyNoMoreInteractions(studentRepository, courseRepository);
        }

        @Test
        @DisplayName("should throw when student does not exist")
        void shouldThrowWhenStudentNotFound() {
            when(studentRepository.findByEmail("missing@uni.edu"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    studentService.enrollCourse("missing@uni.edu", "Spring Boot"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("missing@uni.edu");

            verify(studentRepository).findByEmail("missing@uni.edu");
            verify(courseRepository, never()).findByName(anyString());
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when course does not exist")
        void shouldThrowWhenCourseNotFound() {
            Student existingStudent = student("Alice", "alice@uni.edu");
            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existingStudent));
            when(courseRepository.findByName("Unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    studentService.enrollCourse("alice@uni.edu", "Unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Unknown");

            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(courseRepository).findByName("Unknown");
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("enrolling the same course twice should remain idempotent (Set behavior)")
        void shouldRemainIdempotentWhenAlreadyEnrolled() {
            // Arrange – current production uses HashSet, so second add is a no-op
            Student existingStudent = student("Alice", "alice@uni.edu");
            Course existingCourse = course("Spring Boot");
            existingStudent.addCourse(existingCourse); // already enrolled

            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existingStudent));
            when(courseRepository.findByName("Spring Boot"))
                    .thenReturn(Optional.of(existingCourse));
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Student result = studentService.enrollCourse("alice@uni.edu", "Spring Boot");

            // Assert
            assertThat(result.getCourses()).hasSize(1);
            assertThat(result.getCourses()).contains(existingCourse);
            verify(studentRepository).save(existingStudent);
        }
    }

    // unenrollCourse (Many-to-Many)
    @Nested
    @DisplayName("unenrollCourse")
    class UnenrollCourse {

        @Test
        @DisplayName("should unenroll student from course and maintain bidirectional link")
        void shouldUnenrollSuccessfully() {
            // Arrange
            Student existingStudent = student("Alice", "alice@uni.edu");
            Course existingCourse = course("Spring Boot");
            existingStudent.addCourse(existingCourse);

            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existingStudent));
            when(courseRepository.findByName("Spring Boot"))
                    .thenReturn(Optional.of(existingCourse));
            when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Student result = studentService.unenrollCourse("alice@uni.edu", "Spring Boot");

            // Assert
            assertThat(result.getCourses()).doesNotContain(existingCourse);
            assertThat(existingCourse.getStudents()).doesNotContain(existingStudent);

            verify(studentRepository).findByEmail("alice@uni.edu");
            verify(courseRepository).findByName("Spring Boot");
            verify(studentRepository).save(existingStudent);
            verifyNoMoreInteractions(studentRepository, courseRepository);
        }

        @Test
        @DisplayName("should throw when student does not exist")
        void shouldThrowWhenStudentNotFound() {
            when(studentRepository.findByEmail("missing@uni.edu"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    studentService.unenrollCourse("missing@uni.edu", "Spring Boot"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(courseRepository, never()).findByName(anyString());
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when course does not exist")
        void shouldThrowWhenCourseNotFound() {
            Student existingStudent = student("Alice", "alice@uni.edu");
            when(studentRepository.findByEmail("alice@uni.edu"))
                    .thenReturn(Optional.of(existingStudent));
            when(courseRepository.findByName("Unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    studentService.unenrollCourse("alice@uni.edu", "Unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Unknown");

            verify(studentRepository, never()).save(any());
        }
    }

    // Pagination + Sorting
    @Nested
    @DisplayName("getStudentsWithPaginationAndSorting")
    class PaginationAndSorting {

        @Test
        @DisplayName("should return page of students")
        void shouldReturnPagedStudents() {
            // Arrange
            List<Student> content = List.of(
                    student("Alice", "alice@uni.edu"),
                    student("Bob", "bob@uni.edu")
            );
            Page<Student> page = new PageImpl<>(content);

            when(studentRepository.findAll(any(Pageable.class))).thenReturn(page);

            // Act
            Page<Student> result = studentService.getStudentsWithPaginationAndSorting(
                    0, 10, "email", "asc");

            // Assert
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Student::getEmail)
                    .containsExactly("alice@uni.edu", "bob@uni.edu");

            verify(studentRepository).findAll(any(Pageable.class));
            verifyNoInteractions(courseRepository);
        }

        @ParameterizedTest(name = "direction={0} should produce Sort.Direction.{1}")
        @CsvSource({
                "asc, ASC",
                "ASC, ASC",
                "desc, DESC",
                "DESC, DESC"
        })
        @DisplayName("should build Pageable with correct sort direction")
        void shouldBuildPageableWithCorrectSortDirection(String direction, Sort.Direction expected) {
            // Arrange
            when(studentRepository.findAll(any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            studentService.getStudentsWithPaginationAndSorting(1, 5, "name", direction);

            // Assert – capture the exact Pageable created by the service
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(studentRepository).findAll(pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(5);
            assertThat(pageable.getSort().getOrderFor("name")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("name").getDirection())
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("should default unknown direction to ascending (current production behavior)")
        void shouldDefaultUnknownDirectionToAscending() {
            when(studentRepository.findAll(any(Pageable.class)))
                    .thenReturn(Page.empty());

            studentService.getStudentsWithPaginationAndSorting(0, 10, "email", "sideways");

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(studentRepository).findAll(captor.capture());

            Sort.Order order = captor.getValue().getSort().getOrderFor("email");
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    // Simpler pagination
    @Nested
    @DisplayName("other list methods")
    class OtherListMethods {

        @Test
        @DisplayName("getStudentsWithPagination should use page + size only")
        void shouldPaginateWithoutExplicitSort() {
            when(studentRepository.findAll(any(Pageable.class)))
                    .thenReturn(Page.empty());

            studentService.getStudentsWithPagination(2, 20);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(studentRepository).findAll(captor.capture());

            assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
            assertThat(captor.getValue().getPageSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("getStudentsSorted should sort ascending by given field")
        void shouldReturnSortedStudents() {
            List<Student> students = List.of(student("Alice", "a@uni.edu"));
            when(studentRepository.findAll(any(Sort.class))).thenReturn(students);

            List<Student> result = studentService.getStudentsSorted("name");

            assertThat(result).hasSize(1);

            ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
            verify(studentRepository).findAll(sortCaptor.capture());
            assertThat(sortCaptor.getValue().getOrderFor("name").getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("getAllStudents should return full list")
        void shouldReturnAllStudents() {
            List<Student> students = List.of(
                    student("Alice", "alice@uni.edu"),
                    student("Bob", "bob@uni.edu")
            );
            when(studentRepository.findAll()).thenReturn(students);

            List<Student> result = studentService.getAllStudents();

            assertThat(result).hasSize(2);
            verify(studentRepository).findAll();
            verifyNoInteractions(courseRepository);
        }
    }
}