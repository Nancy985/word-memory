package com.wordmemory.service;

import com.wordmemory.common.BusinessException;
import com.wordmemory.dto.WordBookRequest;
import com.wordmemory.dto.WordBookVO;
import com.wordmemory.dto.WordImportResultVO;
import com.wordmemory.dto.WordRequest;
import com.wordmemory.dto.WordVO;
import com.wordmemory.entity.Word;
import com.wordmemory.entity.WordBook;
import com.wordmemory.repository.WordBookRepository;
import com.wordmemory.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WordBookService {

    private final WordBookRepository wordBookRepository;
    private final WordRepository wordRepository;

    public WordBookService(WordBookRepository wordBookRepository, WordRepository wordRepository) {
        this.wordBookRepository = wordBookRepository;
        this.wordRepository = wordRepository;
    }

    public List<WordBookVO> listWordBooks(Long userId) {
        return wordBookRepository.findByUserIdOrIsPublicTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public WordBookVO getWordBook(Long userId, Long id) {
        WordBook wordBook = getAccessibleWordBook(userId, id);
        return toVO(wordBook);
    }

    @Transactional
    public WordBookVO createWordBook(Long userId, WordBookRequest request) {
        WordBook wordBook = new WordBook();
        wordBook.setName(request.getName());
        wordBook.setDescription(request.getDescription());
        wordBook.setUserId(userId);
        wordBook.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        wordBookRepository.save(wordBook);
        return toVO(wordBook);
    }

    @Transactional
    public WordBookVO updateWordBook(Long userId, Long id, WordBookRequest request) {
        WordBook wordBook = getOwnedWordBook(userId, id);
        wordBook.setName(request.getName());
        wordBook.setDescription(request.getDescription());
        if (request.getIsPublic() != null) {
            wordBook.setIsPublic(request.getIsPublic());
        }
        wordBookRepository.save(wordBook);
        return toVO(wordBook);
    }

    @Transactional
    public void deleteWordBook(Long userId, Long id) {
        WordBook wordBook = getOwnedWordBook(userId, id);
        List<Word> words = wordRepository.findByWordBookIdOrderByIdAsc(id);
        wordRepository.deleteAll(words);
        wordBookRepository.delete(wordBook);
    }

    public List<WordVO> listWords(Long userId, Long wordBookId) {
        getAccessibleWordBook(userId, wordBookId);
        return wordRepository.findByWordBookIdOrderByIdAsc(wordBookId)
                .stream()
                .map(this::toWordVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public WordVO addWord(Long userId, Long wordBookId, WordRequest request) {
        getOwnedWordBook(userId, wordBookId);
        Word word = new Word();
        word.setWordBookId(wordBookId);
        word.setSpelling(request.getSpelling());
        word.setPhonetic(request.getPhonetic());
        word.setMeaning(request.getMeaning());
        word.setExampleSentence(request.getExampleSentence());
        wordRepository.save(word);
        return toWordVO(word);
    }

    @Transactional
    public WordVO updateWord(Long userId, Long wordBookId, Long wordId, WordRequest request) {
        getOwnedWordBook(userId, wordBookId);
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new BusinessException(404, "单词不存在"));
        if (!word.getWordBookId().equals(wordBookId)) {
            throw new BusinessException(400, "单词不属于该单词书");
        }
        word.setSpelling(request.getSpelling());
        word.setPhonetic(request.getPhonetic());
        word.setMeaning(request.getMeaning());
        word.setExampleSentence(request.getExampleSentence());
        wordRepository.save(word);
        return toWordVO(word);
    }

    @Transactional
    public void deleteWord(Long userId, Long wordBookId, Long wordId) {
        getOwnedWordBook(userId, wordBookId);
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new BusinessException(404, "单词不存在"));
        if (!word.getWordBookId().equals(wordBookId)) {
            throw new BusinessException(400, "单词不属于该单词书");
        }
        wordRepository.delete(word);
    }

    @Transactional
    public WordImportResultVO importWords(Long userId, Long wordBookId, String csvContent) {
        getOwnedWordBook(userId, wordBookId);
        if (csvContent == null || csvContent.isBlank()) {
            throw new BusinessException("导入内容不能为空");
        }

        WordImportResultVO result = new WordImportResultVO();
        Set<String> existing = wordRepository.findByWordBookIdOrderByIdAsc(wordBookId).stream()
                .map(w -> w.getSpelling().toLowerCase())
                .collect(Collectors.toSet());

        String[] lines = csvContent.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length < 2) {
                result.getErrors().add("第 " + (i + 1) + " 行格式错误，至少需要：单词,释义");
                result.setSkipped(result.getSkipped() + 1);
                continue;
            }

            String spelling = parts[0].trim();
            String meaning = parts.length >= 3 ? parts[2].trim() : parts[1].trim();
            String phonetic = parts.length >= 3 ? parts[1].trim() : "";
            String example = parts.length >= 4 ? parts[3].trim() : "";

            if (spelling.isEmpty() || meaning.isEmpty()) {
                result.getErrors().add("第 " + (i + 1) + " 行：单词或释义为空");
                result.setSkipped(result.getSkipped() + 1);
                continue;
            }
            if (existing.contains(spelling.toLowerCase())) {
                result.setSkipped(result.getSkipped() + 1);
                continue;
            }

            Word word = new Word();
            word.setWordBookId(wordBookId);
            word.setSpelling(spelling);
            word.setPhonetic(phonetic);
            word.setMeaning(meaning);
            word.setExampleSentence(example);
            wordRepository.save(word);
            existing.add(spelling.toLowerCase());
            result.setImported(result.getImported() + 1);
        }
        return result;
    }

    public WordBook getAccessibleWordBook(Long userId, Long id) {
        WordBook wordBook = wordBookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "单词书不存在"));
        if (!wordBook.getUserId().equals(userId) && !Boolean.TRUE.equals(wordBook.getIsPublic())) {
            throw new BusinessException(403, "无权访问该单词书");
        }
        return wordBook;
    }

    private WordBook getOwnedWordBook(Long userId, Long id) {
        WordBook wordBook = wordBookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "单词书不存在"));
        if (!wordBook.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该单词书");
        }
        return wordBook;
    }

    private WordBookVO toVO(WordBook wordBook) {
        WordBookVO vo = new WordBookVO();
        vo.setId(wordBook.getId());
        vo.setName(wordBook.getName());
        vo.setDescription(wordBook.getDescription());
        vo.setUserId(wordBook.getUserId());
        vo.setIsPublic(wordBook.getIsPublic());
        vo.setWordCount(wordRepository.countByWordBookId(wordBook.getId()));
        vo.setCreatedAt(wordBook.getCreatedAt());
        return vo;
    }

    private WordVO toWordVO(Word word) {
        WordVO vo = new WordVO();
        vo.setId(word.getId());
        vo.setWordBookId(word.getWordBookId());
        vo.setSpelling(word.getSpelling());
        vo.setPhonetic(word.getPhonetic());
        vo.setMeaning(word.getMeaning());
        vo.setExampleSentence(word.getExampleSentence());
        vo.setCreatedAt(word.getCreatedAt());
        return vo;
    }
}
