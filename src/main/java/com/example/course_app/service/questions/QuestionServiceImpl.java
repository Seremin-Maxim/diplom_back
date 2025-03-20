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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с вопросами.
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final AnswerRepository answerRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    @Autowired
    public QuestionServiceImpl(
            QuestionRepository questionRepository,
            TestRepository testRepository,
            AnswerRepository answerRepository,
            StudentAnswerRepository studentAnswerRepository) {
        this.questionRepository = questionRepository;
        this.testRepository = testRepository;
        this.answerRepository = answerRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    @Override
    @Transactional
    public Question createQuestion(Long testId, String text, QuestionType type, Integer points) {
        // Находим тест
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalStateException("Тест с ID " + testId + " не найден"));

        // Создаем новый вопрос
        Question question = new Question();
        question.setTest(test);
        question.setText(text);
        question.setType(type);
        
        // Устанавливаем количество баллов (если не указано, используем значение по умолчанию)
        if (points != null && points > 0) {
            question.setPoints(points);
        }

        // Сохраняем вопрос
        return questionRepository.save(question);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByTestId(Long testId) {
        return questionRepository.findByTestId(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByType(QuestionType type) {
        return questionRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByTestIdAndType(Long testId, QuestionType type) {
        return questionRepository.findByTestIdAndType(testId, type);
    }

    @Override
    @Transactional
    public Question updateQuestion(Long id, String text, QuestionType type, Integer points) {
        // Находим существующий вопрос
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + id + " не найден"));

        // Обновляем информацию о вопросе
        question.setText(text);
        question.setType(type);
        
        // Обновляем количество баллов, если указано положительное значение
        if (points != null && points > 0) {
            question.setPoints(points);
        }

        // Сохраняем обновленный вопрос
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        // Находим вопрос
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + id + " не найден"));
        
        // Получаем все варианты ответов для вопроса
        List<Answer> answers = answerRepository.findByQuestionId(id);
        
        // Удаляем все варианты ответов
        if (!answers.isEmpty()) {
            answerRepository.deleteAll(answers);
        }
        
        // Получаем все ответы студентов на этот вопрос
        List<StudentAnswer> studentAnswers = studentAnswerRepository.findByQuestionId(id);
        
        // Удаляем все ответы студентов
        if (!studentAnswers.isEmpty()) {
            studentAnswerRepository.deleteAll(studentAnswers);
        }
        
        // Удаляем вопрос
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isQuestionBelongsToTest(Long questionId, Long testId) {
        // Находим вопрос
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + questionId + " не найден"));

        // Проверяем принадлежность вопроса тесту
        return question.getTest().getId().equals(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCorrectAnswersCount(Long questionId) {
        // Проверяем существование вопроса
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalStateException("Вопрос с ID " + questionId + " не найден");
        }

        // Получаем количество правильных ответов для вопроса
        return answerRepository.findByQuestionIdAndIsCorrectTrue(questionId).size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return questionRepository.existsById(id);
    }
}
