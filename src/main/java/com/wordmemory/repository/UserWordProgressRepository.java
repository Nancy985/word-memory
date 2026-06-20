package com.wordmemory.repository;

import com.wordmemory.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    Optional<UserWordProgress> findByUserIdAndWordId(Long userId, Long wordId);

    List<UserWordProgress> findByUserIdAndWordIdIn(Long userId, List<Long> wordIds);

    @Query("SELECT p FROM UserWordProgress p JOIN Word w ON p.wordId = w.id " +
           "WHERE p.userId = :userId AND w.wordBookId = :wordBookId " +
           "AND p.nextReviewAt <= :now ORDER BY p.nextReviewAt ASC")
    List<UserWordProgress> findDueReviews(@Param("userId") Long userId,
                                          @Param("wordBookId") Long wordBookId,
                                          @Param("now") LocalDateTime now);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(p) FROM UserWordProgress p JOIN Word w ON p.wordId = w.id " +
           "WHERE p.userId = :userId AND w.wordBookId = :wordBookId")
    long countByUserIdAndWordBookId(@Param("userId") Long userId, @Param("wordBookId") Long wordBookId);

    @Query("SELECT p FROM UserWordProgress p JOIN Word w ON p.wordId = w.id " +
           "WHERE p.userId = :userId AND w.wordBookId = :wordBookId")
    List<UserWordProgress> findAllByUserIdAndWordBookId(@Param("userId") Long userId,
                                                         @Param("wordBookId") Long wordBookId);
}
