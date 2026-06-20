package com.wordmemory.repository;

import com.wordmemory.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByWordBookIdOrderByIdAsc(Long wordBookId);

    long countByWordBookId(Long wordBookId);

    List<Word> findByWordBookIdAndIdNotIn(Long wordBookId, List<Long> excludeIds);
}
