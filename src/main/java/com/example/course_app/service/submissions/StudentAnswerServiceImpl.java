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

        System.out.println("Создание текстового ответа для вопроса ID: " + questionId + ", тип: " + question.getType());
        System.out.println("Текст ответа: " + answerText);
        
        // Проверяем, что вопрос имеет тип TEXT_INPUT или строковое представление "TEXT"
        boolean isTextInput = question.getType() == QuestionType.TEXT_INPUT || "TEXT".equals(question.getType().toString());
        
        if (!isTextInput) {
            System.out.println("Предупреждение: вопрос не является текстовым, но обрабатывается как текстовый");
        }

        // Создаем новый ответ студента
        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setSubmission(submission);
        studentAnswer.setQuestion(question);
        studentAnswer.setAnswerText(answerText);
        
        // Для текстовых вопросов устанавливаем false, пока преподаватель не проверит
        studentAnswer.setIsCorrect(false);
        studentAnswer.setScore(0); // Начальная оценка 0, пока преподаватель не проверит

        // Сохраняем ответ
        StudentAnswer savedAnswer = studentAnswerRepository.save(studentAnswer);
        System.out.println("Текстовый ответ сохранен с ID: " + savedAnswer.getId());
        
        // Добавляем ответ к отправке
        submission.addAnswer(savedAnswer);
        submissionRepository.save(submission);
        
        return savedAnswer;
    }
    
    /**
     * Создать новый ответ студента с выбранными вариантами ответа.
     * Для вопросов с выбором (одиночным или множественным).
     *
     * @param submissionId идентификатор отправки
     * @param questionId идентификатор вопроса
     * @param selectedAnswerIds список идентификаторов выбранных вариантов ответа
     * @return созданный ответ
     */
    @Override
    @Transactional
    public StudentAnswer createStudentAnswerWithSelectedOptions(Long submissionId, Long questionId, List<Long> selectedAnswerIds) {
        // Находим отправку
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalStateException("Отправка с ID " + submissionId + " не найдена"));

        // Находим вопрос
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + questionId + " не найден"));

        // Проверяем, что вопрос имеет тип с выбором
        System.out.println("Тип вопроса: " + question.getType() + ", строковое представление: " + question.getType().toString());
        
        // Проверяем тип вопроса как по енуму, так и по строковому представлению
        boolean isSingleChoice = question.getType() == QuestionType.SINGLE_CHOICE || "SINGLE_CHOICE".equals(question.getType().toString());
        boolean isMultipleChoice = question.getType() == QuestionType.MULTIPLE_CHOICE || "MULTIPLE_CHOICE".equals(question.getType().toString());
        
        if (!isSingleChoice && !isMultipleChoice) {
            throw new IllegalStateException("Вопрос с ID " + questionId + " не является вопросом с выбором. Тип вопроса: " + question.getType());
        }

        // Создаем новый ответ студента
        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setSubmission(submission);
        studentAnswer.setQuestion(question);
        
        // Формируем текст ответа из выбранных вариантов
        String answerText = selectedAnswerIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        
        studentAnswer.setAnswerText(answerText);
        
        // Проверяем правильность ответа
        boolean isCorrect = checkSelectedAnswersCorrectness(question, selectedAnswerIds);
        studentAnswer.setIsCorrect(isCorrect);
        
        // Устанавливаем оценку в зависимости от правильности ответа
        if (isCorrect) {
            // Если ответ правильный, устанавливаем полные баллы за вопрос
            studentAnswer.setScore(question.getPoints());
            System.out.println("Баллы за вопрос: " + question.getPoints());
        } else {
            studentAnswer.setScore(0);
            System.out.println("Баллы за вопрос: 0");
        }

        // Сохраняем ответ
        StudentAnswer savedAnswer = studentAnswerRepository.save(studentAnswer);
        
        // Добавляем ответ к отправке
        submission.addAnswer(savedAnswer);
        submissionRepository.save(submission);
        
        return savedAnswer;
    }
    
    /**
     * Проверить правильность выбранных вариантов ответа.
     *
     * @param question вопрос
     * @param selectedAnswerIds список идентификаторов выбранных вариантов ответа
     * @return true, если ответ правильный
     */
    private boolean checkSelectedAnswersCorrectness(Question question, List<Long> selectedAnswerIds) {
        // Получаем все варианты ответов для вопроса
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        
        System.out.println("Проверка правильности ответа для вопроса ID: " + question.getId() + ", тип: " + question.getType());
        System.out.println("Выбранные варианты: " + selectedAnswerIds);
        
        // Получаем список правильных вариантов ответов
        List<Long> correctAnswerIds = answers.stream()
                .filter(Answer::isCorrect)
                .map(Answer::getId)
                .collect(Collectors.toList());
        
        System.out.println("Правильные варианты: " + correctAnswerIds);
        
        // Проверяем тип вопроса как по енуму, так и по строковому представлению
        boolean isSingleChoice = question.getType() == QuestionType.SINGLE_CHOICE || "SINGLE_CHOICE".equals(question.getType().toString());
        boolean isMultipleChoice = question.getType() == QuestionType.MULTIPLE_CHOICE || "MULTIPLE_CHOICE".equals(question.getType().toString());
        
        if (isSingleChoice) {
            // Для вопроса с одним вариантом ответа
            // Проверяем, что выбран только один вариант и он правильный
            // Для вопросов с одиночным выбором должен быть только один правильный вариант
            if (correctAnswerIds.size() != 1) {
                System.out.println("Ошибка в данных: для вопроса с одиночным выбором указано " + correctAnswerIds.size() + " правильных вариантов");
                // Берем первый правильный вариант для проверки
                if (!correctAnswerIds.isEmpty()) {
                    Long correctId = correctAnswerIds.get(0);
                    boolean result = selectedAnswerIds.size() == 1 && selectedAnswerIds.get(0).equals(correctId);
                    System.out.println("Результат проверки одиночного выбора: " + result);
                    return result;
                }
            }
            
            boolean result = selectedAnswerIds.size() == 1 && correctAnswerIds.contains(selectedAnswerIds.get(0));
            System.out.println("Результат проверки одиночного выбора: " + result);
            return result;
        } else if (isMultipleChoice) {
            // Для вопроса с несколькими вариантами ответа
            // Проверяем, что выбраны все правильные варианты и только они (строгая проверка)
            boolean result = selectedAnswerIds.containsAll(correctAnswerIds) && correctAnswerIds.containsAll(selectedAnswerIds);
            System.out.println("Результат проверки множественного выбора: " + result);
            return result;
        }
        
        System.out.println("Неизвестный тип вопроса: " + question.getType());
        return false;
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
