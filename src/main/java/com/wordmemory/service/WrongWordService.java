package com.wordmemory.service;

import com.wordmemory.entity.WrongWordRecord;
import com.wordmemory.repository.WrongWordRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WrongWordService {

    private final WrongWordRecordRepository wrongWordRecordRepository;

    public WrongWordService(WrongWordRecordRepository wrongWordRecordRepository) {
        this.wrongWordRecordRepository = wrongWordRecordRepository;
    }

    @Transactional
    public void recordWrongWord(Long userId, Long wordId) {
        WrongWordRecord record = wrongWordRecordRepository.findByUserIdAndWordId(userId, wordId)
                .orElseGet(() -> {
                    WrongWordRecord r = new WrongWordRecord();
                    r.setUserId(userId);
                    r.setWordId(wordId);
                    r.setWrongCount(0);
                    return r;
                });
        record.setWrongCount(record.getWrongCount() + 1);
        record.setLastWrongAt(LocalDateTime.now());
        wrongWordRecordRepository.save(record);
    }
}
