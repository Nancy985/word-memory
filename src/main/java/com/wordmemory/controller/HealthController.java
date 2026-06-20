package com.wordmemory.controller;

import com.wordmemory.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", "word-memory-backend");
        info.put("status", "running");
        info.put("port", 3500);
        info.put("docs", "请访问 /api/auth/login 等接口，详见 README.md");
        return ApiResponse.success(info);
    }
}
