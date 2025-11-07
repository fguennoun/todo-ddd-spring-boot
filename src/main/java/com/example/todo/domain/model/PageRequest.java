package com.example.todo.domain.model;

/**
 * Domain pagination request
 */
public class PageRequest {
    private final int pageNumber;
    private final int pageSize;

    public PageRequest(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public static PageRequest of(int pageNumber, int pageSize) {
        return new PageRequest(pageNumber, pageSize);
    }
}

