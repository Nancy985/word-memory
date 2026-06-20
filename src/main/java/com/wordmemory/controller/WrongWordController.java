package com.wordmemory.controller;

import com.wordmemory.common.ApiResponse;
import com.wordmemory.config.AuthInterceptor;
import com.wordmemory.dto.WrongWordStatsVO;
import com.wordmemory.dto.WrongWordVO;
import com.wordmemory.service.WrongWordQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrong-words")
public class WrongWordController {

    private final WrongWordQueryService wrongWordQueryService;

    public WrongWordController(WrongWordQueryService wrongWordQueryService) {
        this.wrongWordQueryService = wrongWordQueryService;
    }

    @GetMapping
    public ApiResponse<List<WrongWordVO>> list(@RequestParam(required = false) Long wordBookId,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(wrongWordQueryService.listWrongWords(userId, wordBookId));
    }

    @GetMapping("/stats")
    public ApiResponse<WrongWordStatsVO> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(wrongWordQueryService.getStats(userId));
    }
}
