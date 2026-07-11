# 🎓 JPA & Hibernate Learning Project

This project is a refactor of a basic Student Management system that originally used raw JDBC. It demonstrates how to switch to JPA + Hibernate and explains exactly how the ORM (Object-Relational Mapping) paradigm works under the hood.

---

## ❓ The Problem JPA Solves

With raw JDBC, you write this every single time:
1. Open a connection
2. Write the SQL string
3. Set parameters (`ps.setInt(...)`)
4. Execute the query
5. Loop through `ResultSet`
6. Manually map columns to Java fields (`new Student(rs.getInt("id"), ...)`)

Multiply this by every entity × every operation (CRUD). That's hundreds of lines of repetitive, error-prone boilerplate code.

---

## 🧠 What is JPA & Hibernate?

**JPA** = Jakarta Persistence API (formerly Java Persistence API)
JPA is a **SPECIFICATION** — it's just a set of interfaces and rules. It says:
*"If you annotate your Java class with `@Entity`, and define a way to map fields to columns, the persistence provider will handle all database operations automatically."*

**Hibernate** = The **IMPLEMENTATION** of that specification (actual working code).

Think of it like this:
*   **JDBC** = You write SQL, you manage connections, you map results manually.
*   **JPA** = You describe WHAT your data looks like, JPA writes the SQL for you.

### How They Fit Together
```text
Your Code
  ↓
Spring Data JPA (adds repository abstractions on top of JPA)
  ↓
JPA API (interfaces)
  ↓
Hibernate (JPA implementation — generates SQL, manages sessions)
  ↓
JDBC Driver
  ↓
Database
```
*Spring Data JPA* is one more layer on top — it gives you `JpaRepository`, which provides CRUD methods without you writing ANY implementation. No class needed — just an interface.

---

## 🗺️ Understanding the Mapping Concept

In JDBC, you have two separate worlds that you manually connect:
```text
Java World                    Database World
─────────────                 ───────────────
Student class          ←→     student table
student_id field       ←→     student_id column
```

JPA automates this with annotations:
```java
@Entity                    // Tells JPA: "This represents a database table."
@Table(name = "student")  // Specifies which table this entity maps to.
public class Student {

    @Id                                           // "This field is the primary key"
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // "Auto-increment it"
    @Column(name = "student_id")                  // "Maps to 'student_id' column"
    private Integer id;                           // Java naming convention: id

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "address")
    private String address;
}
```

---

## 🏦 The Repository Layer: Spring Data JPA

### What does `JpaRepository<Student, Integer>` mean?
```text
Student  → The entity type this repository manages
Integer  → The type of the entity's @Id field
```

### What methods do you get for FREE?
```java
// ===== SAVE =====
Student save(Student entity);           // INSERT or UPDATE

// ===== FIND =====
Optional<Student> findById(Integer id); // SELECT ... WHERE id = ?
List<Student> findAll();                 // SELECT * FROM student

// ===== DELETE =====
void deleteById(Integer id);
void delete(Student entity);

// ===== COUNT / EXISTS =====
long count();                            // SELECT COUNT(*) FROM student
boolean existsById(Integer id);          // Returns true/false
```

### Derived Query Methods (Query by Method Name!)
Spring Data JPA can write queries just by reading your method names. You don't write any SQL or JPQL:
```java
// In TeacherRepository
boolean existsByEmail(String email); 
// Spring translates to: SELECT COUNT(*) > 0 FROM teacher WHERE email = ?
```

### Understanding `Optional<T>`
`findById()` returns `Optional<Student>`, not `Student`.
In raw JDBC, returning `null` caused `NullPointerException` if you forgot to check. `Optional` forces you to handle the "not found" case explicitly, which we handle in the Service layer:
```java
return repository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
```

---

## 🔗 Entity Relationships

This is where JPA truly shines. Instead of primitive IDs (`int teacher_id`) or strings (`String course`), we use real object references.

Our Domain Model:
```text
Teacher ──1:N──→ Course          A teacher teaches many courses
Course  ──M:N──→ Student        A course has many students, vice versa
Student ──1:1──→ StudentProfile  A student has one profile
```

