package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SimpleBookResponse {
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Boolean available;
}

