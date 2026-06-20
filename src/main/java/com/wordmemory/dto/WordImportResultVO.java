package com.wordmemory.dto;

import java.util.ArrayList;
import java.util.List;

public class WordImportResultVO {

    private int imported;
    private int skipped;
    private List<String> errors = new ArrayList<>();

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
