package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BookResponse {
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Integer publicationYear;
    private String description;
    private Integer numberOfPages;
    private String language;
    private String coverImageUrl;
    private Integer totalCopies;
    private Integer availableCopies;
    private Boolean available;
    private BigDecimal price;
    private SimpleGenreResponse genre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

