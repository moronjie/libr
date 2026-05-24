package com.example.libr.repository;

import com.example.libr.entity.Member;
import com.example.libr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByUser(User user);
    Optional<Member> findByNationalId(String nationalId);
    boolean existsByNationalId(String nationalId);
    boolean existsByUser(User user);

    @Query("SELECT m FROM Member m WHERE m.membershipExpiresAt < :today " +
            "AND m.status = 'ACTIVE'")
    List<Member> findExpiredActiveMembers(@Param("today") LocalDate today);
}
