package com.wordmemory.dto;

public class WrongWordStatsVO {

    private long totalWrongWords;
    private long totalWrongTimes;
    private long reviewedWords;
    private long masteredWords;

    public long getTotalWrongWords() {
        return totalWrongWords;
    }

    public void setTotalWrongWords(long totalWrongWords) {
        this.totalWrongWords = totalWrongWords;
    }

    public long getTotalWrongTimes() {
        return totalWrongTimes;
    }

    public void setTotalWrongTimes(long totalWrongTimes) {
        this.totalWrongTimes = totalWrongTimes;
    }

    public long getReviewedWords() {
        return reviewedWords;
    }

    public void setReviewedWords(long reviewedWords) {
        this.reviewedWords = reviewedWords;
    }

    public long getMasteredWords() {
        return masteredWords;
    }

    public void setMasteredWords(long masteredWords) {
        this.masteredWords = masteredWords;
    }
}
