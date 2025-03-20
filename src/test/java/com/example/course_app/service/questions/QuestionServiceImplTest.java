package com.example.course_app.service.questions;

import com.example.course_app.entity.answers.Answer;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.questions.QuestionType;
import com.example.course_app.entity.submissions.StudentAnswer;
import com.example.course_app.entity.tests.Test;
import com.example.course_app.repository.AnswerRepository;
import com.example.course_app.repository.QuestionRepository;
import com.example.course_app.repository.StudentAnswerRepository;
import com.example.course_app.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Test testEntity;
    private Question question;
    private Answer answer;
    private StudentAnswer studentAnswer;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        testEntity = new Test();
        testEntity.setId(1L);
        testEntity.setTitle("Тестовый тест");

        question = new Question();
        question.setId(1L);
        question.setTest(testEntity);
        question.setText("Тестовый вопрос");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(2);

        answer = new Answer();
        answer.setId(1L);
        answer.setQuestion(question);
        answer.setText("Тестовый ответ");
        answer.setCorrect(true);

        studentAnswer = new StudentAnswer();
        studentAnswer.setId(1L);
        studentAnswer.setQuestion(question);
    }

    @Nested
    @DisplayName("Тесты для метода createQuestion")
    class CreateQuestionTests {

        @org.junit.jupiter.api.Test
        @DisplayName("Создание вопроса с указанными параметрами")
        void createQuestion_WithValidData_ShouldCreateAndReturnQuestion() {
            // Подготовка
            when(testRepository.findById(anyLong())).thenReturn(Optional.of(testEntity));
            when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Действие
            Question result = questionService.createQuestion(1L, "Новый вопрос", QuestionType.MULTIPLE_CHOICE, 3);

            // Проверка
            assertNotNull(result);
            assertEquals("Новый вопрос", result.getText());
            assertEquals(QuestionType.MULTIPLE_CHOICE, result.getType());
            assertEquals(3, result.getPoints());
            assertEquals(testEntity, result.getTest());

            // Проверка вызовов методов репозитория
            verify(testRepository).findById(1L);
            verify(questionRepository).save(any(Question.class));
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Создание вопроса без указания баллов (должно использоваться значение по умолчанию)")
        void createQuestion_WithoutPoints_ShouldUseDefaultPoints() {
            // Подготовка
            when(testRepository.findById(anyLong())).thenReturn(Optional.of(testEntity));
            when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Действие
            Question result = questionService.createQuestion(1L, "Новый вопрос", QuestionType.TEXT_INPUT, null);

            // Проверка
            assertNotNull(result);
            assertEquals(1, result.getPoints()); // Значение по умолчанию из сущности Question

            // Проверка вызовов методов репозитория
            verify(testRepository).findById(1L);
            verify(questionRepository).save(any(Question.class));
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Создание вопроса с отрицательным количеством баллов (должно использоваться значение по умолчанию)")
        void createQuestion_WithNegativePoints_ShouldUseDefaultPoints() {
            // Подготовка
            when(testRepository.findById(anyLong())).thenReturn(Optional.of(testEntity));
            when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Действие
            Question result = questionService.createQuestion(1L, "Новый вопрос", QuestionType.ESSAY, -5);

            // Проверка
            assertNotNull(result);
            assertEquals(1, result.getPoints()); // Значение по умолчанию из сущности Question

            // Проверка вызовов методов репозитория
            verify(testRepository).findById(1L);
            verify(questionRepository).save(any(Question.class));
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Создание вопроса для несуществующего теста (должно выбросить исключение)")
        void createQuestion_WithNonExistentTest_ShouldThrowException() {
            // Подготовка
            when(testRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Действие и проверка
            assertThrows(IllegalStateException.class, () -> 
                questionService.createQuestion(999L, "Новый вопрос", QuestionType.SINGLE_CHOICE, 2));

            // Проверка вызовов методов репозитория
            verify(testRepository).findById(999L);
            verify(questionRepository, never()).save(any(Question.class));
        }
    }

    @Nested
    @DisplayName("Тесты для методов получения вопросов")
    class GetQuestionTests {

        @org.junit.jupiter.api.Test
        @DisplayName("Получение вопроса по ID")
        void getQuestionById_WithExistingId_ShouldReturnQuestion() {
            // Подготовка
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

            // Действие
            Optional<Question> result = questionService.getQuestionById(1L);

            // Проверка
            assertTrue(result.isPresent());
            assertEquals(question, result.get());
            verify(questionRepository).findById(1L);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Получение несуществующего вопроса по ID")
        void getQuestionById_WithNonExistingId_ShouldReturnEmptyOptional() {
            // Подготовка
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Действие
            Optional<Question> result = questionService.getQuestionById(999L);

            // Проверка
            assertFalse(result.isPresent());
            verify(questionRepository).findById(999L);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Получение вопросов по ID теста")
        void getQuestionsByTestId_ShouldReturnQuestionsList() {
            // Подготовка
            List<Question> questions = Arrays.asList(question);
            when(questionRepository.findByTestId(1L)).thenReturn(questions);

            // Действие
            List<Question> result = questionService.getQuestionsByTestId(1L);

            // Проверка
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(question, result.get(0));
            verify(questionRepository).findByTestId(1L);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Получение вопросов по типу")
        void getQuestionsByType_ShouldReturnQuestionsList() {
            // Подготовка
            List<Question> questions = Arrays.asList(question);
            when(questionRepository.findByType(QuestionType.SINGLE_CHOICE)).thenReturn(questions);

            // Действие
            List<Question> result = questionService.getQuestionsByType(QuestionType.SINGLE_CHOICE);

            // Проверка
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(question, result.get(0));
            verify(questionRepository).findByType(QuestionType.SINGLE_CHOICE);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Получение вопросов по ID теста и типу")
        void getQuestionsByTestIdAndType_ShouldReturnQuestionsList() {
            // Подготовка
            List<Question> questions = Arrays.asList(question);
            when(questionRepository.findByTestIdAndType(1L, QuestionType.SINGLE_CHOICE)).thenReturn(questions);

            // Действие
            List<Question> result = questionService.getQuestionsByTestIdAndType(1L, QuestionType.SINGLE_CHOICE);

            // Проверка
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(question, result.get(0));
            verify(questionRepository).findByTestIdAndType(1L, QuestionType.SINGLE_CHOICE);
        }
    }

    @Nested
    @DisplayName("Тесты для метода updateQuestion")
    class UpdateQuestionTests {

        @org.junit.jupiter.api.Test
        @DisplayName("Обновление вопроса с корректными данными")
        void updateQuestion_WithValidData_ShouldUpdateAndReturnQuestion() {
            // Подготовка
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Действие
            Question result = questionService.updateQuestion(1L, "Обновленный вопрос", QuestionType.MULTIPLE_CHOICE, 5);

            // Проверка
            assertNotNull(result);
            assertEquals("Обновленный вопрос", result.getText());
            assertEquals(QuestionType.MULTIPLE_CHOICE, result.getType());
            assertEquals(5, result.getPoints());
            verify(questionRepository).findById(1L);
            verify(questionRepository).save(question);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Обновление вопроса без указания баллов (должны остаться прежние)")
        void updateQuestion_WithoutPoints_ShouldKeepOriginalPoints() {
            // Подготовка
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Действие
            Question result = questionService.updateQuestion(1L, "Обновленный вопрос", QuestionType.TEXT_INPUT, null);

            // Проверка
            assertNotNull(result);
            assertEquals("Обновленный вопрос", result.getText());
            assertEquals(QuestionType.TEXT_INPUT, result.getType());
            assertEquals(2, result.getPoints()); // Должны остаться прежние баллы
            verify(questionRepository).findById(1L);
            verify(questionRepository).save(question);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Обновление несуществующего вопроса (должно выбросить исключение)")
        void updateQuestion_WithNonExistingId_ShouldThrowException() {
            // Подготовка
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Действие и проверка
            assertThrows(IllegalStateException.class, () -> 
                questionService.updateQuestion(999L, "Обновленный вопрос", QuestionType.SINGLE_CHOICE, 3));

            // Проверка вызовов методов репозитория
            verify(questionRepository).findById(999L);
            verify(questionRepository, never()).save(any(Question.class));
        }
    }

    @Nested
    @DisplayName("Тесты для метода deleteQuestion")
    class DeleteQuestionTests {

        @org.junit.jupiter.api.Test
        @DisplayName("Удаление вопроса с ответами и ответами студентов")
        void deleteQuestion_WithAnswersAndStudentAnswers_ShouldDeleteAll() {
            // Подготовка
            List<Answer> answers = Arrays.asList(answer);
            List<StudentAnswer> studentAnswers = Arrays.asList(studentAnswer);

            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(answerRepository.findByQuestionId(1L)).thenReturn(answers);
            when(studentAnswerRepository.findByQuestionId(1L)).thenReturn(studentAnswers);

            // Действие
            questionService.deleteQuestion(1L);

            // Проверка вызовов методов репозитория
            verify(questionRepository).findById(1L);
            verify(answerRepository).findByQuestionId(1L);
            verify(answerRepository).deleteAll(answers);
            verify(studentAnswerRepository).findByQuestionId(1L);
            verify(studentAnswerRepository).deleteAll(studentAnswers);
            verify(questionRepository).delete(question);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Удаление вопроса без ответов и ответов студентов")
        void deleteQuestion_WithoutAnswersAndStudentAnswers_ShouldDeleteOnlyQuestion() {
            // Подготовка
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(answerRepository.findByQuestionId(1L)).thenReturn(new ArrayList<>());
            when(studentAnswerRepository.findByQuestionId(1L)).thenReturn(new ArrayList<>());

            // Действие
            questionService.deleteQuestion(1L);

            // Проверка вызовов методов репозитория
            verify(questionRepository).findById(1L);
            verify(answerRepository).findByQuestionId(1L);
            verify(answerRepository, never()).deleteAll(anyList());
            verify(studentAnswerRepository).findByQuestionId(1L);
            verify(studentAnswerRepository, never()).deleteAll(anyList());
            verify(questionRepository).delete(question);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Удаление несуществующего вопроса (должно выбросить исключение)")
        void deleteQuestion_WithNonExistingId_ShouldThrowException() {
            // Подготовка
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Действие и проверка
            assertThrows(IllegalStateException.class, () -> questionService.deleteQuestion(999L));

            // Проверка вызовов методов репозитория
            verify(questionRepository).findById(999L);
            verify(answerRepository, never()).findByQuestionId(anyLong());
            verify(answerRepository, never()).deleteAll(anyList());
            verify(studentAnswerRepository, never()).findByQuestionId(anyLong());
            verify(studentAnswerRepository, never()).deleteAll(anyList());
            verify(questionRepository, never()).delete(any(Question.class));
        }
    }

    @Nested
    @DisplayName("Тесты для метода isQuestionBelongsToTest")
    class IsQuestionBelongsToTestTests {

        @org.junit.jupiter.api.Test
        @DisplayName("Проверка принадлежности вопроса к тесту (положительный случай)")
        void isQuestionBelongsToTest_WhenQuestionBelongsToTest_ShouldReturnTrue() {
            // Подготовка
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

            // Действие
            boolean result = questionService.isQuestionBelongsToTest(1L, 1L);

            // Проверка
            assertTrue(result);
            verify(questionRepository).findById(1L);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Проверка принадлежности вопроса к тесту (отрицательный случай)")
        void isQuestionBelongsToTest_WhenQuestionDoesNotBelongToTest_ShouldReturnFalse() {
            // Подготовка
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

            // Действие
            boolean result = questionService.isQuestionBelongsToTest(1L, 2L);

            // Проверка
            assertFalse(result);
            verify(questionRepository).findById(1L);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Проверка принадлежности несуществующего вопроса к тесту (должно выбросить исключение)")
        void isQuestionBelongsToTest_WithNonExistingQuestion_ShouldThrowException() {
            // Подготовка
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Действие и проверка
            assertThrows(IllegalStateException.class, () -> questionService.isQuestionBelongsToTest(999L, 1L));

            // Проверка вызовов методов репозитория
            verify(questionRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Тесты для метода getCorrectAnswersCount")
    class GetCorrectAnswersCountTests {

        @org.junit.jupiter.api.Test
        @DisplayName("Получение количества правильных ответов для вопроса")
        void getCorrectAnswersCount_ShouldReturnCorrectCount() {
            // Подготовка
            when(questionRepository.existsById(1L)).thenReturn(true);
            when(answerRepository.findByQuestionIdAndIsCorrectTrue(1L)).thenReturn(Arrays.asList(answer));

            // Действие
            long result = questionService.getCorrectAnswersCount(1L);

            // Проверка
            assertEquals(1, result);
            verify(questionRepository).existsById(1L);
            verify(answerRepository).findByQuestionIdAndIsCorrectTrue(1L);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Получение количества правильных ответов для вопроса без правильных ответов")
        void getCorrectAnswersCount_WhenNoCorrectAnswers_ShouldReturnZero() {
            // Подготовка
            when(questionRepository.existsById(1L)).thenReturn(true);
            when(answerRepository.findByQuestionIdAndIsCorrectTrue(1L)).thenReturn(new ArrayList<>());

            // Действие
            long result = questionService.getCorrectAnswersCount(1L);

            // Проверка
            assertEquals(0, result);
            verify(questionRepository).existsById(1L);
            verify(answerRepository).findByQuestionIdAndIsCorrectTrue(1L);
        }

        @org.junit.jupiter.api.Test
        @DisplayName("Получение количества правильных ответов для несуществующего вопроса (должно выбросить исключение)")
        void getCorrectAnswersCount_WithNonExistingQuestion_ShouldThrowException() {
            // Подготовка
            when(questionRepository.existsById(999L)).thenReturn(false);

            // Действие и проверка
            assertThrows(IllegalStateException.class, () -> questionService.getCorrectAnswersCount(999L));

            // Проверка вызовов методов репозитория
            verify(questionRepository).existsById(999L);
            verify(answerRepository, never()).findByQuestionIdAndIsCorrectTrue(anyLong());
        }
    }
}
