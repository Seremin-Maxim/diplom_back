package com.example.course_app.repository;

import com.example.course_app.entity.submissions.StudentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с отправками ответов студентов на тесты.
 */
@Repository
public interface StudentSubmissionRepository extends JpaRepository<StudentSubmission, Long> {
    
    List<StudentSubmission> findByStudentId(Long studentId);
    List<StudentSubmission> findByTestId(Long testId);
    List<StudentSubmission> findByStudentIdAndTestId(Long studentId, Long testId);
    List<StudentSubmission> findByReviewedFalse();

    @Query("SELECT COUNT(DISTINCT s.student.id) FROM StudentSubmission s WHERE s.test.id = :testId")
    long countDistinctStudentsByTestId(@Param("testId") Long testId);

    @Query("SELECT AVG(s.score) FROM StudentSubmission s WHERE s.test.id = :testId")
    Double findAverageScoreByTestId(@Param("testId") Long testId);

    @Query("SELECT s FROM StudentSubmission s WHERE s.test.id = :testId AND s.endTime IS NOT NULL")
    List<StudentSubmission> findCompletedByTestId(@Param("testId") Long testId);

    @Query("SELECT COUNT(s) FROM StudentSubmission s WHERE s.test.id = :testId AND s.endTime IS NULL")
    long countIncompleteByTestId(@Param("testId") Long testId);

    @Query("SELECT AVG(a.score) FROM StudentAnswer a WHERE a.question.id = :questionId")
    Double findAverageScoreForQuestion(@Param("questionId") Long questionId);

    @Query("SELECT COUNT(a) FROM StudentAnswer a WHERE a.question.id = :questionId")
    Long countAttemptsByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT COUNT(a) FROM StudentAnswer a WHERE a.question.id = :questionId AND a.isCorrect = true")
    Long countCorrectAnswersByQuestionId(@Param("questionId") Long questionId);
}