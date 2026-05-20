package com.example.libr.service.impl;

import com.example.libr.dto.request.GenreRequest;
import com.example.libr.dto.response.GenreResponse;

import java.util.List;
import java.util.UUID;

public interface IGenreService {
    GenreResponse createGenre(GenreRequest request);

    List<GenreResponse> getAllGenres();
    GenreResponse getActiveGenre();
    GenreResponse getGenreById(UUID id);
    GenreResponse getGenreByCode(String code);
    GenreResponse getGenreByName(String name);

    GenreResponse updateGenre(UUID id, GenreRequest request);
    void deleteGenre(UUID id);

}
