package com.example.course_app.controller;

import com.example.course_app.dto.LessonDTO;
import com.example.course_app.entity.User;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.service.courses.CourseService;
import com.example.course_app.service.lessons.LessonService;
import com.example.course_app.util.LessonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 * Контроллер для работы с уроками.
 */
@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class LessonController {
    
    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);
    
    /**
     * Вспомогательный класс для приема данных с фронтенда
     */
    private static class LessonRequest {
        private String title;
        private String content;
        private String description;
        private Integer order; // Поле order с фронтенда будет преобразовано в orderNumber
        
        public String getTitle() {
            return title;
        }
        
        public String getContent() {
            return content;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Integer getOrder() {
            return order;
        }
    }

    private final LessonService lessonService;
    private final CourseService courseService;

    @Autowired
    public LessonController(LessonService lessonService, CourseService courseService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
    }

    /**
     * Получить все уроки курса.
     *
     * @param courseId идентификатор курса
     * @return список уроков курса
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourseId(@PathVariable Long courseId) {
        try {
            logger.info("Запрос на получение уроков для курса с ID: {}", courseId);
            
            // Используем транзакцию с полной загрузкой связей
            List<Lesson> lessons = lessonService.getLessonsByCourseIdOrdered(courseId);
            logger.info("Получено уроков: {}", lessons != null ? lessons.size() : 0);
            
            // Если уроков нет, возвращаем пустой список
            if (lessons == null) {
                logger.info("Список уроков пуст, возвращаем пустой список");
                return ResponseEntity.ok(List.of());
            }
            
            // Преобразуем сущности в DTO для избежания проблем с ленивой загрузкой
            List<LessonDTO> lessonDTOs = LessonMapper.toDTOList(lessons);
            logger.info("Уроки успешно преобразованы в DTO, количество: {}", lessonDTOs.size());
            return ResponseEntity.ok(lessonDTOs);
        } catch (Exception e) {
            // Логируем ошибку
            logger.error("Ошибка при получении уроков для курса с ID {}: {}", courseId, e.getMessage(), e);
            e.printStackTrace();
            // Возвращаем пустой список вместо ошибки 500
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Получить все уроки с контентом для курса.
     *
     * @param courseId идентификатор курса
     * @return список уроков с контентом
     */
    @GetMapping("/course/{courseId}/content")
    public ResponseEntity<List<LessonDTO>> getContentLessonsByCourseId(@PathVariable Long courseId) {
        try {
            List<Lesson> lessons = lessonService.getContentLessonsByCourseId(courseId);
            
            if (lessons == null) {
                return ResponseEntity.ok(List.of());
            }
            
            // Преобразуем сущности в DTO
            List<LessonDTO> lessonDTOs = LessonMapper.toDTOList(lessons);
            return ResponseEntity.ok(lessonDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Получить урок по идентификатору.
     *
     * @param id идентификатор урока
     * @return урок или статус 404, если урок не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long id) {
        try {
            Optional<Lesson> lesson = lessonService.getLessonById(id);
            
            return lesson.map(l -> ResponseEntity.ok(LessonMapper.toDTO(l)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать новый урок в курсе.
     *
     * @param lessonRequest данные урока для создания
     * @param courseId идентификатор курса
     * @param authentication данные аутентификации пользователя
     * @return созданный урок или статус 403, если пользователь не имеет прав
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<LessonDTO> createLesson(@RequestBody LessonRequest lessonRequest, 
                                              @PathVariable Long courseId,
                                              Authentication authentication) {
        try {
            logger.info("Начало создания урока для курса с ID: {}", courseId);
            logger.info("Полученные данные: title={}, content={}, description={}, order={}", 
                    lessonRequest.getTitle(), 
                    lessonRequest.getContent() != null ? lessonRequest.getContent().substring(0, Math.min(50, lessonRequest.getContent().length())) + "..." : "null", 
                    lessonRequest.getDescription() != null ? lessonRequest.getDescription().substring(0, Math.min(50, lessonRequest.getDescription().length())) + "..." : "null",
                    lessonRequest.getOrder());
            
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                logger.warn("Попытка создания урока без аутентификации");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            logger.info("Пользователь с ID: {} пытается создать урок", userId);
            
            // Проверяем, принадлежит ли курс преподавателю
            if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
                logger.warn("Пользователь с ID: {} не имеет прав на создание урока в курсе с ID: {}", userId, courseId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            logger.info("Проверка прав доступа пройдена успешно");
            
            // Преобразуем данные из запроса в сущность Lesson
            Lesson lesson = new Lesson();
            lesson.setTitle(lessonRequest.getTitle());
            lesson.setContent(lessonRequest.getContent());
            
            // Для совместимости с фронтендом можем использовать описание в будущем
            if (lessonRequest.getDescription() != null) {
                logger.info("Получено описание урока: {}", lessonRequest.getDescription());
                // В текущей реализации у Lesson нет поля description, но мы можем добавить его в будущем
            }
            
            // Устанавливаем orderNumber из поля order с фронтенда
            if (lessonRequest.getOrder() != null) {
                lesson.setOrderNumber(lessonRequest.getOrder());
                logger.info("Установлен порядковый номер урока: {}", lessonRequest.getOrder());
            } else {
                logger.info("Порядковый номер урока не указан, будет использовано значение по умолчанию");
            }
            
            logger.info("Вызов метода lessonService.createLesson для создания урока");
            Lesson createdLesson = lessonService.createLesson(lesson, courseId);
            logger.info("Урок успешно создан с ID: {}", createdLesson.getId());
            
            // Преобразуем сущность в DTO для избежания проблем с ленивой загрузкой
            LessonDTO lessonDTO = LessonMapper.toDTO(createdLesson);
            logger.info("Урок успешно преобразован в DTO и готов к отправке");
            return ResponseEntity.status(HttpStatus.CREATED).body(lessonDTO);
        } catch (Exception e) {
            logger.error("Ошибка при создании урока: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновить существующий урок.
     *
     * @param id идентификатор урока
     * @param lessonDetails обновленные данные урока
     * @param authentication данные аутентификации пользователя
     * @return обновленный урок или статус 403, если пользователь не имеет прав
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<LessonDTO> updateLesson(@PathVariable Long id, 
                                              @RequestBody Lesson lessonDetails,
                                              Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Получаем информацию о уроке
            Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
            if (lessonOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Lesson lesson = lessonOpt.get();
            Long courseId = lesson.getCourse().getId();
            
            // Проверяем, принадлежит ли курс преподавателю
            if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Lesson updatedLesson = lessonService.updateLesson(id, lessonDetails);
            
            // Преобразуем сущность в DTO
            LessonDTO lessonDTO = LessonMapper.toDTO(updatedLesson);
            return ResponseEntity.ok(lessonDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Изменить порядок урока в курсе.
     *
     * @param id идентификатор урока
     * @param newOrderNumber новый порядковый номер
     * @param authentication данные аутентификации пользователя
     * @return обновленный урок или статус 403, если пользователь не имеет прав
     */
    @PatchMapping("/{id}/order")
    public ResponseEntity<LessonDTO> changeLessonOrder(@PathVariable Long id, 
                                                  @RequestBody Integer newOrderNumber,
                                                  Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Получаем информацию о уроке
            Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
            if (lessonOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Lesson lesson = lessonOpt.get();
            Long courseId = lesson.getCourse().getId();
            
            // Проверяем, принадлежит ли курс преподавателю
            if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Lesson updatedLesson = lessonService.changeLessonOrder(id, newOrderNumber);
            
            // Преобразуем сущность в DTO
            LessonDTO lessonDTO = LessonMapper.toDTO(updatedLesson);
            return ResponseEntity.ok(lessonDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Изменить флаг контента урока.
     *
     * @param id идентификатор урока
     * @param isContent флаг контента
     * @param authentication данные аутентификации пользователя
     * @return обновленный урок или статус 403, если пользователь не имеет прав
     */
    @PatchMapping("/{id}/content-flag")
    public ResponseEntity<Lesson> changeLessonContentFlag(@PathVariable Long id, 
                                                        @RequestBody Boolean isContent,
                                                        Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получаем информацию о уроке
        Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Lesson lesson = lessonOpt.get();
        Long courseId = lesson.getCourse().getId();
        
        // Проверяем, принадлежит ли курс преподавателю
        if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Lesson updatedLesson = lessonService.changeLessonContentFlag(id, isContent);
        return ResponseEntity.ok(updatedLesson);
    }

    /**
     * Удалить урок по идентификатору.
     *
     * @param id идентификатор урока
     * @param authentication данные аутентификации пользователя
     * @return статус 204 (No Content) или статус 403, если пользователь не имеет прав
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Получаем информацию о уроке
            Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
            if (lessonOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Lesson lesson = lessonOpt.get();
            Long courseId = lesson.getCourse().getId();
            
            // Проверяем, принадлежит ли курс преподавателю
            if (!courseService.isCourseOwnedByTeacher(courseId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            lessonService.deleteLesson(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Получить ID пользователя из объекта аутентификации
     * 
     * @param authentication объект аутентификации
     * @return ID пользователя или null, если аутентификация не настроена
     */
    protected Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
