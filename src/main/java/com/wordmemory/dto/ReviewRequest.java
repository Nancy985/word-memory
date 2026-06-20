package com.wordmemory.dto;

import jakarta.validation.constraints.NotNull;

public class ReviewRequest {

    @NotNull(message = "单词 ID 不能为空")
    private Long wordId;

    @NotNull(message = "是否正确不能为空")
    private Boolean correct;

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }
}
