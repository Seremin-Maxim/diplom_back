package com.example.course_app.service.lessons;

import com.example.course_app.entity.courses.Course;
import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.repository.CourseRepository;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.service.tests.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с уроками.
 */
@Service
public class LessonServiceImpl implements LessonService {
    
    private static final Logger logger = LoggerFactory.getLogger(LessonServiceImpl.class);

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final TestRepository testRepository;
    private final TestService testService;

    @Autowired
    public LessonServiceImpl(
            LessonRepository lessonRepository, 
            CourseRepository courseRepository,
            TestRepository testRepository,
            TestService testService) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.testRepository = testRepository;
        this.testService = testService;
    }

    @Override
    @Transactional
    public Lesson createLesson(Lesson lesson, Long courseId) {
        logger.info("Начало создания урока для курса с ID: {}", courseId);
        
        try {
            // Находим курс
            logger.info("Поиск курса с ID: {}", courseId);
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> {
                        logger.error("Курс с ID {} не найден", courseId);
                        return new IllegalArgumentException("Курс с ID " + courseId + " не найден");
                    });
            logger.info("Курс с ID: {} найден: {}", courseId, course.getTitle());
    
            // Устанавливаем курс для урока
            lesson.setCourse(course);
            logger.info("Курс установлен для урока");
            
            // Если порядковый номер не установлен, устанавливаем следующий доступный
            if (lesson.getOrderNumber() == null) {
                int nextOrderNumber = getNextOrderNumber(courseId);
                lesson.setOrderNumber(nextOrderNumber);
                logger.info("Автоматически установлен порядковый номер: {}", nextOrderNumber);
            } else {
                logger.info("Используется предоставленный порядковый номер: {}", lesson.getOrderNumber());
            }
    
            // Сохраняем урок
            logger.info("Сохранение урока в базу данных");
            Lesson savedLesson = lessonRepository.save(lesson);
            logger.info("Урок успешно сохранен с ID: {}", savedLesson.getId());
            
            return savedLesson;
        } catch (Exception e) {
            logger.error("Ошибка при создании урока: {}", e.getMessage(), e);
            throw e; // Перебрасываем исключение дальше, чтобы оно было обработано в контроллере
        }
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long id, Lesson lessonDetails) {
        // Находим существующий урок
        Lesson existingLesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Урок с ID " + id + " не найден"));

        // Обновляем данные урока
        existingLesson.setTitle(lessonDetails.getTitle());
        existingLesson.setContent(lessonDetails.getContent());
        existingLesson.setTimeLimit(lessonDetails.getTimeLimit());
        
        // Не обновляем порядковый номер и флаг контента через этот метод
        // Для этого есть отдельные методы

        // Сохраняем обновленный урок
        return lessonRepository.save(existingLesson);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lesson> getLessonById(Long id) {
        // Используем метод с EntityGraph для загрузки связанных сущностей
        return lessonRepository.findLessonWithRelationsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByCourseId(Long courseId) {
        return lessonRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByCourseIdOrdered(Long courseId) {
        logger.info("Запрос на получение уроков курса с ID: {} в порядке возрастания", courseId);
        try {
            List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderNumberAsc(courseId);
            logger.info("Найдено уроков: {}", lessons != null ? lessons.size() : 0);
            return lessons;
        } catch (Exception e) {
            logger.error("Ошибка при получении уроков курса: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> getContentLessonsByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdAndIsContentTrue(courseId);
    }

    @Override
    @Transactional
    public Lesson changeLessonOrder(Long id, Integer newOrderNumber) {
        // Находим существующий урок
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Урок с ID " + id + " не найден"));

        // Обновляем порядковый номер урока
        lesson.setOrderNumber(newOrderNumber);

        // Сохраняем обновленный урок
        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public Lesson changeLessonContentFlag(Long id, boolean isContent) {
        // Находим существующий урок
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Урок с ID " + id + " не найден"));

        // Обновляем флаг контента урока
        lesson.setIsContent(isContent);

        // Сохраняем обновленный урок
        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        // Проверяем, существует ли урок
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Урок с ID " + id + " не найден"));
        
        // Получаем все тесты для урока
        List<Test> tests = testRepository.findByLessonId(id);
        
        // Удаляем все тесты (и связанные с ними вопросы, ответы и отправки)
        if (!tests.isEmpty()) {
            for (Test test : tests) {
                testService.deleteTest(test.getId());
            }
        }

        // Удаляем урок
        lessonRepository.delete(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return lessonRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLessonBelongsToCourse(Long lessonId, Long courseId) {
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        
        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            return lesson.getCourse().getId().equals(courseId);
        }
        
        return false;
    }

    /**
     * Получить следующий доступный порядковый номер для урока в курсе
     * 
     * @param courseId идентификатор курса
     * @return следующий порядковый номер
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getNextOrderNumber(Long courseId) {
        logger.info("Получение следующего порядкового номера для курса с ID: {}", courseId);
        // Получаем максимальный порядковый номер урока в курсе и добавляем 1
        Integer maxOrderNumber = lessonRepository.findMaxOrderNumberByCourseId(courseId);
        logger.info("Максимальный текущий порядковый номер: {}", maxOrderNumber);
        return maxOrderNumber + 1;
    }
}
