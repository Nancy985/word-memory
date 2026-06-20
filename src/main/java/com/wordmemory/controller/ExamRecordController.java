package com.wordmemory.controller;

import com.wordmemory.common.ApiResponse;
import com.wordmemory.config.AuthInterceptor;
import com.wordmemory.dto.ExamRecordVO;
import com.wordmemory.entity.ExamType;
import com.wordmemory.service.ExamRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamRecordController {

    private final ExamRecordService examRecordService;

    public ExamRecordController(ExamRecordService examRecordService) {
        this.examRecordService = examRecordService;
    }

    @GetMapping
    public ApiResponse<List<ExamRecordVO>> list(@RequestParam(required = false) Long wordBookId,
                                                @RequestParam(required = false) ExamType examType,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(examRecordService.listExamRecords(userId, wordBookId, examType));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExamRecordVO> get(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(examRecordService.getExamRecord(userId, id));
    }
}
