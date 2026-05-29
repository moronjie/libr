package com.example.libr.controller;

import com.example.libr.dto.request.BorrowRequest;
import com.example.libr.dto.response.BorrowResponse;
import com.example.libr.payload.ApiResponse;
import com.example.libr.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // ── POST /api/v1/borrows  (USER - borrow a book)
    @PostMapping
    public ResponseEntity<ApiResponse<BorrowResponse>> borrowBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BorrowRequest request) {
        BorrowResponse response = borrowService.borrowBook(
                UUID.fromString(userDetails.getUsername()), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<BorrowResponse>builder()
                        .message("Book borrowed successfully")
                        .data(response)
                        .build());
    }

    // ── POST /api/v1/borrows/{id}/return  (USER - return a book)
    @PostMapping("/{id}/return")
    public ResponseEntity<ApiResponse<BorrowResponse>> returnBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        BorrowResponse response = borrowService.returnBook(
                UUID.fromString(userDetails.getUsername()), id);
        return ResponseEntity
                .ok(ApiResponse.<BorrowResponse>builder()
                        .message("Book returned successfully")
                        .data(response)
                        .build());
    }

    // ── GET /api/v1/borrows/me/history  (USER - my borrow history)
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getMyBorrowHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<BorrowResponse> response = borrowService.getMyBorrowHistory(
                UUID.fromString(userDetails.getUsername()), pageable);
        return ResponseEntity
                .ok(ApiResponse.<Page<BorrowResponse>>builder()
                        .message("Borrow history retrieved successfully")
                        .data(response)
                        .build());
    }

    // ── GET /api/v1/borrows/me/active  (USER - my active borrows)
    @GetMapping("/me/active")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getMyActiveBorrows(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<BorrowResponse> response = borrowService.getMyActiveBorrows(
                UUID.fromString(userDetails.getUsername()), pageable);
        return ResponseEntity
                .ok(ApiResponse.<Page<BorrowResponse>>builder()
                        .message("Active borrows retrieved successfully")
                        .data(response)
                        .build());
    }

    // ── GET /api/v1/borrows  (ADMIN - all borrow records)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getAllBorrowRecords(Pageable pageable) {
        Page<BorrowResponse> response = borrowService.getAllBorrowRecords(pageable);
        return ResponseEntity
                .ok(ApiResponse.<Page<BorrowResponse>>builder()
                        .message("All borrow records retrieved successfully")
                        .data(response)
                        .build());
    }

    // ── GET /api/v1/borrows/{id}  (ADMIN - single borrow record)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BorrowResponse>> getBorrowRecord(@PathVariable UUID id) {
        BorrowResponse response = borrowService.getBorrowRecord(id);
        return ResponseEntity
                .ok(ApiResponse.<BorrowResponse>builder()
                        .message("Borrow record retrieved successfully")
                        .data(response)
                        .build());
    }

    // ── GET /api/v1/borrows/book/{bookId}  (ADMIN - borrow history for a book)
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BorrowResponse>>> getBookBorrowHistory(
            @PathVariable UUID bookId,
            Pageable pageable) {
        Page<BorrowResponse> response = borrowService.getBookBorrowHistory(bookId, pageable);
        return ResponseEntity
                .ok(ApiResponse.<Page<BorrowResponse>>builder()
                        .message("Book borrow history retrieved successfully")
                        .data(response)
                        .build());
    }

    // ── POST /api/v1/borrows/{id}/admin-return  (ADMIN - force return a book)
    @PostMapping("/{id}/admin-return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BorrowResponse>> adminReturnBook(@PathVariable UUID id) {
        BorrowResponse response = borrowService.adminReturnBook(id);
        return ResponseEntity
                .ok(ApiResponse.<BorrowResponse>builder()
                        .message("Book returned successfully by admin")
                        .data(response)
                        .build());
    }
}
