package com.example.course_app.dto;

import com.example.course_app.entity.questions.Question;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для преобразования сущности Question в DTO и обратно
 */
public class QuestionMapper {

    /**
     * Преобразует сущность Question в QuestionDTO
     * 
     * @param question сущность Question
     * @return объект QuestionDTO
     */
    public static QuestionDTO toDTO(Question question) {
        if (question == null) {
            return null;
        }
        
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setType(question.getType());
        dto.setPoints(question.getPoints());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        
        if (question.getTest() != null) {
            dto.setTestId(question.getTest().getId());
        }
        
        return dto;
    }
    
    /**
     * Преобразует список сущностей Question в список QuestionDTO
     * 
     * @param questions список сущностей Question
     * @return список объектов QuestionDTO
     */
    public static List<QuestionDTO> toDTOList(List<Question> questions) {
        return questions.stream()
                .map(QuestionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
