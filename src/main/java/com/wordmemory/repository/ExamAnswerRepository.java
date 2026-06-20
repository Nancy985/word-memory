package com.wordmemory.repository;

import com.wordmemory.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {

    List<ExamAnswer> findByExamRecordIdOrderByIdAsc(Long examRecordId);
}
