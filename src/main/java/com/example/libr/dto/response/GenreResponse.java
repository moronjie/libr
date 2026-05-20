package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GenreResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private Integer displayOrder;
    private Boolean active;

    private SimpleGenreResponse parentGenre;
}
