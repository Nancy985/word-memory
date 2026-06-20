package com.wordmemory.dto;

import java.time.LocalDateTime;

public class ReviewWordVO {

    private Long wordId;
    private String spelling;
    private String phonetic;
    private String meaning;
    private String exampleSentence;
    private Integer reviewStage;
    private Integer familiarity;
    private LocalDateTime nextReviewAt;
    private boolean isNew;

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

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

    public Integer getReviewStage() {
        return reviewStage;
    }

    public void setReviewStage(Integer reviewStage) {
        this.reviewStage = reviewStage;
    }

    public Integer getFamiliarity() {
        return familiarity;
    }

    public void setFamiliarity(Integer familiarity) {
        this.familiarity = familiarity;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(LocalDateTime nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
