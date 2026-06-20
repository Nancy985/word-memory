package com.wordmemory.dto;

public class WordImportRequest {

    /** CSV 文本，每行：单词,音标,释义,例句（音标和例句可省略） */
    private String csvContent;

    public String getCsvContent() {
        return csvContent;
    }

    public void setCsvContent(String csvContent) {
        this.csvContent = csvContent;
    }
}
