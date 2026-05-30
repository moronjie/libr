package com.example.libr.repository;

import com.example.libr.entity.Book;
import com.example.libr.entity.Member;
import com.example.libr.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    boolean existsByMemberAndBookAndStatusIn(
            Member member, Book book, List<Reservation.ReservationStatus> statuses);

    long countByMemberAndStatusIn(
            Member member, List<Reservation.ReservationStatus> statuses);

    List<Reservation> findAllByBookAndStatusOrderByQueuePositionAsc(
            Book book, Reservation.ReservationStatus status);

    Optional<Reservation> findFirstByBookAndStatusOrderByQueuePositionAsc(
            Book book, Reservation.ReservationStatus status);

    // Get the current highest queue position for a book
    @Query("SELECT COALESCE(MAX(r.queuePosition), 0) FROM Reservation r " +
            "WHERE r.book = :book AND r.status = 'PENDING'")
    int findMaxQueuePositionForBook(@Param("book") Book book);

    Page<Reservation> findAllByMember(Member member, Pageable pageable);

    Page<Reservation> findAllByBook(Book book, Pageable pageable);

    // Find READY reservations that have passed their expiry — for cron job
    @Query("SELECT r FROM Reservation r WHERE r.status = 'READY' " +
            "AND r.expiresAt < :now")
    List<Reservation> findExpiredReadyReservations(@Param("now") LocalDateTime now);
}