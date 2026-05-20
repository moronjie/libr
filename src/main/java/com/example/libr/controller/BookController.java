package com.example.libr.controller;

import com.example.libr.dto.request.BookRequest;
import com.example.libr.dto.response.BookResponse;
import com.example.libr.payload.ApiResponse;
import com.example.libr.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<BookResponse>builder()
                        .message("Book created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBooks();
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable String id) {
        BookResponse response = bookService.getBookById(id);
        return ResponseEntity
                .ok(ApiResponse.<BookResponse>builder()
                        .message("Book retrieved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookByIsbn(@PathVariable String isbn) {
        BookResponse response = bookService.getBookByIsbn(isbn);
        return ResponseEntity
                .ok(ApiResponse.<BookResponse>builder()
                        .message("Book retrieved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByAuthor(@PathVariable String author) {
        List<BookResponse> books = bookService.getBooksByAuthor(author);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books by author retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/genre/{genreId}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByGenre(@PathVariable String genreId) {
        List<BookResponse> books = bookService.getBooksByGenre(genreId);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books by genre retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByTitle(@PathVariable String title) {
        List<BookResponse> books = bookService.getBooksByTitle(title);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books by title retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchBooks(@RequestParam String query) {
        List<BookResponse> books = bookService.searchBooks(query);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Search results retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAvailableBooks() {
        List<BookResponse> books = bookService.getAvailableBooks();
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Available books retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByPublicationYear(@PathVariable Integer year) {
        List<BookResponse> books = bookService.getBooksByPublicationYear(year);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books by publication year retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/publisher/{publisher}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByPublisher(@PathVariable String publisher) {
        List<BookResponse> books = bookService.getBooksByPublisher(publisher);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books by publisher retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByLanguage(@PathVariable String language) {
        List<BookResponse> books = bookService.getBooksByLanguage(language);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books by language retrieved successfully")
                        .data(books)
                        .build());
    }

    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        List<BookResponse> books = bookService.getBooksByPriceRange(minPrice, maxPrice);
        return ResponseEntity
                .ok(ApiResponse.<List<BookResponse>>builder()
                        .message("Books by price range retrieved successfully")
                        .data(books)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable String id,
            @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity
                .ok(ApiResponse.<BookResponse>builder()
                        .message("Book updated successfully")
                        .data(response)
                        .build());
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<ApiResponse<Void>> softDeleteBook(@PathVariable String id) {
        bookService.softDeleteBook(id);
        return ResponseEntity
                .ok(ApiResponse.<Void>builder()
                        .message("Book marked as unavailable successfully")
                        .data(null)
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity
                .ok(ApiResponse.<Void>builder()
                        .message("Book deleted successfully")
                        .data(null)
                        .build());
    }
}
