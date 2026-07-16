package com._4jpa_hibernate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4jpa_hibernate.entity.Teacher;
import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.repository.TeacherRepository;
import com._4jpa_hibernate.service.TeacherService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Unit Tests")
public class TeacherServiceTest {
    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    @Nested
    @DisplayName("addTeacher")
    class AddTeacher{
        @Test
        @DisplayName("should save and return teacher when email is unique")
        void shouldAddTeacherSuccessfully() {
            // Arrange
            Teacher teacher = new Teacher("Alice Smith","alice@email.com","Computer Science");

            when(teacherRepository.existsByEmail("alice@email.com")).thenReturn(false);
            when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Teacher result = teacherService.addTeacher(teacher);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("alice@email.com");
            assertThat(result.getName()).isEqualTo("Alice Smith");
            assertThat(result.getDepartment()).isEqualTo("Computer Science");

            verify(teacherRepository).existsByEmail("alice@email.com");
            verify(teacherRepository).save(teacher);
            verifyNoMoreInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email already exists")
        void shouldThrowWhenEmailAlreadyExists(){
            // Arrange
            Teacher teacher = new Teacher("Alice Smith","alice@email.com","Computer Science");
            when(teacherRepository.existsByEmail("alice@email.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(()-> teacherService.addTeacher(teacher))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("alice@email.com");

            verify(teacherRepository).existsByEmail("alice@email.com");
            verify(teacherRepository, never()).save(any());
            verifyNoMoreInteractions(teacherRepository);
        }
    }

    @Nested
    @DisplayName("getTeacherByEmail")
    class GetTeacherByEmail{

        @Test
        @DisplayName("should return teacher when found")
        void shouldReturnTeacherWhenFound(){
            // Arrange
            Teacher teacher = new Teacher("Johny", "johny@email.com", "Mathematics");
            when(teacherRepository.findByEmail("johny@email.com"))
                    .thenReturn(Optional.of(teacher));

            // Act
            Teacher result = teacherService.getTeacherByEmail("johny@email.com");

            // Assert
            assertThat(result).isEqualTo(teacher);
            verify(teacherRepository).findByEmail("johny@email.com");
            verifyNoMoreInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when teacher does not exist")
        void shouldThrowWhenTeacherNotFound(){
            // Arrange
            Teacher teacher = new Teacher("Johny", "johny@email.com", "Mathematics");
            when(teacherRepository.findByEmail("johny@email.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(()-> teacherService.getTeacherByEmail("johny@email.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("johny@email.com");

            verify(teacherRepository).findByEmail("johny@email.com");
            verifyNoMoreInteractions(teacherRepository);
        }
    }

    @Nested
    @DisplayName("updateTeacher")
    class UpdateTeacher {

        @Test
        @DisplayName("should update teacher when email is unchanged")
        void shouldUpdateTeacherWhenEmailUnchanged(){
            // Arrange
            Teacher existing = new Teacher("Alice Smith","alice@email.com","Computer Science");
            existing.setId(1);

            Teacher updated = new Teacher("Alice","alice@email.com","English");
            when(teacherRepository.findByEmail("alice@email.com")).thenReturn(Optional.of(existing));
            when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Teacher result = teacherService.updateTeacher("alice@email.com", updated);

            // Assert
            assertThat(result.getName()).isEqualTo("Alice");
            assertThat(result.getEmail()).isEqualTo("alice@email.com");
            assertThat(result.getDepartment()).isEqualTo("English");

            verify(teacherRepository).findByEmail("alice@email.com");
            verify(teacherRepository).save(existing);
            verify(teacherRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("should throw when trying to change to an already used email")
        void shouldThrowWhenNewEmailAlreadyExists() {
            // Arrange
            Teacher existing = new Teacher("Alice Smith", "alice@email.com", "Computer Science");
            existing.setId(1);

            Teacher updateRequest = new Teacher("Alice Smith", "bob@email.com", "Computer Science");

            when(teacherRepository.findByEmail("alice@email.com"))
                    .thenReturn(Optional.of(existing));
            when(teacherRepository.existsByEmail("bob@email.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> teacherService.updateTeacher("alice@email.com", updateRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("bob@email.com");

            verify(teacherRepository).findByEmail("alice@email.com");
            verify(teacherRepository).existsByEmail("bob@email.com");
            verify(teacherRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteTeacher")
    class DeleteTeacher {

        @Test
        @DisplayName("should delete teacher when found")
        void shouldDeleteTeacherSuccessfully() {
            // Arrange
            Teacher teacher = new Teacher("Alice Smith", "alice@email.com", "Computer Science");
            when(teacherRepository.findByEmail("alice@email.com"))
                    .thenReturn(Optional.of(teacher));

            // Act
            teacherService.deleteTeacher("alice@email.com");

            // Assert
            verify(teacherRepository).findByEmail("alice@email.com");
            verify(teacherRepository).delete(teacher);
            verifyNoMoreInteractions(teacherRepository);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when deleting non-existent teacher")
        void shouldThrowWhenDeletingNonExistentTeacher() {
            // Arrange
            when(teacherRepository.findByEmail("missing@email.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teacherService.deleteTeacher("missing@email.com"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(teacherRepository).findByEmail("missing@email.com");
            verify(teacherRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getAllTeacher")
    class GetAllTeachers {

        @Test
        @DisplayName("should return list of teachers")
        void shouldReturnAllTeachers() {
            // Arrange
            List<Teacher> teachers = List.of(
                    new Teacher("Alice", "alice@email.com", "CS"),
                    new Teacher("Bob", "bob@email.com", "Math")
            );
            when(teacherRepository.findAllWithCourses()).thenReturn(teachers);

            // Act
            List<Teacher> result = teacherService.getAllTeacher();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(teachers);
            verify(teacherRepository).findAllWithCourses();
            verifyNoMoreInteractions(teacherRepository);
        }
    }
}
