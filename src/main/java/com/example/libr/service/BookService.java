package com.example.libr.service;

import com.example.libr.dto.request.BookRequest;
import com.example.libr.dto.response.BookResponse;
import com.example.libr.entity.Book;
import com.example.libr.entity.Genre;
import com.example.libr.exception.ConflictException;
import com.example.libr.exception.ResourceNotFoundException;
import com.example.libr.mapper.BookMapper;
import com.example.libr.repository.BookRepository;
import com.example.libr.repository.GenreRepository;
import com.example.libr.service.impl.IBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class BookService implements IBookService {
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;
    
    @Override
    public BookResponse createBook(BookRequest request) {
        Book existingBook = bookRepository.findByIsbn(request.getIsbn());
        if (existingBook != null) {
            throw new ConflictException("Book with ISBN '" + request.getIsbn() + "' already exists");
        }

        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre with ID '" + request.getGenreId() + "' not found"));

        Book book = bookMapper.toEntity(request, genre);

        if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
            book.setAvailable(true);
        }
        
        // Save and return
        Book savedBook = bookRepository.save(book);
        return bookMapper.toResponse(savedBook);
    }

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public BookResponse getBookById(String id) {
        UUID bookId = UUID.fromString(id);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID '" + id + "' not found"));
        return bookMapper.toResponse(book);
    }

    @Override
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            throw new ResourceNotFoundException("Book with ISBN '" + isbn + "' not found");
        }
        return bookMapper.toResponse(book);
    }

    @Override
    public List<BookResponse> getBooksByAuthor(String author) {
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getBooksByGenre(String genreId) {
        UUID uuid = UUID.fromString(genreId);
        Genre genre = genreRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Genre with ID '" + genreId + "' not found"));
        
        List<Book> books = bookRepository.findByGenre(genre);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getBooksByTitle(String title) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public BookResponse updateBook(String id, BookRequest request) {
        UUID bookId = UUID.fromString(id);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID '" + id + "' not found"));

        Book existingBook = bookRepository.findByIsbn(request.getIsbn());
        if (existingBook != null && !existingBook.getId().equals(bookId)) {
            throw new ConflictException("Book with ISBN '" + request.getIsbn() + "' already exists");
        }

        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre with ID '" + request.getGenreId() + "' not found"));
        
        // Update book fields
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setPublicationYear(request.getPublicationYear());
        book.setDescription(request.getDescription());
        book.setNumberOfPages(request.getNumberOfPages());
        book.setLanguage(request.getLanguage());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getAvailableCopies());
        book.setPrice(request.getPrice());
        book.setGenre(genre);
        
        // Update availability
        book.setAvailable(book.getAvailableCopies() != null && book.getAvailableCopies() > 0);
        
        Book updatedBook = bookRepository.save(book);
        return bookMapper.toResponse(updatedBook);
    }

    @Override
    public List<BookResponse> searchBooks(String query) {
        List<Book> books = bookRepository.searchBooks(query);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getAvailableBooks() {
        List<Book> books = bookRepository.findByAvailableTrue();
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getBooksByPublicationYear(Integer year) {
        List<Book> books = bookRepository.findByPublicationYear(year);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getBooksByPublisher(String publisher) {
        List<Book> books = bookRepository.findByPublisherContainingIgnoreCase(publisher);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getBooksByLanguage(String language) {
        List<Book> books = bookRepository.findByLanguageIgnoreCase(language);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getBooksByPriceRange(Double minPrice, Double maxPrice) {
        List<Book> books = bookRepository.findByPriceBetween(minPrice, maxPrice);
        return books.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookResponse> getBooksTotalCopies(String id) {
        // This method signature seems incorrect - returning the book with its copy count
        UUID bookId = UUID.fromString(id);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID '" + id + "' not found"));
        return List.of(bookMapper.toResponse(book));
    }

    @Override
    public void softDeleteBook(String id) {
        UUID bookId = UUID.fromString(id);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID '" + id + "' not found"));
        
        // Mark as unavailable (soft delete)
        book.setAvailable(false);
        book.setAvailableCopies(0);
        bookRepository.save(book);
    }

    @Override
    public void deleteBook(String id) {
        UUID bookId = UUID.fromString(id);
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book with ID '" + id + "' not found");
        }
        bookRepository.deleteById(bookId);
    }
}
