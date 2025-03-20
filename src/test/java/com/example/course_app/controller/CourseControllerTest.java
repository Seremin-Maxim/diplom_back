package com.example.course_app.controller;

import com.example.course_app.entity.User;
import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.courses.CourseStatus;
import com.example.course_app.service.courses.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private User teacher;
    private Course course;
    private List<Course> courses;

    @BeforeEach
    void setUp() {
        // Настраиваем ObjectMapper для игнорирования проблемных полей
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(User.class, UserMixIn.class);
        
        mockMvc = MockMvcBuilders
                .standaloneSetup(courseController)
                .build();
        
        // Настраиваем аутентификацию для тестов
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@example.com");
        teacher.setFirstName("Иван");
        teacher.setLastName("Преподавателев");
        teacher.setRole(com.example.course_app.entity.Role.TEACHER);
        
        // Настраиваем аутентификацию
        Authentication authentication = new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

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
    void createCourse_ShouldCreateAndReturnCourse() throws Exception {
        // Arrange
        when(courseService.createCourse(any(Course.class), anyLong())).thenReturn(course);

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                .with(request -> {
                    request.setUserPrincipal(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
                    return request;
                })
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
    void updateCourse_ShouldUpdateAndReturnCourse() throws Exception {
        // Arrange
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Обновленный курс Java");
        updatedCourse.setDescription("Обновленное описание");
        
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        when(courseService.updateCourse(anyLong(), any(Course.class))).thenReturn(updatedCourse);

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .with(request -> {
                    request.setUserPrincipal(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
                    return request;
                })
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
    void updateCourse_NotOwnedByTeacher_ShouldReturn403() throws Exception {
        // Arrange
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Обновленный курс Java");
        
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .with(request -> {
                    request.setUserPrincipal(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isForbidden());

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService, never()).updateCourse(anyLong(), any(Course.class));
    }

    @Test
    @DisplayName("Удаление курса")
    void deleteCourse_ShouldDeleteCourse() throws Exception {
        // Arrange
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        doNothing().when(courseService).deleteCourse(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/courses/1")
                .with(request -> {
                    request.setUserPrincipal(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
                    return request;
                }))
                .andExpect(status().isNoContent());

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService).deleteCourse(1L);
    }

    @Test
    @DisplayName("Удаление чужого курса должно вернуть 403")
    void deleteCourse_NotOwnedByTeacher_ShouldReturn403() throws Exception {
        // Arrange
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/courses/1")
                .with(request -> {
                    request.setUserPrincipal(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
                    return request;
                }))
                .andExpect(status().isForbidden());

        verify(courseService).isCourseOwnedByTeacher(anyLong(), anyLong());
        verify(courseService, never()).deleteCourse(anyLong());
    }

    @Test
    @DisplayName("Изменение статуса курса")
    void changeCourseStatus_ShouldUpdateStatus() throws Exception {
        // Arrange
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        when(courseService.changeCourseStatus(anyLong(), any(CourseStatus.class))).thenReturn(course);

        // Act & Assert
        mockMvc.perform(patch("/api/courses/1/status")
                .with(request -> {
                    request.setUserPrincipal(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
                    return request;
                })
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
    void changeCoursePublicity_ShouldUpdatePublicity() throws Exception {
        // Arrange
        when(courseService.isCourseOwnedByTeacher(anyLong(), anyLong())).thenReturn(true);
        when(courseService.changeCoursePublicity(anyLong(), anyBoolean())).thenReturn(course);

        // Act & Assert
        mockMvc.perform(patch("/api/courses/1/publicity")
                .with(request -> {
                    request.setUserPrincipal(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
                    return request;
                })
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
