package com.wordmemory.dto;

public class MemorizeProgressVO {

    private Long wordBookId;
    private long totalWords;
    private long learnedWords;
    private long dueToday;
    private long masteredWords;
    private double progressPercent;

    public Long getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(Long wordBookId) {
        this.wordBookId = wordBookId;
    }

    public long getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(long totalWords) {
        this.totalWords = totalWords;
    }

    public long getLearnedWords() {
        return learnedWords;
    }

    public void setLearnedWords(long learnedWords) {
        this.learnedWords = learnedWords;
    }

    public long getDueToday() {
        return dueToday;
    }

    public void setDueToday(long dueToday) {
        this.dueToday = dueToday;
    }

    public long getMasteredWords() {
        return masteredWords;
    }

    public void setMasteredWords(long masteredWords) {
        this.masteredWords = masteredWords;
    }

    public double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(double progressPercent) {
        this.progressPercent = progressPercent;
    }
}
