package com.example.libr.repository;

import com.example.libr.entity.Book;
import com.example.libr.entity.BorrowRecord;
import com.example.libr.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BorrowRepository extends JpaRepository<BorrowRecord, UUID> {

    long countByMemberAndStatusIn(Member member, List<BorrowRecord.BorrowStatus> statuses);

    boolean existsByMemberAndBookAndStatusIn(
            Member member, Book book, List<BorrowRecord.BorrowStatus> statuses);

    Page<BorrowRecord> findAllByMember(Member member, Pageable pageable);

    List<BorrowRecord> findAllByStatus(BorrowRecord.BorrowStatus status);

    Page<BorrowRecord> findAllByBook(Book book, Pageable pageable);

    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'ACTIVE' " +
            "AND b.dueDate < :today")
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);
}
