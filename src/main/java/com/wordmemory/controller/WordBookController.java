package com.wordmemory.controller;

import com.wordmemory.common.ApiResponse;
import com.wordmemory.config.AuthInterceptor;
import com.wordmemory.dto.WordImportRequest;
import com.wordmemory.dto.WordImportResultVO;
import com.wordmemory.dto.WordBookRequest;
import com.wordmemory.dto.WordBookVO;
import com.wordmemory.dto.WordRequest;
import com.wordmemory.dto.WordVO;
import com.wordmemory.service.WordBookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wordbooks")
public class WordBookController {

    private final WordBookService wordBookService;

    public WordBookController(WordBookService wordBookService) {
        this.wordBookService = wordBookService;
    }

    @GetMapping
    public ApiResponse<List<WordBookVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(wordBookService.listWordBooks(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<WordBookVO> get(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(wordBookService.getWordBook(userId, id));
    }

    @PostMapping
    public ApiResponse<WordBookVO> create(@Valid @RequestBody WordBookRequest body,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success("创建成功", wordBookService.createWordBook(userId, body));
    }

    @PutMapping("/{id}")
    public ApiResponse<WordBookVO> update(@PathVariable Long id,
                                           @Valid @RequestBody WordBookRequest body,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success("更新成功", wordBookService.updateWordBook(userId, id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        wordBookService.deleteWordBook(userId, id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{id}/words")
    public ApiResponse<List<WordVO>> listWords(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success(wordBookService.listWords(userId, id));
    }

    @PostMapping("/{id}/words")
    public ApiResponse<WordVO> addWord(@PathVariable Long id,
                                       @Valid @RequestBody WordRequest body,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success("添加成功", wordBookService.addWord(userId, id, body));
    }

    @PostMapping("/{id}/words/import")
    public ApiResponse<WordImportResultVO> importWords(@PathVariable Long id,
                                                       @RequestBody WordImportRequest body,
                                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success("导入完成",
                wordBookService.importWords(userId, id, body.getCsvContent()));
    }

    @PutMapping("/{bookId}/words/{wordId}")
    public ApiResponse<WordVO> updateWord(@PathVariable Long bookId,
                                          @PathVariable Long wordId,
                                          @Valid @RequestBody WordRequest body,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return ApiResponse.success("更新成功", wordBookService.updateWord(userId, bookId, wordId, body));
    }

    @DeleteMapping("/{bookId}/words/{wordId}")
    public ApiResponse<Void> deleteWord(@PathVariable Long bookId,
                                        @PathVariable Long wordId,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
        wordBookService.deleteWord(userId, bookId, wordId);
        return ApiResponse.success("删除成功", null);
    }
}