### Complete Entity Relationship Diagram
```text
┌───────────────┐         ┌───────────────┐         ┌─────────────────┐
│    Teacher    │1      N │    Course     │ N     N │     Student     │
│───────────────│◄────────│───────────────│────────►│─────────────────│
│ id (PK)       │         │ id (PK)       │         │ id (PK)         │
│ name          │         │ name          │         │ name            │
│ email (UQ)    │         │ teacher_id(FK)│         │ age             │
│ department    │         │               │         │ address         │
└───────────────┘         └───────────────┘         └────────┬────────┘
                                                             │1
                                                             │
                                                   ┌─────────┴─────────┐
                                                   │  StudentProfile   │1
                                                   │───────────────────│
                                                   │ id (PK)           │
                                                   │ phone_number      │
                                                   │ bio               │
                                                   │ student_id(FK, UQ)│
                                                   └───────────────────┘

                              Join Table (Managed by @ManyToMany):
                              ┌────────────────┐
                              │ student_course │
                              │────────────────│
                              │ student_id(FK) │
                              │ course_id(FK)  │
                              └────────────────┘
```

### Owning Side vs Inverse Side (`mappedBy`)
The most confusing concept in JPA, but the rule is simple: **The side with the Foreign Key is the Owning Side.**

```text
Course (OWNS the relationship)     Teacher (INVERSE side)
┌─────────────────────┐           ┌─────────────────────┐
│ @ManyToOne          │           │ @OneToMany          │
│ @JoinColumn         │  ←FK here │ mappedBy = "teacher"│
│ private Teacher t;  │           │ private List<Course>│
└─────────────────────┘           └─────────────────────┘
```
Changes made only to the Inverse side are **ignored** by Hibernate. You must set the Owning side (`course.setTeacher(teacher)`) for the database to update.

### Cascade Types
What happens to children when the parent changes?
```text
CascadeType.PERSIST  → Save teacher → courses also get saved
CascadeType.MERGE    → Update teacher → courses also get updated
CascadeType.REMOVE   → Delete teacher → courses also get deleted ⚠️
CascadeType.ALL      → All of the above
```
*Note: We also use `orphanRemoval = true`. If a Course is removed from a Teacher's list, the Course is deleted entirely from the database, not just unlinked.*

---

## ⚡ Fetch Types: Lazy vs Eager Loading

When you fetch a Teacher, should JPA also fetch all their Courses?

```text
Option A (Eager):  Fetch Teacher + ALL their courses immediately
→ One big query with JOIN
→ Might fetch massive data you don't need!

Option B (Lazy):   Fetch only the Teacher first
→ Courses are loaded ONLY when you call teacher.getCourses()
→ A second query is executed when needed
```

**Default Fetch Types:**
```text
@OneToMany    → LAZY by default   (good — don't load 1000 courses accidentally)
@ManyToOne    → EAGER by default  (can be problematic! We force LAZY in our code)
@ManyToMany   → LAZY by default   (good)
@OneToOne     → EAGER by default  (can be problematic! We force LAZY in our code)
```

---

## 🛡️ The Service Layer: `@Transactional`

In the Service layer, we use Spring's `@Transactional` annotation. This is crucial for two reasons:

1. **Transaction Management**: It wraps the method in a database transaction. If something fails halfway through, the database rolls back to a safe state.
2. **Hibernate Sessions**: Lazy loading *only* works inside an open Hibernate Session (Transaction). Without `@Transactional`, calling `teacher.getCourses()` in the controller would throw a `LazyInitializationException`.

Additionally, `@Transactional` enables **Dirty Checking**. If you fetch an entity and change a field inside a transactional method, Hibernate automatically knows and runs an `UPDATE` query at the end—no `save()` required!

---

## 📊 Summary: Before vs After

| BEFORE (Raw JDBC) | AFTER (JPA + Hibernate) |
| :--- | :--- |
| `@Repository class` + raw SQL strings | `@Repository interface extends JpaRepository` |
| `DataSource` / `Connection` / `PreparedStatement` | Spring auto-generates implementation |
| `course.getCourse_id()` | `course.getId()` |
| `teacher.setTeacher_id(id)` | `teacher.setId(id)` |
| `new Course(int, String, int)` | `courseRepository.save(course)` |
| Duplicate check by auto-generated ID | Duplicate check by business key (`existsByEmail()`) |
| `findById` returns `null` → manual check | `findById` returns `Optional` → `orElseThrow()` |
| No transaction management | `@Transactional` on service class |
| Manual connection closing / resource leaks | Hibernate manages sessions automatically |
