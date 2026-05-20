package com.example.libr.repository;

import com.example.libr.entity.Book;
import com.example.libr.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    Book findByIsbn(String isbn);
    
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    List<Book> findByGenre(Genre genre);
    
    List<Book> findByAvailableTrue();
    
    List<Book> findByPublicationYear(Integer year);
    
    List<Book> findByPublisherContainingIgnoreCase(String publisher);
    
    List<Book> findByLanguageIgnoreCase(String language);
    
    List<Book> findByPriceBetween(Double minPrice, Double maxPrice);
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooks(@Param("query") String query);
}
