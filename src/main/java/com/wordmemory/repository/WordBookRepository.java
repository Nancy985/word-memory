package com.wordmemory.repository;

import com.wordmemory.entity.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordBookRepository extends JpaRepository<WordBook, Long> {

    List<WordBook> findByUserIdOrIsPublicTrueOrderByCreatedAtDesc(Long userId);

    List<WordBook> findByUserIdOrderByCreatedAtDesc(Long userId);
}
