package com.rishi.taskqueue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rishi.taskqueue.model.QueueToken;

public interface QueueTokenRepository extends JpaRepository<QueueToken, String> {
    List<QueueToken> findByStatus(String status);

    @Query("SELECT MAX(q.tokenNumber) FROM QueueToken q")
    Integer findMaxTokenNumber();
}