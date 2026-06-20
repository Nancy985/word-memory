package com.wordmemory.repository;

import com.wordmemory.entity.ExamRecord;
import com.wordmemory.entity.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {

    List<ExamRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ExamRecord> findByUserIdAndWordBookIdOrderByCreatedAtDesc(Long userId, Long wordBookId);

    List<ExamRecord> findByUserIdAndExamTypeOrderByCreatedAtDesc(Long userId, ExamType examType);
}
