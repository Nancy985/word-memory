package com.wordmemory.config;

import com.wordmemory.entity.User;
import com.wordmemory.entity.Word;
import com.wordmemory.entity.WordBook;
import com.wordmemory.repository.UserRepository;
import com.wordmemory.repository.WordBookRepository;
import com.wordmemory.repository.WordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WordBookRepository wordBookRepository;
    private final WordRepository wordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           WordBookRepository wordBookRepository,
                           WordRepository wordRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.wordBookRepository = wordBookRepository;
        this.wordRepository = wordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User demo = userRepository.findByUsername("demo").orElseGet(() -> {
            User user = new User();
            user.setUsername("demo");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setNickname("演示用户");
            return userRepository.save(user);
        });

        WordBook book = wordBookRepository.findById(1L).orElseGet(() -> {
            WordBook wb = new WordBook();
            wb.setName("CET-4 核心词汇");
            wb.setDescription("大学英语四级核心单词");
            wb.setUserId(demo.getId());
            wb.setIsPublic(true);
            return wordBookRepository.save(wb);
        });

        if (wordRepository.countByWordBookId(book.getId()) == 0) {
            createWord(book.getId(), "abandon", "/əˈbændən/", "v. 放弃；遗弃", "He abandoned his plan.");
            createWord(book.getId(), "ability", "/əˈbɪləti/", "n. 能力；才能", "She has the ability to learn quickly.");
            createWord(book.getId(), "absolute", "/ˈæbsəluːt/", "adj. 绝对的；完全的", "There is no absolute truth.");
            createWord(book.getId(), "abstract", "/ˈæbstrækt/", "adj. 抽象的 n. 摘要", "Abstract art is hard to understand.");
            createWord(book.getId(), "academic", "/ˌækəˈdemɪk/", "adj. 学术的；学院的", "He has an academic background.");
        }
    }

    private void createWord(Long bookId, String spelling, String phonetic, String meaning, String example) {
        Word word = new Word();
        word.setWordBookId(bookId);
        word.setSpelling(spelling);
        word.setPhonetic(phonetic);
        word.setMeaning(meaning);
        word.setExampleSentence(example);
        wordRepository.save(word);
    }
}
