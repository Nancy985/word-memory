package com.wordmemory.repository;

import com.wordmemory.entity.WrongWordRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WrongWordRecordRepository extends JpaRepository<WrongWordRecord, Long> {

    Optional<WrongWordRecord> findByUserIdAndWordId(Long userId, Long wordId);

    List<WrongWordRecord> findByUserIdOrderByWrongCountDescLastWrongAtDesc(Long userId);

    @Query("SELECT w FROM WrongWordRecord w JOIN Word wd ON w.wordId = wd.id " +
           "WHERE w.userId = :userId AND wd.wordBookId = :wordBookId " +
           "ORDER BY w.wrongCount DESC, w.lastWrongAt DESC")
    List<WrongWordRecord> findByUserIdAndWordBookId(@Param("userId") Long userId,
                                                     @Param("wordBookId") Long wordBookId);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(w.wrongCount), 0) FROM WrongWordRecord w WHERE w.userId = :userId")
    long sumWrongCountByUserId(@Param("userId") Long userId);
}
