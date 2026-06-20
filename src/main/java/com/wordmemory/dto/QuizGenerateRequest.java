package com.wordmemory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class QuizGenerateRequest {

    @NotNull(message = "单词书 ID 不能为空")
    private Long wordBookId;

    @Min(value = 1, message = "题目数量至少为 1")
    @Max(value = 50, message = "题目数量最多为 50")
    private Integer count = 10;

    public Long getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(Long wordBookId) {
        this.wordBookId = wordBookId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
