package com.wordmemory.service;

import com.wordmemory.dto.WrongWordStatsVO;
import com.wordmemory.dto.WrongWordVO;
import com.wordmemory.entity.Word;
import com.wordmemory.entity.WordBook;
import com.wordmemory.entity.WrongWordRecord;
import com.wordmemory.repository.UserWordProgressRepository;
import com.wordmemory.repository.WordBookRepository;
import com.wordmemory.repository.WordRepository;
import com.wordmemory.repository.WrongWordRecordRepository;
import com.wordmemory.util.SpacedRepetitionUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WrongWordQueryService {

    private final WrongWordRecordRepository wrongWordRecordRepository;
    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final UserWordProgressRepository progressRepository;

    public WrongWordQueryService(WrongWordRecordRepository wrongWordRecordRepository,
                                 WordRepository wordRepository,
                                 WordBookRepository wordBookRepository,
                                 UserWordProgressRepository progressRepository) {
        this.wrongWordRecordRepository = wrongWordRecordRepository;
        this.wordRepository = wordRepository;
        this.wordBookRepository = wordBookRepository;
        this.progressRepository = progressRepository;
    }

    public List<WrongWordVO> listWrongWords(Long userId, Long wordBookId) {
        List<WrongWordRecord> records = wordBookId != null
                ? wrongWordRecordRepository.findByUserIdAndWordBookId(userId, wordBookId)
                : wrongWordRecordRepository.findByUserIdOrderByWrongCountDescLastWrongAtDesc(userId);

        Map<Long, Word> wordMap = records.stream()
                .map(WrongWordRecord::getWordId)
                .distinct()
                .map(wordRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Word::getId, Function.identity()));

        Map<Long, WordBook> bookMap = wordMap.values().stream()
                .map(Word::getWordBookId)
                .distinct()
                .map(wordBookRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(WordBook::getId, Function.identity()));

        return records.stream()
                .map(record -> {
                    Word word = wordMap.get(record.getWordId());
                    if (word == null) {
                        return null;
                    }
                    WordBook book = bookMap.get(word.getWordBookId());
                    WrongWordVO vo = new WrongWordVO();
                    vo.setWordId(word.getId());
                    vo.setSpelling(word.getSpelling());
                    vo.setPhonetic(word.getPhonetic());
                    vo.setMeaning(word.getMeaning());
                    vo.setWordBookId(word.getWordBookId());
                    vo.setWordBookName(book != null ? book.getName() : null);
                    vo.setWrongCount(record.getWrongCount());
                    vo.setLastWrongAt(record.getLastWrongAt());
                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    public WrongWordStatsVO getStats(Long userId) {
        WrongWordStatsVO stats = new WrongWordStatsVO();
        stats.setTotalWrongWords(wrongWordRecordRepository.countByUserId(userId));
        stats.setTotalWrongTimes(wrongWordRecordRepository.sumWrongCountByUserId(userId));
        stats.setReviewedWords(progressRepository.countByUserId(userId));

        long mastered = progressRepository.findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .filter(p -> p.getReviewStage() >= SpacedRepetitionUtil.MAX_STAGE - 1)
                .count();
        stats.setMasteredWords(mastered);
        return stats;
    }
}
