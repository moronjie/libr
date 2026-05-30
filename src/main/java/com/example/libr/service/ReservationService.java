package com.example.libr.service;

import com.example.libr.dto.request.ReservationRequest;
import com.example.libr.dto.response.ReservationResponse;
import com.example.libr.entity.Book;
import com.example.libr.entity.Member;
import com.example.libr.entity.Reservation;
import com.example.libr.exception.AppException;
import com.example.libr.repository.BookRepository;
import com.example.libr.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final BookRepository bookRepository;
    private final EmailService emailService;

    private static final int MAX_ACTIVE_RESERVATIONS = 3;
    private static final int READY_EXPIRY_DAYS = 3;

    private static final List<Reservation.ReservationStatus> ACTIVE_RESERVATION_STATUSES =
            List.of(Reservation.ReservationStatus.PENDING,
                    Reservation.ReservationStatus.READY);


    @Transactional
    public ReservationResponse placeReservation(UUID userId, ReservationRequest request) {
        Member member = memberService.findMemberByUserId(userId);
        Book book = findBook(request.getBookId());

        validateMemberCanReserve(member, book);

        if (book.getAvailableCopies() > 0) {
            throw new AppException(
                    "This book is currently available. You can borrow it directly.",
                    HttpStatus.BAD_REQUEST);
        }

        int nextQueuePosition = reservationRepository
                .findMaxQueuePositionForBook(book) + 1;

        log.info(
                "Placing reservation for member {} on book '{}'. Next queue position: {}",
                member.getUser().getName(), book.getTitle(), nextQueuePosition
        );

        Reservation reservation = Reservation.builder()
                .member(member)
                .book(book)
                .status(Reservation.ReservationStatus.PENDING)
                .queuePosition(nextQueuePosition)
                .build();

        return mapToResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(UUID userId, UUID reservationId) {
        Member member = memberService.findMemberByUserId(userId);
        Reservation reservation = findReservation(reservationId);

        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new AppException("This reservation does not belong to you",
                    HttpStatus.FORBIDDEN);
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new AppException(
                    "Only PENDING reservations can be cancelled",
                    HttpStatus.BAD_REQUEST);
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        reorderQueue(reservation.getBook(), reservation.getQueuePosition());
        log.info(
                "Member {} cancelled reservation {} for book '{}'. Queue reordered.",
                member.getUser().getName(), reservation.getId(), reservation.getBook().getTitle()
        );

        return mapToResponse(reservation);
    }

    // Admin Cancel Reservation

    @Transactional
    public ReservationResponse adminCancelReservation(UUID reservationId) {
        Reservation reservation = findReservation(reservationId);

        if (reservation.getStatus() == Reservation.ReservationStatus.COLLECTED
                || reservation.getStatus() == Reservation.ReservationStatus.CANCELLED
                || reservation.getStatus() == Reservation.ReservationStatus.EXPIRED) {
            throw new AppException("This reservation cannot be cancelled", HttpStatus.BAD_REQUEST);
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        reorderQueue(reservation.getBook(), reservation.getQueuePosition());

        return mapToResponse(reservation);
    }


    public Page<ReservationResponse> getMyReservations(UUID userId, Pageable pageable) {
        Member member = memberService.findMemberByUserId(userId);
        return reservationRepository.findAllByMember(member, pageable)
                .map(this::mapToResponse);
    }

    public ReservationResponse getReservation(UUID reservationId) {
        return mapToResponse(findReservation(reservationId));
    }

    // Get All Reservations (Admin)

    public Page<ReservationResponse> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable).map(this::mapToResponse);
    }

    // Get Reservation Queue for a Book (Admin)

    public List<ReservationResponse> getBookReservationQueue(UUID bookId) {
        Book book = findBook(bookId);
        return reservationRepository
                .findAllByBookAndStatusOrderByQueuePositionAsc(
                        book, Reservation.ReservationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Called by BorrowService when a book is returned

    @Transactional
    public void processQueueOnReturn(Book book) {
        Optional<Reservation> nextInQueue = reservationRepository
                .findFirstByBookAndStatusOrderByQueuePositionAsc(
                        book, Reservation.ReservationStatus.PENDING);

        nextInQueue.ifPresent(reservation -> {
            reservation.setStatus(Reservation.ReservationStatus.READY);
            reservation.setReadyAt(LocalDateTime.now());
            reservation.setExpiresAt(LocalDateTime.now().plusDays(READY_EXPIRY_DAYS));
            reservationRepository.save(reservation);

            // Notify the member
            emailService.sendReservationReadyEmail(
                    reservation.getMember().getUser().getEmail(),
                    reservation.getMember().getUser().getName(),
                    reservation.getBook().getTitle(),
                    reservation.getExpiresAt()
            );
        });
    }

    // ── Mark Collected — called when member borrows a READY reserved book ─────

    @Transactional
    public void markReservationCollected(Member member, Book book) {
        reservationRepository
                .findFirstByBookAndStatusOrderByQueuePositionAsc(
                        book, Reservation.ReservationStatus.READY)
                .filter(r -> r.getMember().getId().equals(member.getId()))
                .ifPresent(r -> {
                    r.setStatus(Reservation.ReservationStatus.COLLECTED);
                    reservationRepository.save(r);
                });
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireReadyReservations() {
        List<Reservation> expired = reservationRepository
                .findExpiredReadyReservations(LocalDateTime.now());

        expired.forEach(reservation -> {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            // Notify member their reservation expired
            emailService.sendReservationExpiredEmail(
                    reservation.getMember().getUser().getEmail(),
                    reservation.getMember().getUser().getName(),
                    reservation.getBook().getTitle()
            );

            // Pass to next person in queue
            processQueueOnReturn(reservation.getBook());
        });
    }

    private void validateMemberCanReserve(Member member, Book book) {
        if (!member.isProfileComplete()) {
            throw new AppException("Please complete your profile before placing a reservation",
                    HttpStatus.FORBIDDEN);
        }

        if (member.getStatus() != Member.MemberStatus.ACTIVE) {
            throw new AppException("Your account is not active. Contact the library for assistance",
                    HttpStatus.FORBIDDEN);
        }

        boolean alreadyReserved = reservationRepository
                .existsByMemberAndBookAndStatusIn(member, book, ACTIVE_RESERVATION_STATUSES);
        if (alreadyReserved) {
            throw new AppException("You already have an active reservation for this book",
                    HttpStatus.BAD_REQUEST);
        }

        long activeReservationCount = reservationRepository
                .countByMemberAndStatusIn(member, ACTIVE_RESERVATION_STATUSES);
        if (activeReservationCount >= MAX_ACTIVE_RESERVATIONS) {
            throw new AppException(
                    "You have reached the maximum of " + MAX_ACTIVE_RESERVATIONS
                            + " active reservations",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void reorderQueue(Book book, int cancelledPosition) {
        List<Reservation> remaining = reservationRepository
                .findAllByBookAndStatusOrderByQueuePositionAsc(
                        book, Reservation.ReservationStatus.PENDING)
                .stream()
                .filter(r -> r.getQueuePosition() > cancelledPosition)
                .toList();

        remaining.forEach(r -> r.setQueuePosition(r.getQueuePosition() - 1));
        reservationRepository.saveAll(remaining);
    }

    // Helpers
    private Reservation findReservation(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new AppException("Reservation not found",
                        HttpStatus.NOT_FOUND));
    }

    private Book findBook(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException("Book not found", HttpStatus.NOT_FOUND));
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .memberId(reservation.getMember().getId())
                .memberName(reservation.getMember().getUser().getName())
                .bookId(reservation.getBook().getId())
                .bookTitle(reservation.getBook().getTitle())
                .bookAuthor(reservation.getBook().getAuthor())
                .status(reservation.getStatus().name())
                .queuePosition(reservation.getQueuePosition())
                .readyAt(reservation.getReadyAt())
                .expiresAt(reservation.getExpiresAt())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
