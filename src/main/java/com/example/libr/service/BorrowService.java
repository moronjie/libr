package com.example.libr.service;

import com.example.libr.dto.request.BorrowRequest;
import com.example.libr.dto.response.BorrowResponse;
import com.example.libr.entity.Book;
import com.example.libr.entity.BorrowRecord;
import com.example.libr.entity.Member;
import com.example.libr.exception.AppException;
import com.example.libr.repository.BookRepository;
import com.example.libr.repository.BorrowRepository;
import com.example.libr.service.impl.IBorrowService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BorrowService implements IBorrowService {

    private final BorrowRepository borrowRepository;
    private final MemberService memberService;
    private final BookRepository bookRepository;
    private final ReservationService reservationService;

    private static final List<BorrowRecord.BorrowStatus> ACTIVE_STATUSES =
            List.of(BorrowRecord.BorrowStatus.ACTIVE, BorrowRecord.BorrowStatus.OVERDUE);


    @Transactional
    public BorrowResponse borrowBook(UUID userId, BorrowRequest request) {
        Member member = memberService.findMemberByUserId(userId);
        Book book = findBook(request.getBookId());

        long activeBorrowCount = borrowRepository
                .countByMemberAndStatusIn(member, ACTIVE_STATUSES);
        memberService.validateCanBorrow(member, (int) activeBorrowCount);

        validateBookAvailability(book);
        validateNoDuplicateBorrow(member, book);

        // Calculate due date based on member tier
        LocalDate borrowedAt = LocalDate.now();
        LocalDate dueDate = borrowedAt.plusDays(member.getTier().getLoanPeriodDays());

        BorrowRecord record = BorrowRecord.builder()
                .member(member)
                .book(book)
                .borrowedAt(borrowedAt)
                .dueDate(dueDate)
                .status(BorrowRecord.BorrowStatus.ACTIVE)
                .build();

        borrowRepository.save(record);

        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return mapToResponse(record);
    }

    @Transactional
    public BorrowResponse returnBook(UUID userId, UUID borrowRecordId) {
        Member member = memberService.findMemberByUserId(userId);
        BorrowRecord record = findBorrowRecord(borrowRecordId);

        if (!record.getMember().getId().equals(member.getId())) {
            throw new AppException("This borrow record does not belong to you",
                    HttpStatus.FORBIDDEN);
        }

        if (record.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new AppException("This book has already been returned",
                    HttpStatus.BAD_REQUEST);
        }

        record.setReturnedAt(LocalDate.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        borrowRepository.save(record);

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // Trigger reservation queue
        reservationService.processQueueOnReturn(book);

        return mapToResponse(record);
    }


    public Page<BorrowResponse> getMyBorrowHistory(UUID userId, Pageable pageable) {
        Member member = memberService.findMemberByUserId(userId);
        return borrowRepository.findAllByMember(member, pageable)
                .map(this::mapToResponse);
    }

    public Page<BorrowResponse> getMyActiveBorrows(UUID userId, Pageable pageable) {
        Member member = memberService.findMemberByUserId(userId);
        return borrowRepository.findAllByMember(member, pageable)
                .map(this::mapToResponse)
                .map(r -> r);
    }

    public Page<BorrowResponse> getAllBorrowRecords(Pageable pageable) {
        return borrowRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<BorrowResponse> getBookBorrowHistory(UUID bookId, Pageable pageable) {
        Book book = findBook(bookId);
        return borrowRepository.findAllByBook(book, pageable).map(this::mapToResponse);
    }

    public BorrowResponse getBorrowRecord(UUID borrowRecordId) {
        return mapToResponse(findBorrowRecord(borrowRecordId));
    }

    @Transactional
    public BorrowResponse adminReturnBook(UUID borrowRecordId) {
        BorrowRecord record = findBorrowRecord(borrowRecordId);

        if (record.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new AppException("This book has already been returned",
                    HttpStatus.BAD_REQUEST);
        }

        record.setReturnedAt(LocalDate.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        borrowRepository.save(record);

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return mapToResponse(record);
    }


    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markOverdueRecords() {
        List<BorrowRecord> overdueRecords =
                borrowRepository.findOverdueRecords(LocalDate.now());
        overdueRecords.forEach(r -> r.setStatus(BorrowRecord.BorrowStatus.OVERDUE));
        borrowRepository.saveAll(overdueRecords);
    }

    // ── Validations ───────────────────────────────────────────────────────────

    private void validateBookAvailability(Book book) {
        if (book.getAvailableCopies() <= 0) {
            throw new AppException(
                    "No available copies of this book. You can place a reservation instead.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validateNoDuplicateBorrow(Member member, Book book) {
        boolean alreadyBorrowed = borrowRepository
                .existsByMemberAndBookAndStatusIn(member, book, ACTIVE_STATUSES);
        if (alreadyBorrowed) {
            throw new AppException(
                    "You already have an active borrow for this book",
                    HttpStatus.BAD_REQUEST);
        }
    }

    // Helpers

    private BorrowRecord findBorrowRecord(UUID id) {
        return borrowRepository.findById(id)
                .orElseThrow(() -> new AppException("Borrow record not found",
                        HttpStatus.NOT_FOUND));
    }

    private Book findBook(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException("Book not found", HttpStatus.NOT_FOUND));
    }

    private BorrowResponse mapToResponse(BorrowRecord record) {
        LocalDate today = LocalDate.now();
        boolean isOverdue = record.getStatus() == BorrowRecord.BorrowStatus.OVERDUE
                || (record.getStatus() == BorrowRecord.BorrowStatus.ACTIVE
                && record.getDueDate().isBefore(today));
        long overdueDays = isOverdue
                ? ChronoUnit.DAYS.between(record.getDueDate(), today)
                : 0;

        return BorrowResponse.builder()
                .id(record.getId())
                .memberId(record.getMember().getId())
                .memberName(record.getMember().getUser().getName())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .bookAuthor(record.getBook().getAuthor())
                .borrowedAt(record.getBorrowedAt())
                .dueDate(record.getDueDate())
                .returnedAt(record.getReturnedAt())
                .status(record.getStatus().name())
                .isOverdue(isOverdue)
                .overdueDays(overdueDays)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}