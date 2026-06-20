package com.wordmemory.dto;

import com.wordmemory.entity.ExamType;

import java.time.LocalDateTime;
import java.util.List;

public class ExamRecordVO {

    private Long id;
    private Long wordBookId;
    private String wordBookName;
    private ExamType examType;
    private Integer totalCount;
    private Integer correctCount;
    private Double score;
    private Integer durationSeconds;
    private LocalDateTime createdAt;
    private List<ExamAnswerVO> answers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(Long wordBookId) {
        this.wordBookId = wordBookId;
    }

    public String getWordBookName() {
        return wordBookName;
    }

    public void setWordBookName(String wordBookName) {
        this.wordBookName = wordBookName;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ExamAnswerVO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExamAnswerVO> answers) {
        this.answers = answers;
    }
}
