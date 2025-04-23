package com.example.course_app.service.answers;

import com.example.course_app.entity.answers.Answer;
import com.example.course_app.entity.questions.Question;
import com.example.course_app.repository.AnswerRepository;
import com.example.course_app.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с ответами на вопросы.
 */
@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public AnswerServiceImpl(
            AnswerRepository answerRepository,
            QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public Answer createAnswer(Long questionId, String text, boolean isCorrect) {
        // Находим вопрос
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + questionId + " не найден"));

        // Проверка для вопросов с одиночным выбором
        if (isCorrect && question.getType() != null && question.getType().toString().equals("SINGLE_CHOICE")) {
            // Проверяем, есть ли уже правильный ответ для этого вопроса
            List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrectTrue(questionId);
            if (!correctAnswers.isEmpty()) {
                throw new IllegalStateException(
                    "Для вопроса с одиночным выбором (ID: " + questionId + 
                    ") уже существует правильный ответ. Для вопросов типа SINGLE_CHOICE допустим только один правильный ответ."
                );
            }
        }

        // Создаем новый ответ
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setText(text);
        answer.setCorrect(isCorrect);

        // Сохраняем ответ
        return answerRepository.save(answer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Answer> getAnswerById(Long id) {
        return answerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Answer> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Answer> getCorrectAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionIdAndIsCorrectTrue(questionId);
    }

    @Override
    @Transactional
    public Answer updateAnswer(Long id, String text, boolean isCorrect) {
        // Находим существующий ответ
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Ответ с ID " + id + " не найден"));

        // Проверка для вопросов с одиночным выбором
        if (isCorrect && answer.getQuestion().getType() != null && 
            answer.getQuestion().getType().toString().equals("SINGLE_CHOICE")) {
            // Проверяем, есть ли уже другой правильный ответ для этого вопроса
            List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrectTrue(answer.getQuestion().getId());
            if (!correctAnswers.isEmpty() && !correctAnswers.get(0).getId().equals(id)) {
                throw new IllegalStateException(
                    "Для вопроса с одиночным выбором (ID: " + answer.getQuestion().getId() + 
                    ") уже существует правильный ответ. Для вопросов типа SINGLE_CHOICE допустим только один правильный ответ."
                );
            }
        }

        // Обновляем информацию об ответе
        answer.setText(text);
        answer.setCorrect(isCorrect);

        // Сохраняем обновленный ответ
        return answerRepository.save(answer);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long id) {
        // Проверяем существование ответа
        if (!answerRepository.existsById(id)) {
            throw new IllegalStateException("Ответ с ID " + id + " не найден");
        }

        // Удаляем ответ
        answerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAnswerBelongsToQuestion(Long answerId, Long questionId) {
        // Находим ответ
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalStateException("Ответ с ID " + answerId + " не найден"));

        // Проверяем принадлежность ответа вопросу
        return answer.getQuestion().getId().equals(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAnswerCorrect(Long answerId) {
        // Находим ответ
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalStateException("Ответ с ID " + answerId + " не найден"));

        // Проверяем, является ли ответ правильным
        return answer.isCorrect();
    }

    @Override
    @Transactional
    public List<Answer> createSingleChoiceAnswers(Long questionId, String correctAnswerText, List<String> incorrectAnswersText) {
        // Находим вопрос
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + questionId + " не найден"));

        List<Answer> answers = new ArrayList<>();

        // Создаем правильный ответ
        Answer correctAnswer = new Answer();
        correctAnswer.setQuestion(question);
        correctAnswer.setText(correctAnswerText);
        correctAnswer.setCorrect(true);
        answers.add(answerRepository.save(correctAnswer));

        // Создаем неправильные ответы
        for (String incorrectText : incorrectAnswersText) {
            Answer incorrectAnswer = new Answer();
            incorrectAnswer.setQuestion(question);
            incorrectAnswer.setText(incorrectText);
            incorrectAnswer.setCorrect(false);
            answers.add(answerRepository.save(incorrectAnswer));
        }

        return answers;
    }

    @Override
    @Transactional
    public List<Answer> createMultipleChoiceAnswers(Long questionId, List<String> correctAnswersText, List<String> incorrectAnswersText) {
        // Находим вопрос
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalStateException("Вопрос с ID " + questionId + " не найден"));

        List<Answer> answers = new ArrayList<>();

        // Создаем правильные ответы
        for (String correctText : correctAnswersText) {
            Answer correctAnswer = new Answer();
            correctAnswer.setQuestion(question);
            correctAnswer.setText(correctText);
            correctAnswer.setCorrect(true);
            answers.add(answerRepository.save(correctAnswer));
        }

        // Создаем неправильные ответы
        for (String incorrectText : incorrectAnswersText) {
            Answer incorrectAnswer = new Answer();
            incorrectAnswer.setQuestion(question);
            incorrectAnswer.setText(incorrectText);
            incorrectAnswer.setCorrect(false);
            answers.add(answerRepository.save(incorrectAnswer));
        }

        return answers;
    }
}
