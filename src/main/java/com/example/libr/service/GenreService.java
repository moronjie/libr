package com.example.libr.service;

import com.example.libr.dto.request.GenreRequest;
import com.example.libr.dto.response.GenreResponse;
import com.example.libr.entity.Genre;
import com.example.libr.exception.ConflictException;
import com.example.libr.exception.ResourceNotFoundException;
import com.example.libr.mapper.GenreMapper;
import com.example.libr.repository.GenreRepository;
import com.example.libr.service.impl.IGenreService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@Validated
public class GenreService implements IGenreService {
    GenreRepository genreRepository;
    GenreMapper genreMapper;

    GenreService(GenreRepository genreRepository, GenreMapper genreMapper) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
    }

    @Override
    public GenreResponse createGenre(@Valid GenreRequest request) {
        if (genreRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Genre code already exists");
        }

        Genre parent = getParentIfExists(request.getParentId());
        Genre genre = genreMapper.toEntity(request, parent);
        Genre savedGenre = genreRepository.save(genre);

        return genreMapper.toResponse(savedGenre);
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        List<Genre> genres = genreRepository.findAll();

        return genres.stream()
                .map(genreMapper::toResponse)
                .toList();
    }

    @Override
    public GenreResponse getActiveGenre() {
        List<Genre> activeGenre = genreRepository.findByActiveTrue();
        return activeGenre.stream()
                .map(genreMapper::toResponse)
                .findFirst()
                .orElse(null);
    }

    @Override
    public GenreResponse getGenreById(UUID id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Genre not found"));
        return genreMapper.toResponse(genre);
    }

    @Override
    public GenreResponse getGenreByCode(String code) {
        Genre genre = genreRepository.findByCode(code)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Genre not found"));
        return genreMapper.toResponse(genre);
    }

    @Override
    public GenreResponse getGenreByName(String name) {
        Genre genre = genreRepository.findByName(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Genre not found"));
        return genreMapper.toResponse(genre);
    }

    @Override
    public GenreResponse updateGenre(UUID id, @Valid GenreRequest request) {
            Genre genre = genreRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Genre not found"));

            if (request.getCode() != null && !request.getCode().equals(genre.getCode())) {
                if (genreRepository.existsByCode(request.getCode())) {
                    throw new ConflictException("Genre code already exists");
                }
                genre.setCode(request.getCode());
            }

            Genre parent = getParentIfExists(request.getParentId());
            if (parent != null && !parent.getId().equals(genre.getId())) {
                throw new ConflictException("Genre cannot be its own parent");
            }

            genre.setName(request.getName() != null ? request.getName() : genre.getName());
            genre.setDescription(request.getDescription() != null ? request.getDescription() : genre.getDescription());
            genre.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : genre.getDisplayOrder());
            genre.setActive(request.getActive() != null ? request.getActive() : genre.getActive());
            genre.setParentGenre(parent);

            genreRepository.save(genre);
            return genreMapper.toResponse(genre);

    }

    @Override
    public void deleteGenre(UUID id) {
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Genre not found");
        }

        genreRepository.deleteById(id);
    }

    private Genre getParentIfExists(UUID parentId) {
        if (parentId == null) return null;

        return genreRepository.findById(parentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Parent genre not found"));
    }
}
