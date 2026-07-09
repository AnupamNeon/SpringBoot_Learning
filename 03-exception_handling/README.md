# 🚨 Exception Handling Implementation (Step by Step)

This project is continuation of existing Student CRUD project by implementing exception handling. It explains how the application's exception handling was refactored to follow a clean layered architecture using Spring Boot's global exception handling.

---

# 📌 Problem with the Original Implementation

The original code had three major issues:

1. **Swallowed Exceptions**

    * Every `catch` block called `e.printStackTrace()` and returned `null`.
    * This hid the actual cause of failures.

2. **Scattered Null Checks**

    * Every controller manually checked for `null`.
    * Controllers were responsible for building error responses.

3. **Inconsistent Error Responses**

    * Some endpoints returned empty bodies.
    * Others returned plain strings.
    * Some relied on Spring's default error JSON.

### ✅ Solution

Move exception handling to the appropriate layers:

```
Service Layer
        ↓
Throw meaningful exceptions
        ↓
@ControllerAdvice
        ↓
Consistent HTTP responses
```

---

# 🏗️ Architecture

## Before

```text
Repository
    ↓ (returns null on error)
Controller
    ↓ (manual null checks)
Inconsistent HTTP responses
```

## After

```text
Repository
    ↓
throws OperationException
    ↓
Service
    ↓
throws ResourceNotFoundException
throws DuplicateResourceException
    ↓
Controller
    ↓
No null checks
    ↓
GlobalExceptionHandler
    ↓
Consistent ErrorResponse JSON
```

---

# 📁 New Files

All new files are located inside the `exception` package.

---

## 1. `ResourceNotFoundException.java`

Thrown when a requested resource (Student, Course, or Teacher) cannot be found.

### Why extend `RuntimeException`?

Because it is an **unchecked exception**, callers are not forced to write unnecessary `try-catch` blocks. The exception automatically propagates to the global exception handler.

---

## 2. `DuplicateResourceException.java`

Thrown when attempting to create a resource that already exists.

Example:

* Student ID already exists
* Course ID already exists
* Teacher ID already exists

---

## 3. `OperationException.java`

Represents database-related failures.

Instead of swallowing `SQLException`, repositories now wrap it inside an `OperationException` and rethrow it.

---

## 4. `ErrorResponse.java`

Defines a consistent structure for every error returned by the API.

Example:

```json
{
  "status": 404,
  "message": "Student not found with id: 1",
  "timestamp": "2026-07-08T10:30:00"
}
```

---

## 5. `GlobalExceptionHandler.java`

This is the heart of the implementation.

Using `@ControllerAdvice`, Spring automatically catches exceptions thrown from any controller and converts them into proper HTTP responses.

### How it works

```
Controller
      ↓
Exception Thrown
      ↓
@ControllerAdvice
      ↓
@ExceptionHandler
      ↓
HTTP Response
```

### Why this is better

* One centralized place for error handling
* No duplicate controller code
* Consistent JSON responses
* Proper HTTP status codes

### Handler Priority

Spring always chooses the **most specific** matching handler.

For example:

```
ResourceNotFoundException
        ↓
Handled before
        ↓
Exception
```

---

# ✏️ Modified Files

---

## 6. `repository/CourseRepository.java`

### Changes

* Added `OperationException`
* Replaced every:

```java
e.printStackTrace();
return null;
```

with

```java
throw new OperationException(...);
```

* `deleteCourse()`

    * Return type changed from `String` → `boolean`
    * Removed existence check

### Why?

Repositories should only perform database operations.

Business validation (such as checking whether a course exists) belongs in the Service layer.

---

## 7. `repository/StudentRepository.java`

### Changes

* Replaced all `printStackTrace()` calls with `OperationException`
* Removed unused `int id` parameter from `updateStudent()`
* Removed existence checks
* `deleteStudent()`

    * Return type changed from `String` → `boolean`

---

## 8. `repository/TeacherRepository.java`

### Changes

* Replaced all `printStackTrace()` calls
* Removed existence checks
* `deleteTeacher()`

    * Return type changed from `String` → `void`

### Bug Fix

Original code:

```java
if (rs.next())
```

Only the first associated course was dissociated.

Updated code:

```java
while (rs.next())
```

Now **all** associated courses are properly dissociated before deleting the teacher.

