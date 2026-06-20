package com.wordmemory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuizAnswerItem {

    @NotNull(message = "单词 ID 不能为空")
    private Long wordId;

    @NotBlank(message = "用户答案不能为空")
    private String userAnswer;

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
}
