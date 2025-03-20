package com.example.course_app.service.tests;

import com.example.course_app.entity.lessons.Lesson;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.entity.tests.TestType;
import com.example.course_app.repository.LessonRepository;
import com.example.course_app.repository.QuestionRepository;
import com.example.course_app.repository.StudentSubmissionRepository;
import com.example.course_app.repository.TestRepository;
import com.example.course_app.service.questions.QuestionService;
import com.example.course_app.service.submissions.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private TestRepository testRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private StudentSubmissionRepository studentSubmissionRepository;
    @Mock
    private QuestionService questionService;
    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private TestServiceImpl testService;

    private Lesson lesson;
    private Test test;
    private Question question;
    private StudentSubmission submission;

    @BeforeEach
    void setUp() {
        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Test Lesson");

        test = new Test();
        test.setId(1L);
        test.setTitle("Sample Test");
        test.setType(TestType.MULTIPLE_CHOICE);
        test.setRequiresManualCheck(false);
        test.setLesson(lesson);

        question = new Question();
        question.setId(1L);
        question.setTest(test);

        submission = new StudentSubmission();
        submission.setId(1L);
        submission.setTest(test);
    }

    @org.junit.jupiter.api.Test
    void createTest_Success() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        Test result = testService.createTest(lesson.getId(), "Sample Test", TestType.MULTIPLE_CHOICE, false);

        assertNotNull(result);
        assertEquals("Sample Test", result.getTitle());
        assertEquals(TestType.MULTIPLE_CHOICE, result.getType());
        assertFalse(result.isRequiresManualCheck());
        assertEquals(lesson, result.getLesson());

        verify(lessonRepository).findById(lesson.getId());
        verify(testRepository).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void getTestById_Success() {
        when(testRepository.findById(test.getId())).thenReturn(Optional.of(test));

        Optional<Test> result = testService.getTestById(test.getId());

        assertTrue(result.isPresent());
        assertEquals(test.getTitle(), result.get().getTitle());
    }

    @org.junit.jupiter.api.Test
    void getTestsByLessonId_Success() {
        List<Test> tests = Arrays.asList(test);
        when(testRepository.findByLessonId(lesson.getId())).thenReturn(tests);

        List<Test> result = testService.getTestsByLessonId(lesson.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(test.getTitle(), result.get(0).getTitle());
    }

    @org.junit.jupiter.api.Test
    void getTestsByType_Success() {
        List<Test> tests = Arrays.asList(test);
        when(testRepository.findByType(TestType.MULTIPLE_CHOICE)).thenReturn(tests);

        List<Test> result = testService.getTestsByType(TestType.MULTIPLE_CHOICE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TestType.MULTIPLE_CHOICE, result.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void updateTest_Success() {
        when(testRepository.findById(test.getId())).thenReturn(Optional.of(test));
        when(testRepository.save(any(Test.class))).thenReturn(test);

        Test result = testService.updateTest(test.getId(), "Updated Test", TestType.ESSAY, true);

        assertNotNull(result);
        assertEquals("Updated Test", result.getTitle());
        assertEquals(TestType.ESSAY, result.getType());
        assertTrue(result.isRequiresManualCheck());

        verify(testRepository).findById(test.getId());
        verify(testRepository).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void deleteTest_Success() {
        when(testRepository.findById(test.getId())).thenReturn(Optional.of(test));
        when(studentSubmissionRepository.findByTestId(test.getId()))
                .thenReturn(Arrays.asList(submission));
        when(questionRepository.findByTestId(test.getId()))
                .thenReturn(Arrays.asList(question));

        testService.deleteTest(test.getId());

        verify(submissionService).deleteSubmission(submission.getId());
        verify(questionService).deleteQuestion(question.getId());
        verify(testRepository).delete(test);
    }

    @org.junit.jupiter.api.Test
    void isTestBelongsToLesson_True() {
        when(testRepository.findById(test.getId())).thenReturn(Optional.of(test));

        boolean result = testService.isTestBelongsToLesson(test.getId(), lesson.getId());

        assertTrue(result);
    }

    @org.junit.jupiter.api.Test
    void isTestRequiresManualCheck_False() {
        when(testRepository.findById(test.getId())).thenReturn(Optional.of(test));

        boolean result = testService.isTestRequiresManualCheck(test.getId());

        assertFalse(result);
    }

    @org.junit.jupiter.api.Test
    void getQuestionCount_Success() {
        when(testRepository.existsById(test.getId())).thenReturn(true);
        when(questionRepository.countByTestId(test.getId())).thenReturn(5L);

        long result = testService.getQuestionCount(test.getId());

        assertEquals(5L, result);
    }

    @org.junit.jupiter.api.Test
    void getMaxPoints_Success() {
        when(testRepository.existsById(test.getId())).thenReturn(true);
        when(questionRepository.sumPointsByTestId(test.getId())).thenReturn(100);

        int result = testService.getMaxPoints(test.getId());

        assertEquals(100, result);
    }

    @org.junit.jupiter.api.Test
    void getMaxPoints_NoQuestions() {
        when(testRepository.existsById(test.getId())).thenReturn(true);
        when(questionRepository.sumPointsByTestId(test.getId())).thenReturn(null);

        int result = testService.getMaxPoints(test.getId());

        assertEquals(0, result);
    }

    @org.junit.jupiter.api.Test
    void createTest_LessonNotFound() {
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> 
            testService.createTest(lesson.getId(), "Sample Test", TestType.MULTIPLE_CHOICE, false)
        );

        verify(testRepository, never()).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void updateTest_TestNotFound() {
        when(testRepository.findById(test.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> 
            testService.updateTest(test.getId(), "Updated Test", TestType.ESSAY, true)
        );

        verify(testRepository, never()).save(any(Test.class));
    }

    @org.junit.jupiter.api.Test
    void getQuestionCount_TestNotFound() {
        when(testRepository.existsById(test.getId())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> 
            testService.getQuestionCount(test.getId())
        );

        verify(questionRepository, never()).countByTestId(any());
    }
}