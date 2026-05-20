package com.example.libr.service.impl;

import com.example.libr.dto.request.BookRequest;
import com.example.libr.dto.response.BookResponse;

import java.util.List;

public interface IBookService {
    BookResponse createBook(BookRequest request);
    List<BookResponse> getAllBooks();
    BookResponse getBookById(String id);
    BookResponse getBookByIsbn(String isbn);
    List<BookResponse> getBooksByAuthor(String author);
    List<BookResponse> getBooksByGenre(String genreId);
    List<BookResponse> getBooksByTitle(String title);
    BookResponse updateBook(String id, BookRequest request);
    List<BookResponse> searchBooks(String query);
    List<BookResponse> getAvailableBooks();
    List<BookResponse> getBooksByPublicationYear(Integer year);
    List<BookResponse> getBooksByPublisher(String publisher);
    List<BookResponse> getBooksByLanguage(String language);
    List<BookResponse> getBooksByPriceRange(Double minPrice, Double maxPrice);
    List<BookResponse> getBooksTotalCopies(String id);
    void softDeleteBook(String id);
    void deleteBook(String id);
}
