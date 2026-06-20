package com.wordmemory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WordRequest {

    @NotBlank(message = "单词拼写不能为空")
    @Size(max = 100, message = "拼写最长 100 字符")
    private String spelling;

    @Size(max = 100, message = "音标最长 100 字符")
    private String phonetic;

    @NotBlank(message = "释义不能为空")
    @Size(max = 500, message = "释义最长 500 字符")
    private String meaning;

    @Size(max = 1000, message = "例句最长 1000 字符")
    private String exampleSentence;

    public String getSpelling() {
        return spelling;
    }

    public void setSpelling(String spelling) {
        this.spelling = spelling;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }
}
