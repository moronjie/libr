package com.example.libr.mapper;

import com.example.libr.dto.request.GenreRequest;
import com.example.libr.dto.response.GenreResponse;
import com.example.libr.dto.response.SimpleGenreResponse;
import com.example.libr.entity.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {
    public Genre toEntity(GenreRequest request, Genre parent) {
        return Genre.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive())
                .parentGenre(parent)
                .build();
    }

    public GenreResponse toResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .code(genre.getCode())
                .description(genre.getDescription())
                .displayOrder(genre.getDisplayOrder())
                .active(genre.getActive())
                .parentGenre(mapParent(genre.getParentGenre()))
                .build();
    }

    private SimpleGenreResponse mapParent(Genre parent) {
        if (parent == null) return null;

        return SimpleGenreResponse.builder()
                .id(parent.getId())
                .name(parent.getName())
                .code(parent.getCode())
                .build();
    }
}
