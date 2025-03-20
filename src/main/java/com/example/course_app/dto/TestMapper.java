package com.example.course_app.dto;

import com.example.course_app.entity.tests.Test;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для преобразования сущности Test в DTO и обратно
 */
public class TestMapper {

    /**
     * Преобразует сущность Test в TestDTO
     * 
     * @param test сущность Test
     * @return объект TestDTO
     */
    public static TestDTO toDTO(Test test) {
        if (test == null) {
            return null;
        }
        
        TestDTO dto = new TestDTO();
        dto.setId(test.getId());
        dto.setTitle(test.getTitle());
        dto.setType(test.getType());
        dto.setRequiresManualCheck(test.isRequiresManualCheck());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setUpdatedAt(test.getUpdatedAt());
        
        if (test.getLesson() != null) {
            dto.setLessonId(test.getLesson().getId());
        }
        
        return dto;
    }
    
    /**
     * Преобразует список сущностей Test в список TestDTO
     * 
     * @param tests список сущностей Test
     * @return список объектов TestDTO
     */
    public static List<TestDTO> toDTOList(List<Test> tests) {
        return tests.stream()
                .map(TestMapper::toDTO)
                .collect(Collectors.toList());
    }
}
