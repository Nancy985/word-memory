package com.wordmemory.controller;

import com.wordmemory.common.ApiResponse;
import com.wordmemory.config.AuthInterceptor;
import com.wordmemory.dto.MemorizeProgressVO;
import com.wordmemory.dto.ReviewRequest;
import com.wordmemory.dto.ReviewWordVO;
import com.wordmemory.service.MemorizeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memorize")
public class MemorizeController {

    private final MemorizeService memorizeService;

    public MemorizeController(MemorizeService memorizeService) {
        this.memorizeService = memorizeService;
    }

    @GetMapping("/today")
    public ApiResponse<List<ReviewWordVO>> getTodayWords(@RequestParam Long wordBookId,
                                                         @RequestParam(defaultValue = "20") int limit,
                                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(memorizeService.getTodayReviewWords(userId, wordBookId, limit));
    }

    @PostMapping("/review")
    public ApiResponse<ReviewWordVO> submitReview(@Valid @RequestBody ReviewRequest body,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success("复习记录已保存", memorizeService.submitReview(userId, body));
    }

    @GetMapping("/progress")
    public ApiResponse<MemorizeProgressVO> getProgress(@RequestParam Long wordBookId,
                                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(memorizeService.getProgress(userId, wordBookId));
    }
}
