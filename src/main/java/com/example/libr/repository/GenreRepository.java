package com.example.libr.repository;

import com.example.libr.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {
    boolean existsByCode(String code);

    List<Genre> findByActiveTrue();

    Optional<Genre> findByCode(String code);

    Optional<Genre> findByName(String name);
}
