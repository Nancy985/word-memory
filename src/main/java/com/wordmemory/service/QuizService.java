package com.wordmemory.service;

import com.wordmemory.common.BusinessException;
import com.wordmemory.dto.*;
import com.wordmemory.entity.*;
import com.wordmemory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final WordBookService wordBookService;
    private final WordRepository wordRepository;
    private final ExamRecordRepository examRecordRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final WrongWordService wrongWordService;
    private final MemorizeService memorizeService;

    public QuizService(WordBookService wordBookService,
                       WordRepository wordRepository,
                       ExamRecordRepository examRecordRepository,
                       ExamAnswerRepository examAnswerRepository,
                       WrongWordService wrongWordService,
                       MemorizeService memorizeService) {
        this.wordBookService = wordBookService;
        this.wordRepository = wordRepository;
        this.examRecordRepository = examRecordRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.wrongWordService = wrongWordService;
        this.memorizeService = memorizeService;
    }

    public List<QuizQuestionVO> generateQuiz(Long userId, QuizGenerateRequest request) {
        wordBookService.getAccessibleWordBook(userId, request.getWordBookId());

        List<Word> allWords = wordRepository.findByWordBookIdOrderByIdAsc(request.getWordBookId());
        if (allWords.size() < 4) {
            throw new BusinessException("单词书至少需要 4 个单词才能生成四选一测验");
        }

        List<Word> shuffled = new ArrayList<>(allWords);
        Collections.shuffle(shuffled);

        int count = Math.min(request.getCount(), shuffled.size());
        List<Word> selected = shuffled.subList(0, count);

        return selected.stream()
                .map(word -> buildQuestion(word, allWords))
                .collect(Collectors.toList());
    }

    @Transactional
    public QuizResultVO submitQuiz(Long userId, QuizSubmitRequest request) {
        wordBookService.getAccessibleWordBook(userId, request.getWordBookId());

        Map<Long, Word> wordMap = wordRepository.findByWordBookIdOrderByIdAsc(request.getWordBookId())
                .stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));

        int correctCount = 0;
        List<ExamAnswer> examAnswers = new ArrayList<>();

        for (QuizAnswerItem item : request.getAnswers()) {
            Word word = wordMap.get(item.getWordId());
            if (word == null) {
                throw new BusinessException("单词 ID " + item.getWordId() + " 不存在于该单词书");
            }

            boolean isCorrect = word.getMeaning().equals(item.getUserAnswer());
            if (isCorrect) {
                correctCount++;
                ReviewRequest reviewRequest = new ReviewRequest();
                reviewRequest.setWordId(word.getId());
                reviewRequest.setCorrect(true);
                memorizeService.submitReview(userId, reviewRequest);
            } else {
                wrongWordService.recordWrongWord(userId, word.getId());
                ReviewRequest reviewRequest = new ReviewRequest();
                reviewRequest.setWordId(word.getId());
                reviewRequest.setCorrect(false);
                memorizeService.submitReview(userId, reviewRequest);
            }

            ExamAnswer answer = new ExamAnswer();
            answer.setWordId(word.getId());
            answer.setUserAnswer(item.getUserAnswer());
            answer.setCorrectAnswer(word.getMeaning());
            answer.setIsCorrect(isCorrect);
            examAnswers.add(answer);
        }

        int totalCount = request.getAnswers().size();
        double score = totalCount == 0 ? 0 : Math.round(correctCount * 10000.0 / totalCount) / 100.0;

        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setWordBookId(request.getWordBookId());
        record.setExamType(ExamType.SELF_TEST);
        record.setTotalCount(totalCount);
        record.setCorrectCount(correctCount);
        record.setScore(score);
        record.setDurationSeconds(request.getDurationSeconds());
        examRecordRepository.save(record);

        for (ExamAnswer answer : examAnswers) {
            answer.setExamRecordId(record.getId());
        }
        examAnswerRepository.saveAll(examAnswers);

        QuizResultVO result = new QuizResultVO();
        result.setExamId(record.getId());
        result.setTotalCount(totalCount);
        result.setCorrectCount(correctCount);
        result.setScore(score);
        result.setDurationSeconds(request.getDurationSeconds());
        return result;
    }

    private QuizQuestionVO buildQuestion(Word word, List<Word> allWords) {
        List<String> wrongOptions = allWords.stream()
                .filter(w -> !w.getId().equals(word.getId()))
                .map(Word::getMeaning)
                .distinct()
                .collect(Collectors.toList());
        Collections.shuffle(wrongOptions);

        List<String> options = new ArrayList<>();
        options.add(word.getMeaning());
        for (String wrong : wrongOptions) {
            if (options.size() >= 4) {
                break;
            }
            if (!options.contains(wrong)) {
                options.add(wrong);
            }
        }

        while (options.size() < 4 && wrongOptions.size() > options.size() - 1) {
            options.add("选项 " + (options.size() + 1));
        }

        Collections.shuffle(options);

        QuizQuestionVO vo = new QuizQuestionVO();
        vo.setWordId(word.getId());
        vo.setSpelling(word.getSpelling());
        vo.setPhonetic(word.getPhonetic());
        vo.setOptions(options);
        return vo;
    }
}
