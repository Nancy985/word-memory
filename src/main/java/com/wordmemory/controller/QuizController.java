package com.wordmemory.controller;

import com.wordmemory.common.ApiResponse;
import com.wordmemory.config.AuthInterceptor;
import com.wordmemory.dto.QuizGenerateRequest;
import com.wordmemory.dto.QuizQuestionVO;
import com.wordmemory.dto.QuizResultVO;
import com.wordmemory.dto.QuizSubmitRequest;
import com.wordmemory.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/generate")
    public ApiResponse<List<QuizQuestionVO>> generate(@Valid @RequestBody QuizGenerateRequest body,
                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(quizService.generateQuiz(userId, body));
    }

    @PostMapping("/submit")
    public ApiResponse<QuizResultVO> submit(@Valid @RequestBody QuizSubmitRequest body,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success("提交成功", quizService.submitQuiz(userId, body));
    }
}
