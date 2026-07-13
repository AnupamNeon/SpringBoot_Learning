package com._4jpa_hibernate;

import com._4jpa_hibernate.entity.Teacher;
import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.repository.TeacherRepository;
import com._4jpa_hibernate.service.TeacherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
}
