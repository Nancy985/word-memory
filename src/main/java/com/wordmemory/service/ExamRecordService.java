package com.wordmemory.service;

import com.wordmemory.common.BusinessException;
import com.wordmemory.dto.ExamAnswerVO;
import com.wordmemory.dto.ExamRecordVO;
import com.wordmemory.entity.ExamAnswer;
import com.wordmemory.entity.ExamRecord;
import com.wordmemory.entity.ExamType;
import com.wordmemory.entity.Word;
import com.wordmemory.entity.WordBook;
import com.wordmemory.repository.ExamAnswerRepository;
import com.wordmemory.repository.ExamRecordRepository;
import com.wordmemory.repository.WordBookRepository;
import com.wordmemory.repository.WordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExamRecordService {

    private final ExamRecordRepository examRecordRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final WordBookRepository wordBookRepository;
    private final WordRepository wordRepository;

    public ExamRecordService(ExamRecordRepository examRecordRepository,
                             ExamAnswerRepository examAnswerRepository,
                             WordBookRepository wordBookRepository,
                             WordRepository wordRepository) {
        this.examRecordRepository = examRecordRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.wordBookRepository = wordBookRepository;
        this.wordRepository = wordRepository;
    }

    public List<ExamRecordVO> listExamRecords(Long userId, Long wordBookId, ExamType examType) {
        List<ExamRecord> records;
        if (wordBookId != null) {
            records = examRecordRepository.findByUserIdAndWordBookIdOrderByCreatedAtDesc(userId, wordBookId);
        } else if (examType != null) {
            records = examRecordRepository.findByUserIdAndExamTypeOrderByCreatedAtDesc(userId, examType);
        } else {
            records = examRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        Map<Long, WordBook> bookMap = wordBookRepository.findAll().stream()
                .collect(Collectors.toMap(WordBook::getId, Function.identity()));

        return records.stream()
                .map(record -> toVO(record, bookMap, false))
                .collect(Collectors.toList());
    }

    public ExamRecordVO getExamRecord(Long userId, Long id) {
        ExamRecord record = examRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "考试记录不存在"));
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看该考试记录");
        }

        Map<Long, WordBook> bookMap = wordBookRepository.findAll().stream()
                .collect(Collectors.toMap(WordBook::getId, Function.identity()));

        return toVO(record, bookMap, true);
    }

    private ExamRecordVO toVO(ExamRecord record, Map<Long, WordBook> bookMap, boolean includeAnswers) {
        ExamRecordVO vo = new ExamRecordVO();
        vo.setId(record.getId());
        vo.setWordBookId(record.getWordBookId());
        WordBook book = bookMap.get(record.getWordBookId());
        vo.setWordBookName(book != null ? book.getName() : null);
        vo.setExamType(record.getExamType());
        vo.setTotalCount(record.getTotalCount());
        vo.setCorrectCount(record.getCorrectCount());
        vo.setScore(record.getScore());
        vo.setDurationSeconds(record.getDurationSeconds());
        vo.setCreatedAt(record.getCreatedAt());

        if (includeAnswers) {
            List<ExamAnswer> answers = examAnswerRepository.findByExamRecordIdOrderByIdAsc(record.getId());
            Map<Long, Word> wordMap = answers.stream()
                    .map(ExamAnswer::getWordId)
                    .distinct()
                    .map(wordRepository::findById)
                    .filter(opt -> opt.isPresent())
                    .map(opt -> opt.get())
                    .collect(Collectors.toMap(Word::getId, Function.identity()));

            vo.setAnswers(answers.stream().map(answer -> {
                ExamAnswerVO avo = new ExamAnswerVO();
                avo.setWordId(answer.getWordId());
                Word word = wordMap.get(answer.getWordId());
                avo.setSpelling(word != null ? word.getSpelling() : null);
                avo.setUserAnswer(answer.getUserAnswer());
                avo.setCorrectAnswer(answer.getCorrectAnswer());
                avo.setIsCorrect(answer.getIsCorrect());
                return avo;
            }).collect(Collectors.toList()));
        }

        return vo;
    }
}
