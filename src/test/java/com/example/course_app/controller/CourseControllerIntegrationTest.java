package com.example.course_app.controller;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.service.courses.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private CourseService courseService;

    private User teacher;
    private Course course;
    private List<Course> courses;

    @BeforeEach
    void setUp() {
        // Настраиваем ObjectMapper для игнорирования проблемных полей
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(User.class, UserMixIn.class);
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@example.com");
        teacher.setFirstName("Иван");
        teacher.setLastName("Преподавателев");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);

        course = new Course();
        course.setId(1L);
        course.setTitle("Программирование на Java");
        course.setDescription("Курс по основам Java");
        course.setTeacher(teacher);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublic(true);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Python для начинающих");
        course2.setDescription("Базовый курс Python");
        course2.setTeacher(teacher);
        course2.setStatus(CourseStatus.DRAFT);
        course2.setPublic(false);

        courses = Arrays.asList(course, course2);
    }

    @Test
    @DisplayName("Получение всех курсов")
    @WithMockUser(roles = "ADMIN")
    void getAllCourses_ShouldReturnAllCourses() throws Exception {
        // Arrange
        when(courseService.getAllCourses()).thenReturn(courses);

        // Act & Assert
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Программирование на Java")))
                .andExpect(jsonPath("$[1].title", is("Python для начинающих")));

        verify(courseService).getAllCourses();
    }

    @Test
    @DisplayName("Получение курса по ID")
    @WithMockUser(roles = "USER")
    void getCourseById_ShouldReturnCourse() throws Exception {
        // Arrange
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        // Act & Assert
        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Программирование на Java")))
                .andExpect(jsonPath("$.description", is("Курс по основам Java")))
                .andExpect(jsonPath("$.status", is("PUBLISHED")))
                .andExpect(jsonPath("$.public", is(true)));

        verify(courseService).getCourseById(1L);
    }

    @Test
    @DisplayName("Получение несуществующего курса должно вернуть 404")
    @WithMockUser(roles = "USER")
    void getCourseById_NonExistentCourse_ShouldReturn404() throws Exception {
        // Arrange
        when(courseService.getCourseById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/courses/999"))
                .andExpect(status().isNotFound());

        verify(courseService).getCourseById(999L);
    }

    @Test
    @DisplayName("Создание курса")
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void createCourse_ShouldCreateAndReturnCourse() throws Exception {
        // Arrange
        when(courseService.createCourse(any(Course.class), anyLong())).thenReturn(course);

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(teacher))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Программирование на Java")))
                .andExpect(jsonPath("$.description", is("Курс по основам Java")));

        verify(courseService).createCourse(any(Course.class), anyLong());
    }

    @Test
    @DisplayName("Обновление курса")
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void updateCourse_ShouldUpdateAndReturnCourse() throws Exception {
        // Arrange
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Обновленный курс Java");
        updatedCourse.setDescription("Обновленное описание");
        
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        when(courseService.updateCourse(anyLong(), any(Course.class))).thenReturn(updatedCourse);

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(teacher))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Обновленный курс Java")))
                .andExpect(jsonPath("$.description", is("Обновленное описание")));

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService).updateCourse(1L, updatedCourse);
    }

    @Test
    @DisplayName("Обновление чужого курса должно вернуть 403")
    @WithMockUser(username = "other@example.com", roles = "TEACHER")
    void updateCourse_NotOwnedByTeacher_ShouldReturn403() throws Exception {
        // Arrange
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Обновленный курс Java");
        
        User otherTeacher = new User();
        otherTeacher.setId(2L);
        otherTeacher.setEmail("other@example.com");
        otherTeacher.setRole(com.example.course_app.entity.Role.TEACHER);
        
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(otherTeacher))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isForbidden());

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService, never()).updateCourse(anyLong(), any(Course.class));
    }

    @Test
    @DisplayName("Удаление курса")
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void deleteCourse_ShouldDeleteCourse() throws Exception {
        // Arrange
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        doNothing().when(courseService).deleteCourse(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/courses/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(teacher)))
                .andExpect(status().isNoContent());

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService).deleteCourse(1L);
    }

    @Test
    @DisplayName("Удаление чужого курса должно вернуть 403")
    @WithMockUser(username = "other@example.com", roles = "TEACHER")
    void deleteCourse_NotOwnedByTeacher_ShouldReturn403() throws Exception {
        // Arrange
        User otherTeacher = new User();
        otherTeacher.setId(2L);
        otherTeacher.setEmail("other@example.com");
        otherTeacher.setRole(com.example.course_app.entity.Role.TEACHER);
        
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/courses/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(otherTeacher)))
                .andExpect(status().isForbidden());

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService, never()).deleteCourse(anyLong());
    }

    @Test
    @DisplayName("Изменение статуса курса")
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void changeCourseStatus_ShouldUpdateStatus() throws Exception {
        // Arrange
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        when(courseService.changeCourseStatus(anyLong(), any(CourseStatus.class))).thenReturn(course);

        // Act & Assert
        mockMvc.perform(patch("/api/courses/1/status")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(teacher))
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"PUBLISHED\""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("PUBLISHED")));

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService).changeCourseStatus(1L, CourseStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Изменение публичности курса")
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void changeCoursePublicity_ShouldUpdatePublicity() throws Exception {
        // Arrange
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        when(courseService.changeCoursePublicity(anyLong(), anyBoolean())).thenReturn(course);

        // Act & Assert
        mockMvc.perform(patch("/api/courses/1/publicity")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(teacher))
                .contentType(MediaType.APPLICATION_JSON)
                .content("true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.public", is(true)));

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService).changeCoursePublicity(1L, true);
    }

    @Test
    @DisplayName("Получение курсов преподавателя")
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void getTeacherCourses_ShouldReturnTeacherCourses() throws Exception {
        // Arrange
        when(courseService.getCoursesByTeacherId(anyLong())).thenReturn(courses);

        // Act & Assert
        mockMvc.perform(get("/api/courses/teacher/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Программирование на Java")))
                .andExpect(jsonPath("$[1].title", is("Python для начинающих")));

        verify(courseService).getCoursesByTeacherId(1L);
    }

    @Test
    @DisplayName("Получение публичных курсов")
    @WithMockUser(roles = "USER")
    void getPublicCourses_ShouldReturnPublicCourses() throws Exception {
        // Arrange
        when(courseService.getPublicCourses()).thenReturn(List.of(course));

        // Act & Assert
        mockMvc.perform(get("/api/courses/public"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Программирование на Java")))
                .andExpect(jsonPath("$[0].public", is(true)));

        verify(courseService).getPublicCourses();
    }
}
