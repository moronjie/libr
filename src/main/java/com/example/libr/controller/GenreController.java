package com.example.libr.controller;

import com.example.libr.dto.request.GenreRequest;
import com.example.libr.dto.response.GenreResponse;
import com.example.libr.payload.ApiResponse;
import com.example.libr.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(@Valid @RequestBody GenreRequest request) {
        GenreResponse response = genreService.createGenre(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<GenreResponse>builder()
                        .message("Genre created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        List<GenreResponse> genres = genreService.getAllGenres();
        return ResponseEntity
                .ok(ApiResponse.<List<GenreResponse>>builder()
                        .message("Genres retrieved successfully")
                        .data(genres)
                        .build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<GenreResponse>> getActiveGenre() {
        GenreResponse response = genreService.getActiveGenre();
        return ResponseEntity
                .ok(ApiResponse.<GenreResponse>builder()
                        .message("Active genre retrieved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreById(@PathVariable UUID id) {
        GenreResponse response = genreService.getGenreById(id);
        return ResponseEntity
                .ok(ApiResponse.<GenreResponse>builder()
                        .message("Genre retrieved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreByCode(@PathVariable String code) {
        GenreResponse response = genreService.getGenreByCode(code);
        return ResponseEntity
                .ok(ApiResponse.<GenreResponse>builder()
                        .message("Genre retrieved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreByName(@PathVariable String name) {
        GenreResponse response = genreService.getGenreByName(name);
        return ResponseEntity
                .ok(ApiResponse.<GenreResponse>builder()
                        .message("Genre retrieved successfully")
                        .data(response)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable UUID id,
            @Valid @RequestBody GenreRequest request) {
        GenreResponse response = genreService.updateGenre(id, request);
        return ResponseEntity
                .ok(ApiResponse.<GenreResponse>builder()
                        .message("Genre updated successfully")
                        .data(response)
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity
                .ok(ApiResponse.<Void>builder()
                        .message("Genre deleted successfully")
                        .data(null)
                        .build());
    }
}
