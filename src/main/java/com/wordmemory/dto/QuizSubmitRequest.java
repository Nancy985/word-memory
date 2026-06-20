package com.wordmemory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class QuizSubmitRequest {

    @NotNull(message = "单词书 ID 不能为空")
    private Long wordBookId;

    private Integer durationSeconds;

    @NotEmpty(message = "答题列表不能为空")
    @Valid
    private List<QuizAnswerItem> answers;

    public Long getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(Long wordBookId) {
        this.wordBookId = wordBookId;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public List<QuizAnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizAnswerItem> answers) {
        this.answers = answers;
    }
}
