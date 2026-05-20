package com.example.libr.mapper;

import com.example.libr.dto.request.BookRequest;
import com.example.libr.dto.response.BookResponse;
import com.example.libr.dto.response.SimpleBookResponse;
import com.example.libr.dto.response.SimpleGenreResponse;
import com.example.libr.entity.Book;
import com.example.libr.entity.Genre;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    
    public Book toEntity(BookRequest request, Genre genre) {
        return Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publisher(request.getPublisher())
                .publicationYear(request.getPublicationYear())
                .description(request.getDescription())
                .numberOfPages(request.getNumberOfPages())
                .language(request.getLanguage())
                .coverImageUrl(request.getCoverImageUrl())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getAvailableCopies())
                .price(request.getPrice())
                .genre(genre)
                .build();
    }
    
    public BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .description(book.getDescription())
                .numberOfPages(book.getNumberOfPages())
                .language(book.getLanguage())
                .coverImageUrl(book.getCoverImageUrl())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .available(book.getAvailable())
                .price(book.getPrice())
                .genre(mapGenre(book.getGenre()))
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
    
    public SimpleBookResponse toSimpleResponse(Book book) {
        return SimpleBookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .available(book.getAvailable())
                .build();
    }
    
    private SimpleGenreResponse mapGenre(Genre genre) {
        if (genre == null) return null;
        
        return SimpleGenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .code(genre.getCode())
                .build();
    }
}

