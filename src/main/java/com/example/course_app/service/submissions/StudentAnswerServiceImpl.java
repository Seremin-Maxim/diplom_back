package com.example.course_app.service.submissions;

import com.example.course_app.entity.answers.Answer;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.entity.questions.QuestionType;
import com.example.course_app.entity.submissions.StudentAnswer;
import com.example.course_app.entity.submissions.StudentSubmission;
import com.example.course_app.repository.AnswerRepository;
import com.example.course_app.repository.QuestionRepository;
import com.example.course_app.repository.StudentAnswerRepository;
import com.example.course_app.repository.StudentSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с ответами студентов на вопросы.
 */
@Service
public class StudentAnswerServiceImpl implements StudentAnswerService {

    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentSubmissionRepository submissionRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Autowired
    public StudentAnswerServiceImpl(
            StudentAnswerRepository studentAnswerRepository,
            StudentSubmissionRepository submissionRepository,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository) {
        this.studentAnswerRepository = studentAnswerRepository;
        this.submissionRepository = submissionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    @Override
    @Transactional
    public StudentAnswer createStudentAnswer(Long submissionId, Long questionId, String answerText) {
        // Находим отправку
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + submissionId + " не найдена"));

        // Находим вопрос
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + questionId + " не найден"));

        // Создаем новый ответ студента
        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setSubmission(submission);
        studentAnswer.setQuestion(question);
        studentAnswer.setAnswerText(answerText);
        
        // Автоматически проверяем правильность ответа для вопросов с автоматической проверкой
        if (question.getType() == QuestionType.SINGLE_CHOICE || 
            question.getType() == QuestionType.MULTIPLE_CHOICE) {
            boolean isCorrect = checkAnswerAutomatically(question, answerText);
            studentAnswer.setIsCorrect(isCorrect);
        } else {
            // Для вопросов с ручной проверкой устанавливаем false, пока преподаватель не проверит
            studentAnswer.setIsCorrect(false);
        }

        // Сохраняем ответ
        StudentAnswer savedAnswer = studentAnswerRepository.save(studentAnswer);
        
        // Добавляем ответ к отправке
        submission.addAnswer(savedAnswer);
        submissionRepository.save(submission);
        
        return savedAnswer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentAnswer> getStudentAnswerById(Long id) {
        return studentAnswerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAnswer> getAnswersBySubmissionId(Long submissionId) {
        return studentAnswerRepository.findBySubmissionId(submissionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAnswer> getAnswersByQuestionId(Long questionId) {
        return studentAnswerRepository.findByQuestionId(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentAnswer getAnswerBySubmissionIdAndQuestionId(Long submissionId, Long questionId) {
        return studentAnswerRepository.findBySubmissionIdAndQuestionId(submissionId, questionId);
    }

    @Override
    @Transactional
    public StudentAnswer updateAnswerText(Long id, String answerText) {
        // Находим существующий ответ
        StudentAnswer studentAnswer = studentAnswerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Ответ с ID " + id + " не найден"));

        // Обновляем текст ответа
        studentAnswer.setAnswerText(answerText);
        
        // Автоматически проверяем правильность ответа для вопросов с автоматической проверкой
        Question question = studentAnswer.getQuestion();
        if (question.getType() == QuestionType.SINGLE_CHOICE || 
            question.getType() == QuestionType.MULTIPLE_CHOICE) {
            boolean isCorrect = checkAnswerAutomatically(question, answerText);
            studentAnswer.setIsCorrect(isCorrect);
        }

        // Сохраняем обновленный ответ
        return studentAnswerRepository.save(studentAnswer);
    }

    @Override
    @Transactional
    public StudentAnswer markAnswerAsCorrect(Long id, boolean isCorrect) {
        // Находим существующий ответ
        StudentAnswer studentAnswer = studentAnswerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Ответ с ID " + id + " не найден"));

        // Отмечаем ответ как правильный или неправильный
        studentAnswer.setIsCorrect(isCorrect);

        // Сохраняем обновленный ответ
        return studentAnswerRepository.save(studentAnswer);
    }

    @Override
    @Transactional
    public void deleteStudentAnswer(Long id) {
        // Проверяем существование ответа
        if (!studentAnswerRepository.existsById(id)) {
            throw new IllegalStateException("Ответ с ID " + id + " не найден");
        }
        
        // Удаляем ответ
        studentAnswerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllAnswersBySubmissionId(Long submissionId) {
        // Находим все ответы для отправки
        List<StudentAnswer> answers = studentAnswerRepository.findBySubmissionId(submissionId);
        
        // Удаляем все ответы
        if (!answers.isEmpty()) {
            studentAnswerRepository.deleteAll(answers);
        }
    }

    @Override
    @Transactional
    public boolean checkAnswerCorrectness(Long answerId) {
        // Находим ответ
        StudentAnswer studentAnswer = studentAnswerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalStateException("Ответ с ID " + answerId + " не найден"));
        
        // Получаем вопрос
        Question question = studentAnswer.getQuestion();
        
        // Проверяем правильность ответа в зависимости от типа вопроса
        boolean isCorrect = false;
        
        switch (question.getType()) {
            case SINGLE_CHOICE:
            case MULTIPLE_CHOICE:
                isCorrect = checkAnswerAutomatically(question, studentAnswer.getAnswerText());
                break;
            case TEXT_INPUT:
                // Для текстовых вопросов с коротким ответом можно сравнить с правильным ответом
                isCorrect = checkTextAnswer(question, studentAnswer.getAnswerText());
                break;
            case ESSAY:
            case CODING:
                // Для эссе и кодирования требуется ручная проверка
                // Оставляем текущее значение isCorrect
                isCorrect = studentAnswer.getIsCorrect();
                break;
            default:
                isCorrect = false;
        }
        
        // Обновляем флаг правильности
        studentAnswer.setIsCorrect(isCorrect);
        studentAnswerRepository.save(studentAnswer);
        
        return isCorrect;
    }

    @Override
    @Transactional(readOnly = true)
    public int getCorrectAnswersCount(Long submissionId) {
        // Получаем все правильные ответы для отправки
        List<StudentAnswer> correctAnswers = studentAnswerRepository.findBySubmissionIdAndIsCorrectTrue(submissionId);
        
        return correctAnswers.size();
    }
    
    /**
     * Автоматически проверяет правильность ответа для вопросов с выбором.
     * 
     * @param question вопрос
     * @param answerText текст ответа (содержит ID выбранных вариантов)
     * @return true, если ответ правильный
     */
    private boolean checkAnswerAutomatically(Question question, String answerText) {
        if (question.getType() == QuestionType.SINGLE_CHOICE) {
            // Для вопросов с одним вариантом ответа
            try {
                Long selectedAnswerId = Long.parseLong(answerText.trim());
                
                // Находим выбранный вариант ответа
                Optional<Answer> selectedAnswer = answerRepository.findById(selectedAnswerId);
                
                // Проверяем, является ли выбранный вариант правильным
                return selectedAnswer.isPresent() && selectedAnswer.get().isCorrect();
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
            // Для вопросов с множественным выбором
            try {
                // Разбиваем строку с ID на отдельные ID
                List<Long> selectedAnswerIds = Arrays.stream(answerText.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                
                // Получаем все варианты ответов для вопроса
                List<Answer> allAnswers = answerRepository.findByQuestionId(question.getId());
                
                // Получаем правильные варианты ответов
                List<Long> correctAnswerIds = allAnswers.stream()
                        .filter(Answer::isCorrect)
                        .map(Answer::getId)
                        .collect(Collectors.toList());
                
                // Проверяем, что выбраны все правильные варианты и только они
                return selectedAnswerIds.containsAll(correctAnswerIds) && 
                       correctAnswerIds.containsAll(selectedAnswerIds);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Проверяет правильность текстового ответа.
     * 
     * @param question вопрос
     * @param answerText текст ответа
     * @return true, если ответ правильный
     */
    private boolean checkTextAnswer(Question question, String answerText) {
        if (question.getType() != QuestionType.TEXT_INPUT) {
            return false;
        }
        
        // Получаем все варианты ответов для вопроса
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        
        // Проверяем, совпадает ли ответ студента с одним из правильных ответов
        // (с учетом регистра или без, в зависимости от требований)
        return answers.stream()
                .filter(Answer::isCorrect)
                .anyMatch(answer -> answer.getText().equalsIgnoreCase(answerText.trim()));
    }
}
