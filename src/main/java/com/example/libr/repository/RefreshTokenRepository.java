package com.example.libr.repository;

import com.example.libr.entity.RefreshToken;
import com.example.libr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findAllByUser(User user);
    void deleteAllByUser(User user);
}