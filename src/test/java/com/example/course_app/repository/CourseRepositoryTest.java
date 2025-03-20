package com.example.course_app.repository;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Поиск курсов по ID преподавателя")
    void findByTeacherId_ShouldReturnCourses() {
        // Arrange
        User teacher = new User();
        teacher.setEmail("teacher@example.com");
        teacher.setFirstName("Иван");
        teacher.setLastName("Преподавателев");
        teacher.setPassword("password");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);
        teacher = userRepository.save(teacher);

        Course course1 = new Course();
        course1.setTitle("Java для начинающих");
        course1.setDescription("Базовый курс Java");
        course1.setTeacher(teacher);
        course1.setStatus(CourseStatus.PUBLISHED);
        entityManager.persist(course1);

        Course course2 = new Course();
        course2.setTitle("Продвинутый Java");
        course2.setDescription("Углубленный курс Java");
        course2.setTeacher(teacher);
        course2.setStatus(CourseStatus.DRAFT);
        entityManager.persist(course2);

        entityManager.flush();

        // Act
        List<Course> courses = courseRepository.findByTeacherId(teacher.getId());

        // Assert
        assertEquals(2, courses.size());
        assertTrue(courses.stream().anyMatch(c -> c.getTitle().equals("Java для начинающих")));
        assertTrue(courses.stream().anyMatch(c -> c.getTitle().equals("Продвинутый Java")));
    }

    @Test
    @DisplayName("Поиск курсов по статусу")
    void findByStatus_ShouldReturnCourses() {
        // Arrange
        User teacher = new User();
        teacher.setEmail("teacher2@example.com");
        teacher.setFirstName("Петр");
        teacher.setLastName("Учителев");
        teacher.setPassword("password");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);
        teacher = userRepository.save(teacher);

        Course course1 = new Course();
        course1.setTitle("Python для начинающих");
        course1.setDescription("Базовый курс Python");
        course1.setTeacher(teacher);
        course1.setStatus(CourseStatus.PUBLISHED);
        entityManager.persist(course1);

        Course course2 = new Course();
        course2.setTitle("Продвинутый Python");
        course2.setDescription("Углубленный курс Python");
        course2.setTeacher(teacher);
        course2.setStatus(CourseStatus.DRAFT);
        entityManager.persist(course2);

        entityManager.flush();

        // Act
        List<Course> publishedCourses = courseRepository.findByStatus(CourseStatus.PUBLISHED);
        List<Course> draftCourses = courseRepository.findByStatus(CourseStatus.DRAFT);

        // Assert
        assertTrue(publishedCourses.stream().anyMatch(c -> c.getTitle().equals("Python для начинающих")));
        assertTrue(draftCourses.stream().anyMatch(c -> c.getTitle().equals("Продвинутый Python")));
    }

    @Test
    @DisplayName("Поиск публичных опубликованных курсов")
    void findByStatusAndIsPublicTrue_ShouldReturnCourses() {
        // Arrange
        User teacher = new User();
        teacher.setEmail("teacher3@example.com");
        teacher.setFirstName("Сергей");
        teacher.setLastName("Профессоров");
        teacher.setPassword("password");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);
        teacher = userRepository.save(teacher);

        Course course1 = new Course();
        course1.setTitle("C# для начинающих");
        course1.setDescription("Базовый курс C#");
        course1.setTeacher(teacher);
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setPublic(true);
        entityManager.persist(course1);

        Course course2 = new Course();
        course2.setTitle("Продвинутый C#");
        course2.setDescription("Углубленный курс C#");
        course2.setTeacher(teacher);
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setPublic(false);
        entityManager.persist(course2);

        entityManager.flush();

        // Act
        List<Course> publicCourses = courseRepository.findByStatusAndIsPublicTrue(CourseStatus.PUBLISHED);

        // Assert
        assertEquals(1, publicCourses.size());
        assertEquals("C# для начинающих", publicCourses.get(0).getTitle());
    }

    @Test
    @DisplayName("Поиск курсов по части названия")
    void findByTitleContainingIgnoreCase_ShouldReturnCourses() {
        // Arrange
        User teacher = new User();
        teacher.setEmail("teacher4@example.com");
        teacher.setFirstName("Алексей");
        teacher.setLastName("Лекторов");
        teacher.setPassword("password");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);
        teacher = userRepository.save(teacher);

        Course course1 = new Course();
        course1.setTitle("JavaScript для начинающих");
        course1.setDescription("Базовый курс JavaScript");
        course1.setTeacher(teacher);
        entityManager.persist(course1);

        Course course2 = new Course();
        course2.setTitle("Продвинутый JavaScript");
        course2.setDescription("Углубленный курс JavaScript");
        course2.setTeacher(teacher);
        entityManager.persist(course2);

        Course course3 = new Course();
        course3.setTitle("TypeScript основы");
        course3.setDescription("Базовый курс TypeScript");
        course3.setTeacher(teacher);
        entityManager.persist(course3);

        entityManager.flush();

        // Act
        List<Course> jsCourses = courseRepository.findByTitleContainingIgnoreCase("javascript");
        List<Course> scriptCourses = courseRepository.findByTitleContainingIgnoreCase("script");

        // Assert
        assertEquals(2, jsCourses.size());
        assertEquals(3, scriptCourses.size());
    }

    @Test
    @DisplayName("Проверка существования курса по названию")
    void existsByTitle_ShouldReturnTrue_WhenCourseExists() {
        // Arrange
        User teacher = new User();
        teacher.setEmail("teacher5@example.com");
        teacher.setFirstName("Мария");
        teacher.setLastName("Учителева");
        teacher.setPassword("password");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);
        teacher = userRepository.save(teacher);

        Course course = new Course();
        course.setTitle("Уникальный курс");
        course.setDescription("Описание уникального курса");
        course.setTeacher(teacher);
        entityManager.persist(course);

        entityManager.flush();

        // Act & Assert
        assertTrue(courseRepository.existsByTitle("Уникальный курс"));
        assertFalse(courseRepository.existsByTitle("Несуществующий курс"));
    }

    @Test
    @DisplayName("Проверка существования курса по названию, исключая текущий курс")
    void existsByTitleAndIdNot_ShouldReturnFalse_WhenOnlyCurrentCourseHasTitle() {
        // Arrange
        User teacher = new User();
        teacher.setEmail("teacher6@example.com");
        teacher.setFirstName("Ольга");
        teacher.setLastName("Преподавателева");
        teacher.setPassword("password");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);
        teacher = userRepository.save(teacher);

        Course course = new Course();
        course.setTitle("Особый курс");
        course.setDescription("Описание особого курса");
        course.setTeacher(teacher);
        course = entityManager.persist(course);

        entityManager.flush();

        // Act & Assert
        assertFalse(courseRepository.existsByTitleAndIdNot("Особый курс", course.getId()));
        assertTrue(courseRepository.existsByTitleAndIdNot("Особый курс", 999L));
    }
}
