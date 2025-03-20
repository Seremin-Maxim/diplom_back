package com.example.course_app.dto;

import com.example.course_app.entity.answers.Answer;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для преобразования между сущностями Answer и DTO.
 */
public class AnswerMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Преобразовать сущность Answer в DTO.
     *
     * @param answer сущность Answer
     * @return объект AnswerDTO
     */
    public static AnswerDTO toDTO(Answer answer) {
        if (answer == null) {
            return null;
        }

        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setText(answer.getText());
        dto.setCorrect(answer.isCorrect());
        
        if (answer.getCreatedAt() != null) {
            dto.setCreatedAt(answer.getCreatedAt().format(formatter));
        }
        
        if (answer.getUpdatedAt() != null) {
            dto.setUpdatedAt(answer.getUpdatedAt().format(formatter));
        }
        
        return dto;
    }

    /**
     * Преобразовать список сущностей Answer в список DTO.
     *
     * @param answers список сущностей Answer
     * @return список объектов AnswerDTO
     */
    public static List<AnswerDTO> toDTOList(List<Answer> answers) {
        return answers.stream()
                .map(AnswerMapper::toDTO)
                .collect(Collectors.toList());
    }
}
