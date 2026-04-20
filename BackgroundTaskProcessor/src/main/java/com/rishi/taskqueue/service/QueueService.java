package com.rishi.taskqueue.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rishi.taskqueue.exception.QueueEmptyException;
import com.rishi.taskqueue.model.QueueToken;
import com.rishi.taskqueue.repository.QueueTokenRepository;

import jakarta.annotation.PostConstruct;

@Service
public class QueueService {

    private final QueueTokenRepository repository;
    private final RedisQueueService redisQueue;
    private final RedisTemplate<String, String> redisTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private int tokenCounter = 1;

    public QueueService(QueueTokenRepository repository, RedisQueueService redisQueue, RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisQueue = redisQueue;
        this.redisTemplate = redisTemplate;

        // Start worker threads
        // for (int i = 0; i < 3; i++) {
        //     executor.submit(() -> {
        //         while (true) {
        //             try {
        //                 String tokenId = redisQueue.popTask();

        //                 if (tokenId == null) {
        //                     Thread.sleep(1000);
        //                     continue;
        //                 }

        //                 QueueToken token = repository.findById(tokenId)
        //                         .orElseThrow(() -> new RuntimeException("Token not found"));

        //                 processToken(token);

        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //             }
        //         }
        //     });
        // }
    }

    public String generateToken() {
        String id = UUID.randomUUID().toString();

        QueueToken token = new QueueToken();
        token.setId(id);
        token.setTokenNumber(tokenCounter++);
        token.setStatus("WAITING");
        token.setCreatedAt(LocalDateTime.now());

        repository.save(token);
        System.out.println("Token created and pushing to Redis");
        try {
            redisQueue.pushTask(id);
        } catch (Exception e) {
            System.out.println("Redis push failed for token: " + id);
        }

        return id;
    }

    @PostConstruct
    public void recoverPendingTokens() {
        List<QueueToken> tokens = repository.findByStatus("WAITING");

        List<String> queue = redisTemplate.opsForList()
                .range("task_queue", 0, -1);

        for (QueueToken token : tokens) {
            if (queue == null || !queue.contains(token.getId())) {
                redisQueue.pushTask(token.getId());
            }
        }
    }


    @PostConstruct
    public void resetOnStartup() {
        repository.deleteAll();              // clear DB
        redisTemplate.delete("task_queue");  // clear Redis queue
        tokenCounter = 1;                    // reset counter
    }

    public void resetQueue() {
        repository.deleteAll();              // clear DB
        redisTemplate.delete("task_queue");  // clear Redis queue
        tokenCounter = 1;                    // reset counter
    }

    public QueueToken getToken(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

    public QueueToken serveNextToken() {
        String id = redisQueue.popTask();

    if (id == null) {
        throw new QueueEmptyException("Queue is empty");
    }
        QueueToken token = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setStatus("SERVING");
        return repository.save(token);
    }

    private void processToken(QueueToken token) {

        try {
            token.setStatus("SERVING");
            repository.save(token);

            Thread.sleep(5000); // simulate work

            token.setStatus("COMPLETED");
            repository.save(token);

        } catch (Exception e) {
            token.setStatus("FAILED");
            repository.save(token);
        }
    }
}