---

## 9. `service/CourseService.java`

### Changes

* Added:

    * `ResourceNotFoundException`
    * `DuplicateResourceException`

### Methods Updated

#### `getCourseById()`

* Throws `ResourceNotFoundException` when the repository returns `null`.

#### `addCourse()`

* Checks for duplicate IDs.
* Throws `DuplicateResourceException`.

#### `updateCourse()`

Signature changed:

```java
updateCourse(int id, Course course)
```

The service:

1. Verifies the course exists.
2. Sets the ID from the path variable.
3. Updates the course.

#### `deleteCourse()`

* Return type changed from `String` → `void`
* Validates existence before deletion.

---

## 10. `service/StudentService.java`

Applied the same pattern as `CourseService`.

### Changes

* Throws exceptions instead of returning `null`
* Duplicate validation
* Update method signature changed
* Delete method simplified

---

## 11. `service/TeacherService.java`

Applied the same improvements.

### Changes

* Added validation exceptions
* `updateTeacher(int id, Teacher teacher)`
* `deleteTeacher()` returns `void`

---

## 12. `controller/CourseController.java`

### Changes

* Dependency changed:

```
CourseRepository
        ↓
CourseService
```

* Removed all manual `null` checks
* `updateCourse()` now passes the path variable ID
* Simplified delete endpoint

---

## 13. `controller/StudentController.java`

### Changes

* Removed all manual `null` checks
* Simplified update endpoint
* Simplified delete endpoint

---

## 14. `controller/TeacherController.java`

### Changes

* Dependency changed:

```
TeacherRepository
        ↓
TeacherService
```

* Removed manual `null` checks
* Update now passes the path variable ID
* Simplified delete endpoint

---

# ✅ Unchanged Files

The following files required **no modifications**:

* `Application.java`
* `config/AppConfig.java`
* `config/JdbcConfig.java`
* `model/Course.java`
* `model/Student.java`
* `model/Teacher.java`

---

# 📋 Summary

| Layer                      | Changes                            | Purpose                                                 |
| -------------------------- | ---------------------------------- | ------------------------------------------------------- |
| **Exception Classes**      | Added custom exceptions            | Provide meaningful application-specific errors          |
| **ErrorResponse**          | Added common response DTO          | Standardized error response format                      |
| **GlobalExceptionHandler** | Added `@ControllerAdvice`          | Centralized exception handling                          |
| **Repositories**           | Throw `OperationException`         | Stop swallowing database errors                         |
| **Repositories**           | Removed business validation        | Keep repository focused on database access              |
| **Services**               | Throw `ResourceNotFoundException`  | Handle business validation in the correct layer         |
| **Services**               | Throw `DuplicateResourceException` | Prevent duplicate resource creation                     |
| **Services**               | Updated `update()` signatures      | Ensure path variable IDs are always used                |
| **Controllers**            | Removed null checks                | Delegate error handling to the global exception handler |
| **Controllers**            | Use Service layer consistently     | Maintain clean architecture                             |

---

# 🧪 Testing the Changes

| Request                        | Before                    | After                                                                                             |
| ------------------------------ | ------------------------- | ------------------------------------------------------------------------------------------------- |
| `GET /students/999`            | Empty body (404)          | `{"status":404,"message":"Student not found with id: 999","timestamp":...}`                       |
| `POST /students` (existing ID) | Empty body / 500          | `{"status":409,"message":"Student already exists with id: 1","timestamp":...}`                    |
| `GET /students/abc`            | Default Spring error JSON | `{"status":400,"message":"Invalid value for parameter 'id'. Expected type: int","timestamp":...}` |
| `DELETE /teachers/999`         | Empty body (404)          | `{"status":404,"message":"Teacher not found with id: 999","timestamp":...}`                       |
| Database connection failure    | Returned `null`           | `{"status":500,"message":"Database error while fetching student with id: 1","timestamp":...}`     |

---

# 🎉 Result

The application now follows a much cleaner exception-handling architecture:

* ✅ Repositories only perform database operations.
* ✅ Services enforce business rules.
* ✅ Controllers focus only on request handling.
* ✅ `@ControllerAdvice` centralizes all exception handling.
* ✅ Every error response follows a consistent JSON format.
* ✅ The codebase is easier to maintain, test, and extend.
