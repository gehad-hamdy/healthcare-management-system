package com.healthcare.dto;

import lombok.Data;

@Data
public class PageRequestDTO {
    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDirection = "ASC";
}