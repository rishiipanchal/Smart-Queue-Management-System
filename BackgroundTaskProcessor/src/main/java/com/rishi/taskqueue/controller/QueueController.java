package com.rishi.taskqueue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rishi.taskqueue.model.QueueToken;
import com.rishi.taskqueue.service.QueueService;
import com.rishi.taskqueue.service.RedisQueueService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;
    private final RedisQueueService redisQueue;

    public QueueController(QueueService queueService, RedisQueueService redisQueue) {
        this.queueService = queueService;
        this.redisQueue = redisQueue;
    }

    @PostMapping("/token")
    public String createToken(@Valid @RequestBody(required = false) Object dummy) {
        return queueService.generateToken();
    }

    @GetMapping("/token/{id}")
    public QueueToken getToken(@PathVariable String id) {
        return queueService.getToken(id);
    }

    @PostMapping("/serve")
    public ResponseEntity<?> serveNext() {
        try {
            QueueToken token = queueService.serveNextToken();
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(200).body(e.getMessage());
        }
    }

    @GetMapping("/size")
    public Long getQueueSize() {
        return redisQueue.getQueueSize();
    }

    @PostMapping("/reset")
    public String reset() {
        queueService.resetQueue();
        return "reset";
    }
}