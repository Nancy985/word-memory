package com.wordmemory.service;

import com.wordmemory.common.BusinessException;
import com.wordmemory.dto.MemorizeProgressVO;
import com.wordmemory.dto.ReviewRequest;
import com.wordmemory.dto.ReviewWordVO;
import com.wordmemory.entity.UserWordProgress;
import com.wordmemory.entity.Word;
import com.wordmemory.repository.UserWordProgressRepository;
import com.wordmemory.repository.WordRepository;
import com.wordmemory.util.SpacedRepetitionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MemorizeService {

    private final WordBookService wordBookService;
    private final WordRepository wordRepository;
    private final UserWordProgressRepository progressRepository;
    private final WrongWordService wrongWordService;

    public MemorizeService(WordBookService wordBookService,
                           WordRepository wordRepository,
                           UserWordProgressRepository progressRepository,
                           WrongWordService wrongWordService) {
        this.wordBookService = wordBookService;
        this.wordRepository = wordRepository;
        this.progressRepository = progressRepository;
        this.wrongWordService = wrongWordService;
    }

    public List<ReviewWordVO> getTodayReviewWords(Long userId, Long wordBookId, int limit) {
        wordBookService.getAccessibleWordBook(userId, wordBookId);
        LocalDateTime now = LocalDateTime.now();

        List<Word> allWords = wordRepository.findByWordBookIdOrderByIdAsc(wordBookId);
        Map<Long, Word> wordMap = allWords.stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));

        List<UserWordProgress> dueProgress = progressRepository.findDueReviews(userId, wordBookId, now);
        Set<Long> addedWordIds = new java.util.HashSet<>();
        List<ReviewWordVO> result = new ArrayList<>();

        for (UserWordProgress progress : dueProgress) {
            if (result.size() >= limit) {
                break;
            }
            Word word = wordMap.get(progress.getWordId());
            if (word != null && addedWordIds.add(word.getId())) {
                result.add(toReviewWordVO(word, progress, false));
            }
        }

        for (Word word : allWords) {
            if (result.size() >= limit) {
                break;
            }
            if (addedWordIds.contains(word.getId())) {
                continue;
            }
            var progressOpt = progressRepository.findByUserIdAndWordId(userId, word.getId());
            if (progressOpt.isEmpty()) {
                result.add(toReviewWordVO(word, null, true));
                addedWordIds.add(word.getId());
            }
        }

        return result;
    }

    @Transactional
    public ReviewWordVO submitReview(Long userId, ReviewRequest request) {
        Word word = wordRepository.findById(request.getWordId())
                .orElseThrow(() -> new BusinessException(404, "单词不存在"));

        wordBookService.getAccessibleWordBook(userId, word.getWordBookId());

        UserWordProgress progress = progressRepository.findByUserIdAndWordId(userId, word.getId())
                .orElseGet(() -> {
                    UserWordProgress p = new UserWordProgress();
                    p.setUserId(userId);
                    p.setWordId(word.getId());
                    p.setReviewStage(0);
                    p.setNextReviewAt(LocalDateTime.now());
                    return p;
                });

        progress.setLastReviewAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(request.getCorrect())) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
            int newStage = SpacedRepetitionUtil.advanceStage(progress.getReviewStage());
            progress.setReviewStage(newStage);
            progress.setNextReviewAt(SpacedRepetitionUtil.calculateNextReview(newStage - 1));
        } else {
            progress.setWrongCount(progress.getWrongCount() + 1);
            progress.setReviewStage(SpacedRepetitionUtil.resetStage());
            progress.setNextReviewAt(SpacedRepetitionUtil.calculateNextReview(0));
            wrongWordService.recordWrongWord(userId, word.getId());
        }

        progress.setFamiliarity(SpacedRepetitionUtil.calculateFamiliarity(
                progress.getReviewStage(),
                progress.getCorrectCount(),
                progress.getWrongCount()
        ));

        progressRepository.save(progress);
        return toReviewWordVO(word, progress, false);
    }

    public MemorizeProgressVO getProgress(Long userId, Long wordBookId) {
        wordBookService.getAccessibleWordBook(userId, wordBookId);

        long totalWords = wordRepository.countByWordBookId(wordBookId);
        long learnedWords = progressRepository.countByUserIdAndWordBookId(userId, wordBookId);
        long dueToday = progressRepository.findDueReviews(userId, wordBookId, LocalDateTime.now()).size();

        long masteredWords = progressRepository.findAllByUserIdAndWordBookId(userId, wordBookId).stream()
                .filter(p -> p.getReviewStage() >= SpacedRepetitionUtil.MAX_STAGE - 1)
                .count();

        MemorizeProgressVO vo = new MemorizeProgressVO();
        vo.setWordBookId(wordBookId);
        vo.setTotalWords(totalWords);
        vo.setLearnedWords(learnedWords);
        vo.setDueToday(dueToday);
        vo.setMasteredWords(masteredWords);
        vo.setProgressPercent(totalWords == 0 ? 0 : Math.round(learnedWords * 10000.0 / totalWords) / 100.0);
        return vo;
    }

    private ReviewWordVO toReviewWordVO(Word word, UserWordProgress progress, boolean isNew) {
        ReviewWordVO vo = new ReviewWordVO();
        vo.setWordId(word.getId());
        vo.setSpelling(word.getSpelling());
        vo.setPhonetic(word.getPhonetic());
        vo.setMeaning(word.getMeaning());
        vo.setExampleSentence(word.getExampleSentence());
        vo.setNew(isNew);
        if (progress != null) {
            vo.setReviewStage(progress.getReviewStage());
            vo.setFamiliarity(progress.getFamiliarity());
            vo.setNextReviewAt(progress.getNextReviewAt());
        } else {
            vo.setReviewStage(0);
            vo.setFamiliarity(0);
            vo.setNextReviewAt(LocalDateTime.now());
        }
        return vo;
    }
}
