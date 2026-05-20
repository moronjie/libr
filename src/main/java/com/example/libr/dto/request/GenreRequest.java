package com.example.libr.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GenreRequest {
    @NotBlank(message = "Genre name is required")
    @Size(min = 2, max = 100, message = "Genre name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Genre code is required")
    @Size(min = 2, max = 50, message = "Genre code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Genre code must contain only uppercase letters, numbers, underscores, and hyphens")
    private String code;
    
    @NotBlank(message = "Genre description is required")
    @Size(max = 500, message = "Genre description must not exceed 500 characters")
    private String description;
    
    @Min(value = 0, message = "Display order must be a positive number")
    private Integer displayOrder;
    
    @NotNull(message = "Active status is required")
    private Boolean active;
    
    private UUID parentId;
}
