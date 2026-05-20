package com.example.libr.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BookRequest {
    
    @NotBlank(message = "Book title is required")
    @Size(min = 1, max = 255, message = "Book title must be between 1 and 255 characters")
    private String title;
    
    @NotBlank(message = "Author name is required")
    @Size(min = 1, max = 255, message = "Author name must be between 1 and 255 characters")
    private String author;
    
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", 
             message = "Invalid ISBN format")
    private String isbn;
    
    @Size(max = 255, message = "Publisher name must not exceed 255 characters")
    private String publisher;
    
    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 9999, message = "Publication year must not exceed 9999")
    private Integer publicationYear;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Min(value = 1, message = "Number of pages must be at least 1")
    private Integer numberOfPages;
    
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;
    
    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImageUrl;
    
    @Min(value = 0, message = "Total copies must be a positive number")
    private Integer totalCopies;
    
    @Min(value = 0, message = "Available copies must be a positive number")
    private Integer availableCopies;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @NotNull(message = "Genre ID is required")
    private UUID genreId;
}

